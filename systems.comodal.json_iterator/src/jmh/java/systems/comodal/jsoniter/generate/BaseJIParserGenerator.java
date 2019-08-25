package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.ValueType;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

abstract class BaseJIParserGenerator implements JIParserGenerator {

  private final String parentNameChain;
  private final String parentFieldName;
  private final Map<String, JIParserGenerator> sortedFields;

  BaseJIParserGenerator(final String parentNameChain,
                        final String parentFieldName) {
    this.parentNameChain = parentNameChain;
    this.parentFieldName = parentFieldName;
    this.sortedFields = new TreeMap<>(Comparator.comparing(String::length).thenComparing(String::compareTo));
  }

  @Override
  public void addValueField(final String fieldName, final ValueType valueType) {
    sortedFields.computeIfAbsent(fieldName, _fieldName -> new JIValueParserGenerator(parentNameChain + '.' + fieldName, fieldName, valueType));
  }

  @Override
  public JIParserGenerator addObjectField(final String fieldName) {
    return sortedFields.computeIfAbsent(fieldName, _fieldName -> new JIObjectParserGenerator(parentNameChain + '.' + fieldName, fieldName));
  }

  @Override
  public JIParserGenerator addArrayField(final String fieldName, final int numNested, final ValueType arrayType) {
    return sortedFields.computeIfAbsent(fieldName, _fieldName -> new JIArrayParserGenerator(parentNameChain + '.' + _fieldName, _fieldName, numNested, arrayType));
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
  public Map<String, JIParserGenerator> getSortedFields() {
    return sortedFields;
  }
}
