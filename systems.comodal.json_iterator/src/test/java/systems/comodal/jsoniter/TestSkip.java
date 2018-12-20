package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class TestSkip {

  @Test
  void test_skip_number() throws IOException {
    var iter = JsonIterator.parse("[1,2]");
    assertTrue(iter.readArray());
    iter.skip();
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertFalse(iter.readArray());
  }

  @Test
  void test_skip_string() throws IOException {
    var iter = JsonIterator.parse("[\"hello\",2]");
    assertTrue(iter.readArray());
    iter.skip();
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertFalse(iter.readArray());
  }

  @Test
  void test_skip_string_streaming() throws IOException {
    var iter = JsonIterator.parse(new ByteArrayInputStream("\"hello".getBytes()), 2);
    assertThrows(JsonException.class, iter::skip);
    iter = JsonIterator.parse(new ByteArrayInputStream("\"hello\"".getBytes()), 2);
    iter.skip();
    iter = JsonIterator.parse(new ByteArrayInputStream("\"hello\"1".getBytes()), 2);
    iter.skip();
    assertEquals(1, iter.readInt());
    iter = JsonIterator.parse(new ByteArrayInputStream("\"h\\\"ello\"1".getBytes()), 3);
    iter.skip();
    assertEquals(1, iter.readInt());
    iter = JsonIterator.parse(new ByteArrayInputStream("\"\\\\\"1".getBytes()), 3);
    iter.skip();
    assertEquals(1, iter.readInt());
  }

  @Test
  void test_skip_object() throws IOException {
    var iter = JsonIterator.parse("[{\"hello\": {\"world\": \"a\"}},2]");
    assertTrue(iter.readArray());
    iter.skip();
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertFalse(iter.readArray());
  }

  @Test
  void test_skip_array() throws IOException {
    var iter = JsonIterator.parse("[ [1,  3] ,2]");
    assertTrue(iter.readArray());
    iter.skip();
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertFalse(iter.readArray());
  }

  @Test
  void test_skip_nested() throws IOException {
    var iter = JsonIterator.parse("[ [1, {\"a\": [\"b\"] },  3] ,2]");
    assertTrue(iter.readArray());
    iter.skip();
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertFalse(iter.readArray());
  }
}
