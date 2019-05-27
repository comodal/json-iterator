package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestInteger {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_positive_negative_int(final JsonIteratorFactory factory) {
    assertEquals(0, factory.create("0").readInt());
    assertEquals(4321, factory.create("4321").readInt());
    assertEquals(54321, factory.create("54321").readInt());
    assertEquals(654321, factory.create("654321").readInt());
    assertEquals(7654321, factory.create("7654321").readInt());
    assertEquals(87654321, factory.create("87654321").readInt());
    assertEquals(987654321, factory.create("987654321").readInt());
    assertEquals(2147483647, factory.create("2147483647").readInt());
    assertEquals(-4321, factory.create("-4321").readInt());
    assertEquals(-2147483648, factory.create("-2147483648").readInt());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_positive_negative_long(final JsonIteratorFactory factory) {
    assertEquals(0L, factory.create("0").readLong());
    assertEquals(4321L, factory.create("4321").readLong());
    assertEquals(54321L, factory.create("54321").readLong());
    assertEquals(654321L, factory.create("654321").readLong());
    assertEquals(7654321L, factory.create("7654321").readLong());
    assertEquals(87654321L, factory.create("87654321").readLong());
    assertEquals(987654321L, factory.create("987654321").readLong());
    assertEquals(9223372036854775807L, factory.create("9223372036854775807").readLong());
    assertEquals(-4321L, factory.create("-4321").readLong());
    assertEquals(-9223372036854775808L, factory.create("-9223372036854775808").readLong());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_max_min_int(final JsonIteratorFactory factory) {
    assertEquals(Integer.MAX_VALUE, factory.create(Integer.toString(Integer.MAX_VALUE)).readInt());
    assertEquals(Integer.MAX_VALUE - 1, factory.create(Integer.toString(Integer.MAX_VALUE - 1)).readInt());
    assertEquals(Integer.MIN_VALUE + 1, factory.create(Integer.toString(Integer.MIN_VALUE + 1)).readInt());
    assertEquals(Integer.MIN_VALUE, factory.create(Integer.toString(Integer.MIN_VALUE)).readInt());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_max_min_long(final JsonIteratorFactory factory) {
    assertEquals(Long.MAX_VALUE, factory.create(Long.toString(Long.MAX_VALUE)).readLong());
    assertEquals(Long.MAX_VALUE - 1, factory.create(Long.toString(Long.MAX_VALUE - 1)).readLong());
    assertEquals(Long.MIN_VALUE + 1, factory.create(Long.toString(Long.MIN_VALUE + 1)).readLong());
    assertEquals(Long.MIN_VALUE, factory.create(Long.toString(Long.MIN_VALUE)).readLong());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_large_number(final JsonIteratorFactory factory) {
    var ji = factory.create("2147483648");
    assertThrows(JsonException.class, ji::readInt);

    for (int i = 300000000; i < 2000000000; i += 10000000) {
      ji = factory.create(i + "0");
      assertThrows(JsonException.class, ji::readInt);

      ji = factory.create(-i + "0");
      assertThrows(JsonException.class, ji::readInt);
    }

    ji = factory.create("9223372036854775808");
    assertThrows(JsonException.class, ji::readLong);

    for (long i = 1000000000000000000L; i < 9000000000000000000L; i += 100000000000000000L) {
      ji = factory.create(i + "0");
      assertThrows(JsonException.class, ji::readLong);

      ji = factory.create(-i + "0");
      assertThrows(JsonException.class, ji::readLong);
    }
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_leading_zero(final JsonIteratorFactory factory) {
    var ji = factory.create("0");
    assertEquals(0, ji.readInt());

    ji = factory.create("0");
    assertEquals(0L, ji.readLong());

    ji = factory.create("01");
    assertThrows(JsonException.class, ji::readInt);

    ji = factory.create("02147483647");
    assertThrows(JsonException.class, ji::readInt);

    ji = factory.create("01");
    assertThrows(JsonException.class, ji::readLong);

    ji = factory.create("09223372036854775807");
    assertThrows(JsonException.class, ji::readLong);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_max_int(final JsonIteratorFactory factory) {
    final var ji = factory.create("[2147483647,-2147483648]");
    ji.readArray();
    assertEquals(Integer.MAX_VALUE, ji.readInt());
    ji.readArray();
    assertEquals(Integer.MIN_VALUE, ji.readInt());
  }
}
