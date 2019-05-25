package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.IOException;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

final class TestDemo {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_iterator_api(final JsonIteratorFactory factory) throws IOException {
    final var json = "[0,1,2,3]";
    final var ji = factory.create(json);
    int total = 0;
    while (ji.readArray()) {
      total += ji.readInt();
    }
    assertEquals(6, total);
  }


  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_iterator(final JsonIteratorFactory factory) throws IOException {
    final var json = "{\"numbers\": [\"1\", \"2\", [\"3\", \"4\"]]}";
    final var ji = factory.create(json);
    assertEquals("numbers", ji.readObject());
    assertTrue(ji.readArray());
    assertEquals("1", ji.readString());
    assertTrue(ji.readArray());
    assertEquals("2", ji.readString());
    assertTrue(ji.readArray());
    assertEquals(ValueType.ARRAY, ji.whatIsNext());
    assertTrue(ji.readArray()); // start inner array
    assertEquals(ValueType.STRING, ji.whatIsNext());
    assertEquals("3", ji.readString());
    assertTrue(ji.readArray());
    assertEquals("4", ji.readString());
    assertFalse(ji.readArray()); // end inner array
    assertFalse(ji.readArray()); // end outer array
    assertNull(ji.readObject()); // end object
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_iterator_apply(final JsonIteratorFactory factory) throws IOException {
    final var json = "{\"numbers\": [\"1\", \"2\", [\"3\", \"4\"]]}";
    final var ji = factory.create(json);
    final var last = ji.applyObject(TRUE, (context, buf, offset, len, _iter) -> {
      assertEquals(TRUE, context);
      assertEquals("numbers", new String(buf, offset, len));
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

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_iterator_consume(final JsonIteratorFactory factory) throws IOException {
    final var ji = factory.create("{\"numbers\": [\"1\", \"2\", [\"3\", \"4\"]]}");
    final var context = ji.testObject(TRUE, (_context, buf, offset, len, _iter) -> {
      assertEquals(TRUE, _context);
      assertEquals("numbers", new String(buf, offset, len));
      assertEquals("1", _iter.openArray().readString());
      assertEquals("2", _iter.continueArray().readString());
      assertEquals(ValueType.ARRAY, _iter.continueArray().whatIsNext());
      assertEquals(ValueType.STRING, _iter.openArray().whatIsNext());
      assertEquals("3", _iter.readString());
      assertEquals("4", ji.continueArray().readString());
      ji.closeArray().closeArray();
      return true;
    });
    assertEquals(TRUE, context);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_readme(final JsonIteratorFactory factory) throws IOException {
    var jsonIterator = factory.create("{\"hello\": \"world\"}");
    var fieldName = jsonIterator.readObject();
    var fieldValue = jsonIterator.readString();
    assertEquals("hello", fieldName);
    assertEquals("world", fieldValue);
    assertEquals("hello world", fieldName + ' ' + fieldValue);
//    System.out.println(fieldName + ' ' + fieldValue);
  }
}
