package systems.comodal.jsoniter.generate;

import systems.comodal.jsoniter.ValueType;

import java.util.Map;

interface JIParserGenerator {

  String getParentNameChain();

  String getParentName();

  ValueType getType();

  Map<String, JIParserGenerator> getFields();

  JIParser printLogic(final JIParserConfig config,
                      final StringBuilder builder,
                      final String depthTab);

  void addValueField(final String fieldName, final ValueType valueType);

  JIParserGenerator addObjectField(final String fieldName);

  JIParserGenerator addArrayField(final String fieldName, final int numNested, final ValueType arrayType);
}
