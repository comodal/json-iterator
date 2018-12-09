package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

final class TestDemo {

  @Test
  void test_iterator_api() throws IOException {
    JsonIterator iter = JsonIterator.parse("[0,1,2,3]");
    int total = 0;
    while (iter.readArray()) {
      total += iter.readInt();
    }
    assertEquals(6, total);
  }

  @Test
  void test_iterator() throws IOException {
    final var iter = JsonIterator.parse(`{"numbers": ["1", "2", ["3", "4"]]}`);
    assertEquals("numbers", iter.readObject());
    assertTrue(iter.readArray());
    assertEquals("1", iter.readString());
    assertTrue(iter.readArray());
    assertEquals("2", iter.readString());
    assertTrue(iter.readArray());
    assertEquals(ValueType.ARRAY, iter.whatIsNext());
    assertTrue(iter.readArray()); // start inner array
    assertEquals(ValueType.STRING, iter.whatIsNext());
    assertEquals("3", iter.readString());
    assertTrue(iter.readArray());
    assertEquals("4", iter.readString());
    assertFalse(iter.readArray()); // end inner array
    assertFalse(iter.readArray()); // end outer array
    assertNull(iter.readObject()); // end object
  }

  @Test
  void test_iterator2() throws IOException {
    final var iter = JsonIterator.parse(`{"numbers": ["1", "2", ["3", "4"]]}`);
    final var last = iter.applyObjField(TRUE, (context, len, buf, _iter) -> {
      assertEquals(TRUE, context);
      assertEquals("numbers", new String(buf, 0, len));
      assertTrue(_iter.readArray());
      assertEquals("1", _iter.readString());
      assertTrue(_iter.readArray());
      assertEquals("2", _iter.readString());
      assertTrue(_iter.readArray());
      assertEquals(ValueType.ARRAY, _iter.whatIsNext());
      assertTrue(_iter.readArray()); // start inner array
      assertEquals(ValueType.STRING, _iter.whatIsNext());
      assertEquals("3", _iter.readString());
      assertTrue(_iter.readArray());
      final var _last = _iter.readString();
      assertFalse(_iter.readArray()); // end inner array
      assertFalse(_iter.readArray()); // end outer array
      assertNull(_iter.readObject()); // end object
      return _last;
    });
    assertEquals("4", last);
  }

  @Test
  void test_readme() throws IOException {
    var jsonIterator = JsonIterator.parse(`{"hello": "world"}`);
    var fieldName = jsonIterator.readObject();
    var fieldValue = jsonIterator.readString();
    assertEquals("hello", fieldName);
    assertEquals("world", fieldValue);
    System.out.println(fieldName + ' ' + fieldValue);
  }
}
