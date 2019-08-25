package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.ValueType;

import static java.lang.System.lineSeparator;
import static systems.comodal.jsoniter.ValueType.ARRAY;
import static systems.comodal.jsoniter.ValueType.OBJECT;

final class JIArrayParserGenerator extends BaseJIParserGenerator {

  private final int numNested;
  private final ValueType arrayType;

  JIArrayParserGenerator(final String parentNameChain,
                         final String parentFieldName,
                         final int numNested,
                         final ValueType arrayType) {
    super(parentNameChain, parentFieldName);
    this.numNested = numNested;
    this.arrayType = arrayType;
  }

  @Override
  public JIParser printLogic(final JIParserConfig config,
                             final StringBuilder builder,
                             final String depthTab) {
    final String tab = config.getTab();
    var nestedTab = depthTab;
    int numArrayLevels = 0;
    do {
      builder.append(nestedTab).append(tab).append(tab).append("while(ji.readArray()) {")
          .append(lineSeparator());
      nestedTab += tab;
    } while (++numArrayLevels <= numNested);
    if (arrayType == OBJECT) {
      final var parser = config.createParser(this);
      builder.append(nestedTab).append(tab).append(tab)
          .append(String.format("ji.testObject(null, %s_PARSER);", parser.getName()))
          .append(lineSeparator());
      closeArray(nestedTab, tab, builder, numArrayLevels);
      return parser;
    } else {
      builder.append(nestedTab).append(tab).append(tab);
      if (arrayType == null) {
        builder.append("ji.skip();");
      } else {
        switch (arrayType) {
          case NUMBER:
            builder.append("ji.readBigDecimalStripTrailingZeroes();");
            break;
          case STRING:
            builder.append("ji.readString();");
            break;
          case BOOLEAN:
            builder.append("ji.readBoolean();");
            break;
          default:
            builder.append("ji.skip();");
        }
      }
      builder.append(lineSeparator());
      closeArray(nestedTab, tab, builder, numArrayLevels);
      return null;
    }
  }

  private static void closeArray(String nestedTab,
                                 final String tab,
                                 final StringBuilder builder,
                                 int numArrayLevels) {
    do {
      nestedTab = nestedTab.substring(0, nestedTab.length() - tab.length());
      builder.append(nestedTab).append(tab).append(tab).append('}')
          .append(lineSeparator());
    } while (--numArrayLevels > 0);
  }

  @Override
  public ValueType getType() {
    return ARRAY;
  }
}
