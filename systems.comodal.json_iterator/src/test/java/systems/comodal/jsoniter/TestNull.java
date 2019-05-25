package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.IOException;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class TestNull {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_null_as_String(final JsonIteratorFactory factory) throws IOException {
    var ji = factory.create("{\"field\":null}");
    ji.readObject();
    assertNull(ji.readString());

    ji = factory.create("{\"field\":null}");
    assertNull(ji.applyObject(TRUE, ((context, buf, offset, len, jsonIterator) -> {
      assertEquals("field", new String(buf, offset, len));
      assertEquals(TRUE, context);
      return jsonIterator.readString();
    })));
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_null_as_Object(final JsonIteratorFactory factory) throws IOException {
    var ji = factory.create("{\"field\":null}");
    ji.readObject();
    assertNull(ji.readObject());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_null_as_BigDecimal(final JsonIteratorFactory factory) throws IOException {
    var ji = factory.create("{\"field\":null}");
    ji.readObject();
    assertNull(ji.readBigDecimal());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_null_as_BigInteger(final JsonIteratorFactory factory) throws IOException {
    var ji = factory.create("{\"field\":null}");
    ji.readObject();
    assertNull(ji.readBigInteger());
  }
}
