package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestWhatIsNext {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test(final JsonIteratorFactory factory) throws IOException {
    var ji = factory.create("{}");
    assertEquals(ValueType.OBJECT, ji.whatIsNext());
  }
}
