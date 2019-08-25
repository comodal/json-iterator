package systems.comodal.jsoniter.generate;

public interface JIParser {

  static JIParser create(final String name, final String code) {
    return new JIParserVal(name, code);
  }

  String getName();

  String getCode();
}
