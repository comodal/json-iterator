package systems.comodal.jsoniter.generate;

public interface JIParserConfig {

  static JIParserConfig.Builder build() {
    return new JIParserConfigVal.JIParserConfigBuilder();
  }

  boolean skipUnexpectedFields();

  JIParser createParser(final JIParserGenerator generator);

  void printParsers();

  String getTab();

  interface Builder {

    JIParserConfig create();

    Builder skipUnexpectedFields(final boolean skipUnexpectedFields);

    Builder style(final JIParserStyle style);

    Builder tabSize(final int tabSize);
  }
}
