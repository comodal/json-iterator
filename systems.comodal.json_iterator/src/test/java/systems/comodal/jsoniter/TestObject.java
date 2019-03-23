package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

final class TestObject {

  @Test
  void test_empty_object() throws IOException {
    var iter = JsonIterator.parse("{}");
    assertNull(iter.readObject());
  }

  @Test
  void test_one_field() throws IOException {
    var iter = JsonIterator.parse("{ \"field1\"\n" +
        ":\n" +
        "\t\"hello\" }");
    assertEquals("field1", iter.readObject());
    assertEquals("hello", iter.readString());
    assertNull(iter.readObject());

    iter = JsonIterator.parse("{ \"field1\"\n" +
        ":\n" +
        "\t\"hello\" }");
    assertNull(iter.applyObject(TRUE, ((context, len, buf, jsonIterator) -> {
      assertEquals(TRUE, context);
      assertEquals("field1", new String(buf, 0, len));
      assertEquals("hello", jsonIterator.readString());
      return jsonIterator.applyObject(FALSE, (_context, _len, _buf, _jsonIterator) -> {
        assertEquals(FALSE, _context);
        assertEquals(-1, _len);
        assertNull(_buf);
        return null;
      });
    })));

    iter = JsonIterator.parse("{ \"field1\"\n" +
        ":\n" +
        "\t\"hello\" }");
    assertEquals(iter, iter.skipObjField());
    assertEquals("hello", iter.readString());
    assertNull(iter.skipObjField());
  }

  @Test
  void test_two_fields() throws IOException {
    var iter = JsonIterator.parse("{ \"field1\" : \"hello\" , \"field2\": \"world\" }");
    assertEquals("field1", iter.readObject());
    assertEquals("hello", iter.readString());
    assertEquals("field2", iter.readObject());
    assertEquals("world", iter.readString());
    assertNull(iter.readObject());

    iter.reset(0);
    assertEquals("world", iter.skipUntil("field2").readString());
  }

  @Test
  void test_skip_until() throws IOException {
    var iter = JsonIterator.parse("{ \"field1\" : \"hello\" , \"field2\": {\"nested1\" : \"blah\", \"nested2\": \"world\"} }");
    assertEquals("world", iter.skipUntil("field2").skipUntil("nested2").readString());
  }

  @Test
  void test_read_null() throws IOException {
    var iter = JsonIterator.parse("null");
    assertTrue(iter.readNull());
  }
}
