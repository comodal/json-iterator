package systems.comodal.jsoniter.generate;

final class JIParserVal implements JIParser {

  private final String name;
  private final String code;

  JIParserVal(final String name, final String code) {
    this.name = name;
    this.code = code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getCode() {
    return code;
  }
}
