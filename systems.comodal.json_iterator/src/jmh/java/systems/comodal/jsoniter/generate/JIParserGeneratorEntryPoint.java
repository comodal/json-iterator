package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.ValueType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static systems.comodal.jsoniter.ValueType.ARRAY;
import static systems.comodal.jsoniter.ValueType.OBJECT;

public class JIParserGeneratorEntryPoint {

  private static void printUsage() {
    System.err.println("Usage: ./gradlew gen <style>[ifelse, nlogn, nhashn] <unexpected_field_behavior>[throw, skip] <tab_size>[2, 4, etc.] <camelCaseRootName> <file_or_url>(asdf.json, https://)");
  }

  public static void main(final String[] args) throws IOException {
    if (args.length != 5) {
      printUsage();
      return;
    }
    final var config = JIParserConfig.build()
        .style(JIParserStyle.valueOf(args[0].toUpperCase(Locale.ENGLISH)))
        .skipUnexpectedFields(args[1].equalsIgnoreCase("skip"))
        .tabSize(Integer.parseInt(args[2]))
        .create();
    final var rootName = args[3];
    final var fileOrUri = args[4];
    final JsonIterator ji;
    if (fileOrUri.startsWith("http")) {
//      final var uri = URI.create(fileOrUri);
      return;
    } else {
      ji = JsonIterator.parse(Files.readAllBytes(Paths.get(fileOrUri)));
    }
    generateParser(config, rootName, ji);
  }

  private static void generateParser(final JIParserConfig config,
                                     final String rootName,
                                     final JsonIterator ji) {
    var type = ji.whatIsNext();
    if (type == ARRAY) {
      for (int numNested = 0; ji.readArray(); numNested++) {
        type = ji.whatIsNext();
        if (type == OBJECT) {
          final var rootGenerator = new JIArrayParserGenerator(rootName, rootName, numNested, OBJECT);
          initGenerators(rootGenerator, ji);
          config.createParser(rootGenerator);
          config.printParsers();
          return;
        } else if (type != ARRAY) {
          break;
        }
      }
    } else if (type == OBJECT) {
      final var rootGenerator = new JIObjectParserGenerator(rootName, rootName);
      initGenerators(rootGenerator, ji);
      config.createParser(rootGenerator);
      config.printParsers();
      return;
    }
    System.err.println("Top level must be an object or object array, exiting.");
  }

  private static void initGenerators(final JIParserGenerator generator, final JsonIterator ji) {
    ValueType type, arrayType;
    for (String field; (field = ji.readObjField()) != null; ) {
      if ((type = ji.whatIsNext()) == ARRAY) {
        if (ji.readArray()) {
          for (int numNested = 0; ; numNested++) {
            if ((arrayType = ji.whatIsNext()) == ARRAY) {
              if (ji.readArray()) {
                continue;
              } else {
                generator.addArrayField(field, numNested, null);
              }
            } else if (arrayType == OBJECT) {
              final var arrayElementGenerator = generator.addArrayField(field, numNested, OBJECT);
              do {
                initGenerators(arrayElementGenerator, ji);
              } while (ji.readArray());
              while (numNested-- > 0) {
                ji.skipRestOfArray();
              }
            } else {
              generator.addArrayField(field, numNested, arrayType);
              ji.skip();
              do {
                ji.skipRestOfArray();
              } while (numNested-- > 0);
            }
            break;
          }
        } else {
          generator.addArrayField(field, 0, null);
        }
      } else if (type == OBJECT) {
        initGenerators(generator.addObjectField(field), ji);
      } else {
        generator.addValueField(field, type);
        ji.skip();
      }
    }
  }
}
