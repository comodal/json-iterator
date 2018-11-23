package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class TestObject {

  @Test
  void test_empty_object() throws IOException {
    var iter = JsonIterator.parse("{}");
    assertNull(iter.readObject());
  }

  @Test
  void test_one_field() throws IOException {
    var iter = JsonIterator.parse("{ 'field1'\r:\n\t'hello' }".replace('\'', '"'));
    assertEquals("field1", iter.readObject());
    assertEquals("hello", iter.readString());
    assertNull(iter.readObject());

    iter = JsonIterator.parse("{ 'field1'\r:\n\t'hello' }".replace('\'', '"'));
    assertEquals("hello", ((Map) iter.read()).get("field1"));
  }

  @Test
  void test_two_fields() throws IOException {
    var iter = JsonIterator.parse("{ 'field1' : 'hello' , 'field2': 'world' }".replace('\'', '"'));
    assertEquals("field1", iter.readObject());
    assertEquals("hello", iter.readString());
    assertEquals("field2", iter.readObject());
    assertEquals("world", iter.readString());
    assertNull(iter.readObject());
  }

  @Test
  void test_read_null() throws IOException {
    var iter = JsonIterator.parse("null".replace('\'', '"'));
    assertTrue(iter.readNull());
  }
}
