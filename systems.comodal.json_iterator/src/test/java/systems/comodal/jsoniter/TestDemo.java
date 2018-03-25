package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

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
    JsonIterator iter = JsonIterator.parse("{'numbers': ['1', '2', ['3', '4']]}".replace('\'', '"'));
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
  void test_readme() throws IOException {
    JsonIterator jsonIterator = JsonIterator.parse("{\"hello\": \"world\"}");
    String fieldName = jsonIterator.readObject();
    String fieldValue = jsonIterator.readString();
    System.out.println(fieldName + ' ' + fieldValue);
  }
}
