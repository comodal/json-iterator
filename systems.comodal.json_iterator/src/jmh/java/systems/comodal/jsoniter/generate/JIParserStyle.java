package systems.comodal.jsoniter.generate;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static systems.comodal.jsoniter.JIUtil.fieldHashCode;

public enum JIParserStyle {

  IFELSE() {
    @Override
    JIParser createParser(final JIParserConfig config,
                          final JIParserGenerator generator) {
      final var fields = generator.getSortedFields();
      if (fields == null || fields.isEmpty()) {
        return null;
      }
      final var parserName = formatParserName(generator.getParentNameChain().replace('.', '_'));
      final var tab = config.getTab();
      final var builder = startParser(tab, parserName);
      builder.append(tab).append(tab);
      fields.values().stream()
          .sorted(Comparator.comparing(JIParserGenerator::getParentName))
          .forEach(childGenerator -> {
            builder
                .append(format("if (fieldEquals(\"%s\", buf, offset, len)) {", childGenerator.getParentName()))
                .append(lineSeparator());
            childGenerator.printLogic(config, builder, tab);
            builder.append(tab).append(tab).append("} else ");
          });
      builder.append('{').append(lineSeparator());
      if (config.skipUnexpectedFields()) {
        builder.append(tab).append(tab).append(tab).append("ji.skip();")
            .append(lineSeparator());
      } else {
        printUnexpected(tab + tab, tab, builder, generator);
      }
      final var parserCode = builder
          .append(tab).append(tab).append('}')
          .append(lineSeparator())
          .append(tab).append(tab).append("return true;")
          .append(lineSeparator())
          .append(tab).append("};")
          .append(lineSeparator())
          .toString();
      return JIParser.create(parserName, parserCode);
    }
  },
  NLOGN {
    @Override
    JIParser createParser(final JIParserConfig config,
                          final JIParserGenerator generator) {
      final var fields = generator.getSortedFields();
      if (fields == null || fields.isEmpty()) {
        return null;
      }
      final var parserName = formatParserName(generator.getParentNameChain().replace('.', '_'));
      final var tab = config.getTab();
      final var builder = startParser(tab, parserName);

      final JIParserGenerator[] generators = fields.values().toArray(new JIParserGenerator[0]);
      int i = generators.length >> 1;
      var childGenerator = generators[i];
      if (i == 0) {
        builder.append(tab).append(tab)
            .append(format("if (fieldEquals(\"%s\", buf, offset, len)) {", childGenerator.getParentName()))
            .append(lineSeparator());
        childGenerator.printLogic(config, builder, tab);
        builder.append(tab).append(tab).append(tab).append("return true;")
            .append(lineSeparator())
            .append(tab).append(tab).append('}')
            .append(lineSeparator());
      } else {
        final var nextDepthTab = tab + tab;
        builder.append(tab).append(tab).append("int c;")
            .append(lineSeparator())
            .append(tab).append(tab)
            .append(format("if ((c = fieldCompare(\"%s\", buf, offset, len)) == 0) {", childGenerator.getParentName()))
            .append(lineSeparator());
        childGenerator.printLogic(config, builder, tab);
        builder.append(tab).append(tab).append(tab).append("return true;")
            .append(lineSeparator())
            .append(tab).append(tab)
            .append("} else if (c < 0) {")
            .append(lineSeparator())
            .append(nextDepthTab).append(tab);

        printNLogNParserField(config, nextDepthTab, builder, generators, i >> 1, 0, i);
        if (i < (generators.length - 1)) {
          builder.append(tab).append(tab).append("} else ");
          final int nextIndex = i + ((generators.length - i) >> 1);
          final int nextFrom = i + 1;
          if (nextFrom - nextIndex < 3) {
            printNLogNParserField(config, tab, builder, generators, nextIndex, nextFrom, generators.length);
          } else {
            builder.append('{')
                .append(lineSeparator())
                .append(nextDepthTab).append(tab);
            printNLogNParserField(config, nextDepthTab, builder, generators, nextIndex, nextFrom, generators.length);
            builder.append(tab).append(tab).append('}').append(lineSeparator());
          }
        } else {
          builder.append(tab).append(tab).append('}').append(lineSeparator());
        }
      }
      if (config.skipUnexpectedFields()) {
        builder.append(tab).append(tab).append("ji.skip();")
            .append(lineSeparator())
            .append(tab).append(tab).append("return true;")
            .append(lineSeparator());
      } else {
        printUnexpected(tab, tab, builder, generator);
      }
      final var parserCode = builder
          .append(tab).append("};")
          .append(lineSeparator())
          .toString();
      return JIParser.create(parserName, parserCode);
    }

    private void printNLogNParserField(final JIParserConfig config,
                                       final String depthTab,
                                       final StringBuilder builder,
                                       final JIParserGenerator[] generators,
                                       final int i, final int from, final int to) {
      final var tab = config.getTab();
      final var generator = generators[i];
      if (from == (to - 1)) {
        builder.append(format("if (fieldEquals(\"%s\", buf, offset, len)) {", generator.getParentName()))
            .append(lineSeparator());
        generator.printLogic(config, builder, depthTab);
        builder.append(depthTab).append(tab).append(tab).append("return true;")
            .append(lineSeparator())
            .append(depthTab).append(tab).append('}')
            .append(lineSeparator());
      } else {
        builder.append(format("if ((c = fieldCompare(\"%s\", buf, offset, len)) == 0) {", generator.getParentName()))
            .append(lineSeparator());
        generator.printLogic(config, builder, depthTab);
        builder.append(depthTab).append(tab).append(tab).append("return true;")
            .append(lineSeparator());
        final var nextDepthTab = depthTab + tab;
        if (i > from) {
          builder.append(depthTab).append(tab)
              .append("} else if (c < 0) {")
              .append(lineSeparator())
              .append(nextDepthTab).append(tab);
          printNLogNParserField(config, nextDepthTab, builder, generators, from + ((i - from) >> 1), from, i);
          if (i < (to - 1)) {
            if (i + 1 == (to - 1)) {
              final var next = generators[i + 1];
              builder.append(depthTab).append(tab)
                  .append(format("} else if (fieldEquals(\"%s\", buf, offset, len)) {", next.getParentName()))
                  .append(lineSeparator());
              next.printLogic(config, builder, depthTab);
              builder.append(depthTab).append(tab).append(tab).append("return true;")
                  .append(lineSeparator())
                  .append(depthTab).append(tab).append('}')
                  .append(lineSeparator());
            } else {
              builder.append(depthTab).append(tab).append("} else ");
              final int nextIndex = i + ((to - i) >> 1);
              final int nextFrom = i + 1;
              if (nextFrom - nextIndex < 3) {
                printNLogNParserField(config, depthTab, builder, generators, nextIndex, nextFrom, to);
              } else {
                builder.append('{')
                    .append(lineSeparator())
                    .append(nextDepthTab).append(tab);
                printNLogNParserField(config, nextDepthTab, builder, generators, nextIndex, nextFrom, to);
                builder.append(depthTab).append(tab).append('}').append(lineSeparator());
              }
            }
          } else {
            builder.append(depthTab).append(tab).append('}').append(lineSeparator());
          }
        } else {
          final var next = generators[i + 1];
          builder.append(depthTab).append(tab)
              .append(format("} else if (c > 0 && fieldEquals(\"%s\", buf, offset, len)) {", next.getParentName()))
              .append(lineSeparator());
          next.printLogic(config, builder, depthTab);
          builder.append(depthTab).append(tab).append(tab).append("return true;")
              .append(lineSeparator())
              .append(depthTab).append(tab).append('}')
              .append(lineSeparator());
        }
      }
    }
  },
  NHASHN {
    @Override
    JIParser createParser(final JIParserConfig config,
                          final JIParserGenerator generator) {
      final var fields = generator.getSortedFields();
      if (fields == null || fields.isEmpty()) {
        return null;
      }
      final var parserName = formatParserName(generator.getParentNameChain().replace('.', '_'));
      final var tab = config.getTab();
      final var builder = startParser(tab, parserName);

      final JIParserGenerator[] generators = fields.values().toArray(new JIParserGenerator[0]);
      Arrays.sort(generators, Comparator.comparing(JIParserGenerator::getParentName));
      int maxCommonPrefixLength = 1;
      int maxFieldLength = 1;
      String reference, other;

      for (int i = 0; i < generators.length; i++) {
        reference = generators[i].getParentName();
        if (reference.length() > maxFieldLength) {
          maxFieldLength = reference.length();
        }
        for (int j = i + 1; j < generators.length; j++) {
          other = generators[j].getParentName();
          if (reference.regionMatches(0, other, 0, maxCommonPrefixLength)) {
            do {
              ++maxCommonPrefixLength;
            } while (reference.regionMatches(0, other, 0, maxCommonPrefixLength));
          } else {
            break;
          }
        }
      }
      final var deDuplicate = new HashSet<Integer>(generators.length);
      AVOID_COLLISIONS:
      while (maxCommonPrefixLength < maxFieldLength) {
        for (final var g : generators) {
          final var chars = g.getParentName().toCharArray();
          final int hashCode = fieldHashCode(chars, 0, Math.min(chars.length, maxCommonPrefixLength));
          if (!deDuplicate.add(hashCode)) {
            deDuplicate.clear();
            maxCommonPrefixLength++;
            continue AVOID_COLLISIONS;
          }
        }
        break;
      }

      final int _maxCommonPrefixLength = maxCommonPrefixLength;
      final var grouped = Arrays.stream(generators).collect(Collectors.groupingBy(g -> {
        final var chars = g.getParentName().toCharArray();
        return fieldHashCode(chars, 0, Math.min(chars.length, _maxCommonPrefixLength));
      }, LinkedHashMap::new, Collectors.toList()));
      builder.append(tab).append(tab)
          .append(format("switch(fieldHashCode(buf, offset, len < %d ? offset + len : offset + %d)) {", maxCommonPrefixLength, maxCommonPrefixLength))
          .append(lineSeparator());
      final var logicTab = tab + tab + tab;
      for (final var hashGroup : grouped.entrySet()) {
        final int hashCode = hashGroup.getKey();
        builder.append(logicTab).append(format("case %d:", hashCode))
            .append(lineSeparator())
            .append(logicTab).append(tab);
        final var iterator = hashGroup.getValue().iterator();
        for (var childGenerator = iterator.next(); ; ) {
          builder.append(format("if (fieldEquals(\"%s\", buf, offset, len)) {", childGenerator.getParentName()))
              .append(lineSeparator());
          childGenerator.printLogic(config, builder, tab);
          builder.append(logicTab).append(tab).append(tab).append("return true;")
              .append(lineSeparator())
              .append(logicTab).append(tab).append('}');
          if (iterator.hasNext()) {
            builder.append(" else ");
          } else {
            builder.append(lineSeparator())
                .append(logicTab).append(tab).append("break;")
                .append(lineSeparator());
            break;
          }
        }
      }
      builder.append(tab).append(tab).append('}').append(lineSeparator());
      if (config.skipUnexpectedFields()) {
        builder.append(tab).append(tab).append("ji.skip();")
            .append(lineSeparator())
            .append(tab).append(tab).append("return true;")
            .append(lineSeparator());
      } else {
        printUnexpected(tab, tab, builder, generator);
      }
      final var parserCode = builder.append(tab).append("};")
          .append(lineSeparator())
          .toString();
      return JIParser.create(parserName, parserCode);
    }
  };


  abstract JIParser createParser(final JIParserConfig config,
                                 final JIParserGenerator generator);


  private static StringBuilder startParser(final String tab, final String parserName) {
    return new StringBuilder(format(
        "%sprivate static final ContextFieldBufferPredicate<?> %s_PARSER = (builder, buf, offset, len, ji) -> {",
        tab, parserName))
        .append(lineSeparator());
  }

  private static String formatParserName(final String fieldName) {
    if (Character.isLowerCase(fieldName.charAt(0))) {
      final var builder = new StringBuilder();
      int tail = 1, head = 0;
      for (final int len = fieldName.length(); tail < len; tail++) {
        if (Character.isUpperCase(fieldName.charAt(tail))) {
          builder.append(fieldName.substring(head, tail).toUpperCase(Locale.ENGLISH)).append('_');
          head = tail;
        }
      }
      return builder.append(fieldName.substring(head, tail).toUpperCase(Locale.ENGLISH)).toString();
    }
    return fieldName.toUpperCase(Locale.ENGLISH);
  }

  private static void printUnexpected(final String depthTab,
                                      final String tab,
                                      final StringBuilder builder,
                                      final JIParserGenerator generator) {
    builder.append(depthTab).append(tab).append("throw new IllegalStateException(String.format(\"Unexpected ")
        .append(generator.getParentNameChain())
        .append(" field '%s'.\", new String(buf, offset, len)));")
        .append(lineSeparator());
  }
}
