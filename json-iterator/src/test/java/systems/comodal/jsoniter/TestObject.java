package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

final class TestObject {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_empty_object(final JsonIteratorFactory factory) {
    var ji = factory.create("{}");
    assertNull(ji.readObject());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_one_field(final JsonIteratorFactory factory) {
    var ji = factory.create("{ \"field1\"\n" +
        ":\n" +
        "\t\"hello\" }");
    assertEquals("field1", ji.readObject());
    assertEquals("hello", ji.readString());
    assertNull(ji.readObject());

    ji = factory.create("{ \"field1\"\n" +
        ":\n" +
        "\t\"hello\" }");
    assertNull(ji.applyObject(TRUE, ((context, buf, offset, len, jsonIterator) -> {
      assertEquals(TRUE, context);
      assertEquals("field1", new String(buf, offset, len));
      assertEquals("hello", jsonIterator.readString());
      return jsonIterator.applyObject(FALSE, (_context, _buf, _offset, _len, _jsonIterator) -> {
        assertEquals(FALSE, _context);
        assertEquals(-1, _len);
        assertNull(_buf);
        return null;
      });
    })));

    ji = factory.create("{ \"field1\"\n" +
        ":\n" +
        "\t\"hello\" }");
    assertEquals(ji, ji.skipObjField());
    assertEquals("hello", ji.readString());
    assertNull(ji.skipObjField());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_two_fields(final JsonIteratorFactory factory) {
    var ji = factory.create("{ \"field1\" : \"hello\" , \"field2\": \"world\" }");
    assertEquals("field1", ji.readObject());
    assertEquals("hello", ji.readString());
    assertEquals("field2", ji.readObject());
    assertEquals("world", ji.readString());
    assertNull(ji.readObject());

    ji = factory.create("{ \"field1\" : \"hello\" , \"field2\": \"world\" }");
    assertEquals("world", ji.skipUntil("field2").readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_skip_until(final JsonIteratorFactory factory) {
    var ji = factory.create("{ \"field1\" : \"hello\" , \"field2\": {\"nested1\" : \"blah\", \"nested2\": \"world\"} }");
    assertEquals("world", ji.skipUntil("field2").skipUntil("nested2").readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_read_null(final JsonIteratorFactory factory) {
    var ji = factory.create("null");
    assertTrue(ji.readNull());
  }
}
