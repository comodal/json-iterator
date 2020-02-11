package systems.comodal.jsoniter;

import systems.comodal.jsoniter.factories.ByteArray;
import systems.comodal.jsoniter.factories.ByteArrayInputStream;
import systems.comodal.jsoniter.factories.CharArray;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.util.List;

final class TestFactories {

  private static final List<JsonIteratorFactory> FACTORIES = List.of(ByteArray.INSTANCE, CharArray.INSTANCE, ByteArrayInputStream.INSTANCE);
  private static final List<JsonIteratorFactory> MARK_FACTORIES = List.of(ByteArray.INSTANCE, CharArray.INSTANCE);

  static List<JsonIteratorFactory> factories() {
    return FACTORIES;
  }

  static List<JsonIteratorFactory> markableFactories() {
    return MARK_FACTORIES;
  }
}
