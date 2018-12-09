package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class TestArray {

  @Test
  void test_empty_array() throws IOException {
    var iter = JsonIterator.parse("[]");
    assertFalse(iter.readArray());

    iter = JsonIterator.parse("[]");
    assertEquals(iter, iter.openArray());
    assertEquals(iter, iter.closeArray());
  }

  @Test
  void test_one_element() throws IOException {
    var iter = JsonIterator.parse("[1]");
    assertTrue(iter.readArray());
    assertEquals(1, iter.readInt());
    assertFalse(iter.readArray());

    iter = JsonIterator.parse("[1]");
    assertEquals(iter, iter.openArray());
    assertEquals(1, iter.readInt());
    assertEquals(iter, iter.closeArray());
  }

  @Test
  void test_two_elements() throws IOException {
    var iter = JsonIterator.parse(" [ 1 , 2 ] ");
    assertTrue(iter.readArray());
    assertEquals(1, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertFalse(iter.readArray());

    iter = JsonIterator.parse(" [ 1 , 2 ] ");
    assertEquals(iter, iter.openArray());
    assertEquals(1, iter.readInt());
    assertEquals(iter, iter.continueArray());
    assertEquals(2, iter.readInt());
    assertEquals(iter, iter.closeArray());
  }

  @Test
  void test_three_elements() throws IOException {
    var iter = JsonIterator.parse(" [ 1 , 2, 3 ] ");
    assertTrue(iter.readArray());
    assertEquals(1, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(3, iter.readInt());
    assertFalse(iter.readArray());

    iter = JsonIterator.parse(" [ 1 , 2, 3 ] ");
    assertEquals(iter, iter.openArray());
    assertEquals(1, iter.readInt());
    assertEquals(iter, iter.continueArray());
    assertEquals(2, iter.readInt());
    assertEquals(iter, iter.continueArray());
    assertEquals(3, iter.readInt());
    assertEquals(iter, iter.closeArray());
  }

  @Test
  void test_four_elements() throws IOException {
    var iter = JsonIterator.parse(" [ 1 , 2, 3, 4 ] ");
    assertTrue(iter.readArray());
    assertEquals(1, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(3, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(4, iter.readInt());
    assertFalse(iter.readArray());
  }

  @Test
  void test_five_elements() throws IOException {
    var iter = JsonIterator.parse(" [ 1 , 2, 3, 4, 5  ] ");
    assertTrue(iter.readArray());
    assertEquals(1, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(2, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(3, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(4, iter.readInt());
    assertTrue(iter.readArray());
    assertEquals(5, iter.readInt());
    assertFalse(iter.readArray());
  }
}
