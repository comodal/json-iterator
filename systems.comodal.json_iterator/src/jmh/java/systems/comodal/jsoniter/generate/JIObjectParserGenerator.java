package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.ValueType;

import static java.lang.System.lineSeparator;

final class JIObjectParserGenerator extends BaseJIParserGenerator {

  JIObjectParserGenerator(final String parentNameChain,
                          final String parentFieldName) {
    super(parentNameChain, parentFieldName);
  }

  @Override
  public JIParser printLogic(final JIParserConfig config,
                             final StringBuilder builder,
                             final String depthTab) {
    final String tab = config.getTab();
    final var parser = config.createParser(this);
    builder.append(depthTab).append(tab).append(tab)
        .append(String.format("ji.testObject(null, %s_PARSER);", parser.getName()))
        .append(lineSeparator());
    return parser;
  }

  @Override
  public ValueType getType() {
    return ValueType.OBJECT;
  }
}
