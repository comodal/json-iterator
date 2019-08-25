package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.ValueType;

import java.util.Map;

import static java.lang.System.lineSeparator;

final class JIValueParserGenerator implements JIParserGenerator {

  private final String parentNameChain;
  private final String parentFieldName;
  private final ValueType type;

  JIValueParserGenerator(final String parentNameChain,
                         final String parentFieldName,
                         final ValueType type) {
    this.parentNameChain = parentNameChain;
    this.parentFieldName = parentFieldName;
    this.type = type;
  }

  @Override
  public JIParser printLogic(final JIParserConfig config,
                             final StringBuilder builder,
                             final String depthTab) {
    final String tab = config.getTab();
    builder.append(depthTab).append(tab).append(tab);
    switch (type) {
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
    builder.append(lineSeparator());
    return null;
  }

  @Override
  public void addValueField(final String fieldName, final ValueType valueType) {

  }

  @Override
  public JIParserGenerator addObjectField(final String fieldName) {
    return null;
  }

  @Override
  public JIParserGenerator addArrayField(final String fieldName, final int numNested, final ValueType arrayType) {
    return null;
  }

  @Override
  public String getParentName() {
    return parentFieldName;
  }

  @Override
  public ValueType getType() {
    return type;
  }

  @Override
  public Map<String, JIParserGenerator> getSortedFields() {
    return null;
  }

  @Override
  public String getParentNameChain() {
    return parentNameChain;
  }
}
