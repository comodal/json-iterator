package systems.comodal.jsoniter;

import systems.comodal.jsoniter.factories.ByteArray;
import systems.comodal.jsoniter.factories.ByteArrayInputStream;
import systems.comodal.jsoniter.factories.CharArray;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.util.List;

final class TestFactories {

  private static final List<JsonIteratorFactory> FACTORIES = List.of(ByteArray.INSTANCE, CharArray.INSTANCE, ByteArrayInputStream.INSTANCE);

  static List<JsonIteratorFactory> factories() {
    return FACTORIES;
  }
}
