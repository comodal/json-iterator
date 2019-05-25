package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class TestArray {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_empty_array(final JsonIteratorFactory factory) throws IOException {
    final var json = "[]";

    var ji = factory.create(json);
    assertFalse(ji.readArray());

    ji = factory.create(json);
    assertEquals(ji, ji.openArray());
    assertEquals(ji, ji.closeArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_one_element(final JsonIteratorFactory factory) throws IOException {
    final var json = "[1]";

    var ji = factory.create(json);
    assertTrue(ji.readArray());
    assertEquals(1, ji.readInt());
    assertFalse(ji.readArray());

    ji = factory.create(json);
    assertEquals(ji, ji.openArray());
    assertEquals(1, ji.readInt());
    assertEquals(ji, ji.closeArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_two_elements(final JsonIteratorFactory factory) throws IOException {
    final var json = " [ 1 , 2 ] ";

    var ji = factory.create(json);
    assertTrue(ji.readArray());
    assertEquals(1, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertFalse(ji.readArray());

    ji = factory.create(json);
    assertEquals(ji, ji.openArray());
    assertEquals(1, ji.readInt());
    assertEquals(ji, ji.continueArray());
    assertEquals(2, ji.readInt());
    assertEquals(ji, ji.closeArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_three_elements(final JsonIteratorFactory factory) throws IOException {
    final var json = " [ 1 , 2, 3 ] ";

    var ji = factory.create(json);
    assertTrue(ji.readArray());
    assertEquals(1, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(3, ji.readInt());
    assertFalse(ji.readArray());

    ji = factory.create(json);
    assertEquals(ji, ji.openArray());
    assertEquals(1, ji.readInt());
    assertEquals(ji, ji.continueArray());
    assertEquals(2, ji.readInt());
    assertEquals(ji, ji.continueArray());
    assertEquals(3, ji.readInt());
    assertEquals(ji, ji.closeArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_four_elements(final JsonIteratorFactory factory) throws IOException {
    final var json = " [ 1 , 2, 3, 4 ] ";
    final var ji = factory.create(json);
    assertTrue(ji.readArray());
    assertEquals(1, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(3, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(4, ji.readInt());
    assertFalse(ji.readArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_five_elements(final JsonIteratorFactory factory) throws IOException {
    final var json = " [ 1 , 2, 3, 4, 5  ] ";
    final var ji = factory.create(json);
    assertTrue(ji.readArray());
    assertEquals(1, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(2, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(3, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(4, ji.readInt());
    assertTrue(ji.readArray());
    assertEquals(5, ji.readInt());
    assertFalse(ji.readArray());
  }
}
