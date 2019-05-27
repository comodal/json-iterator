package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import static org.junit.jupiter.api.Assertions.*;

final class TestSkip {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_number(final JsonIteratorFactory factory) {
    var ji = factory.create("[1,2]");
    assertTrue(ji.readArray());
    ji.skip();
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertFalse(ji.readArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_string(final JsonIteratorFactory factory) {
    var ji = factory.create("[\"hello\",2]");
    assertTrue(ji.readArray());
    ji.skip();
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertFalse(ji.readArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_string_streaming(final JsonIteratorFactory factory) {
    var ji = factory.create("\"hello", 2, 2);
    assertThrows(JsonException.class, ji::skip);

    ji = factory.create("\"hello\"", 2, 2);
    ji.skip();

    ji = factory.create("\"hello\"1", 2, 2);
    ji.skip();
    assertEquals(1, ji.readInt());

    ji = factory.create("\"h\\\"ello\"1", 2, 3);
    ji.skip();
    assertEquals(1, ji.readInt());

    ji = factory.create("\"\\\\\"1", 2, 3);
    ji.skip();
    assertEquals(1, ji.readInt());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_object(final JsonIteratorFactory factory) {
    var ji = factory.create("[{\"hello\": {\"world\": \"a\"}},2]");
    assertTrue(ji.readArray());
    ji.skip();
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertFalse(ji.readArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_array(final JsonIteratorFactory factory) {
    var ji = factory.create("[ [1,  3] ,2]");
    assertTrue(ji.readArray());
    ji.skip();
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertFalse(ji.readArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_nested(final JsonIteratorFactory factory) {
    var ji = factory.create("[ [1, {\"a\": [\"b\"] },  3] ,2]");
    assertTrue(ji.readArray());
    ji.skip();
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertFalse(ji.readArray());
  }
}
