package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestWhatIsNext {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test(final JsonIteratorFactory factory) {
    assertEquals(ValueType.OBJECT, factory.create("{}").whatIsNext());
    assertEquals(ValueType.STRING, factory.create("\"string\"").whatIsNext());
    assertEquals(ValueType.ARRAY, factory.create("[\"array\"]").whatIsNext());
    assertEquals(ValueType.BOOLEAN, factory.create("t").whatIsNext());
    assertEquals(ValueType.BOOLEAN, factory.create("f").whatIsNext());
    assertEquals(ValueType.NULL, factory.create("n").whatIsNext());
    assertEquals(ValueType.NUMBER, factory.create("-").whatIsNext());
    IntStream.rangeClosed(0, 9)
        .forEach(i -> assertEquals(ValueType.NUMBER, factory.create(Integer.toString(i)).whatIsNext()));
  }
}
