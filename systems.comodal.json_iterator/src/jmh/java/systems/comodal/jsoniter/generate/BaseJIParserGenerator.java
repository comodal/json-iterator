package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.ValueType;

import java.util.LinkedHashMap;

abstract class BaseJIParserGenerator implements JIParserGenerator {

  private final String parentNameChain;
  private final String parentFieldName;
  private final LinkedHashMap<String, JIParserGenerator> fields;

  BaseJIParserGenerator(final String parentNameChain,
                        final String parentFieldName) {
    this.parentNameChain = parentNameChain;
    this.parentFieldName = parentFieldName;
    this.fields = new LinkedHashMap<>();
  }

  @Override
  public void addValueField(final String fieldName, final ValueType valueType) {
    fields.computeIfAbsent(fieldName, _fieldName -> new JIValueParserGenerator(parentNameChain + '.' + fieldName, fieldName, valueType));
  }

  @Override
  public JIParserGenerator addObjectField(final String fieldName) {
    return fields.computeIfAbsent(fieldName, _fieldName -> new JIObjectParserGenerator(parentNameChain + '.' + fieldName, fieldName));
  }

  @Override
  public JIParserGenerator addArrayField(final String fieldName, final int numNested, final ValueType arrayType) {
    return fields.computeIfAbsent(fieldName, _fieldName -> new JIArrayParserGenerator(parentNameChain + '.' + _fieldName, _fieldName, numNested, arrayType));
  }

  @Override
  public String getParentNameChain() {
    return parentNameChain;
  }

  @Override
  public String getParentName() {
    return parentFieldName;
  }

  @Override
  public LinkedHashMap<String, JIParserGenerator> getFields() {
    return fields;
  }
}
