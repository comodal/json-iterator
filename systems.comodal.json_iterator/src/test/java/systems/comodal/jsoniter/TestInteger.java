package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestInteger {

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_positive_negative_int(final String mode) throws IOException {
    assertEquals(0, parseInt(mode, "0"));
    assertEquals(4321, parseInt(mode, "4321"));
    assertEquals(54321, parseInt(mode, "54321"));
    assertEquals(654321, parseInt(mode, "654321"));
    assertEquals(7654321, parseInt(mode, "7654321"));
    assertEquals(87654321, parseInt(mode, "87654321"));
    assertEquals(987654321, parseInt(mode, "987654321"));
    assertEquals(2147483647, parseInt(mode, "2147483647"));
    assertEquals(-4321, parseInt(mode, "-4321"));
    assertEquals(-2147483648, parseInt(mode, "-2147483648"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_positive_negative_long(final String mode) throws IOException {
    assertEquals(0L, parseLong(mode, "0"));
    assertEquals(4321L, parseLong(mode, "4321"));
    assertEquals(54321L, parseLong(mode, "54321"));
    assertEquals(654321L, parseLong(mode, "654321"));
    assertEquals(7654321L, parseLong(mode, "7654321"));
    assertEquals(87654321L, parseLong(mode, "87654321"));
    assertEquals(987654321L, parseLong(mode, "987654321"));
    assertEquals(9223372036854775807L, parseLong(mode, "9223372036854775807"));
    assertEquals(-4321L, parseLong(mode, "-4321"));
    assertEquals(-9223372036854775808L, parseLong(mode, "-9223372036854775808"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_max_min_int(final String mode) throws IOException {
    assertEquals(Integer.MAX_VALUE, parseInt(mode, Integer.toString(Integer.MAX_VALUE)));
    assertEquals(Integer.MAX_VALUE - 1, parseInt(mode, Integer.toString(Integer.MAX_VALUE - 1)));
    assertEquals(Integer.MIN_VALUE + 1, parseInt(mode, Integer.toString(Integer.MIN_VALUE + 1)));
    assertEquals(Integer.MIN_VALUE, parseInt(mode, Integer.toString(Integer.MIN_VALUE)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_max_min_long(final String mode) throws IOException {
    assertEquals(Long.MAX_VALUE, parseLong(mode, Long.toString(Long.MAX_VALUE)));
    assertEquals(Long.MAX_VALUE - 1, parseLong(mode, Long.toString(Long.MAX_VALUE - 1)));
    assertEquals(Long.MIN_VALUE + 1, parseLong(mode, Long.toString(Long.MIN_VALUE + 1)));
    assertEquals(Long.MIN_VALUE, parseLong(mode, Long.toString(Long.MIN_VALUE)));
  }


  private static int parseInt(final String mode, final String input) throws IOException {
    switch (mode) {
      case "STREAMING":
        return JsonIterator.parse(new ByteArrayInputStream(input.getBytes()), 2).readInt();
      default:
        final var iter = (BytesJsonIterator) JsonIterator.parse(input);
        final int v = iter.readInt();
        assertEquals(input.length(), iter.head); // iterator head should point on next non-parsed byte
        return v;
    }
  }

  private static long parseLong(final String mode, final String input) throws IOException {
    switch (mode) {
      case "STREAMING":
        return JsonIterator.parse(new ByteArrayInputStream(input.getBytes()), 2).readLong();
      default:
        final var iter = (BytesJsonIterator) JsonIterator.parse(input);
        long v = iter.readLong();
        assertEquals(input.length(), iter.head); // iterator head should point on next non-parsed byte
        return v;
    }
  }

  @Test
  void test_large_number() {
    var iter = JsonIterator.parse("2147483648");
    assertThrows(JsonException.class, iter::readInt);

    for (int i = 300000000; i < 2000000000; i += 10000000) {
      iter = JsonIterator.parse(i + "0");
      assertThrows(JsonException.class, iter::readInt);

      iter = JsonIterator.parse(-i + "0");
      assertThrows(JsonException.class, iter::readInt);
    }

    iter = JsonIterator.parse("9223372036854775808");
    assertThrows(JsonException.class, iter::readLong);

    for (long i = 1000000000000000000L; i < 9000000000000000000L; i += 100000000000000000L) {
      iter = JsonIterator.parse(i + "0");
      assertThrows(JsonException.class, iter::readLong);

      iter = JsonIterator.parse(-i + "0");
      assertThrows(JsonException.class, iter::readLong);
    }
  }

  @Test
  void test_leading_zero() throws IOException {
    var iter = JsonIterator.parse("0");
    assertEquals(0, iter.readInt());

    iter = JsonIterator.parse("0");
    assertEquals(0L, iter.readLong());

    iter = JsonIterator.parse("01");
    assertThrows(JsonException.class, iter::readInt);

    iter = JsonIterator.parse("02147483647");
    assertThrows(JsonException.class, iter::readInt);

    iter = JsonIterator.parse("01");
    assertThrows(JsonException.class, iter::readLong);

    iter = JsonIterator.parse("09223372036854775807");
    assertThrows(JsonException.class, iter::readLong);
/* FIXME if we should fail on parsing of leading zeroes for other numbers
        try {
            JsonIterator.deserialize("01", double.class);
            fail();
        } catch (JsonException e) {
        }
        try {
            JsonIterator.deserialize("01", float.class);
            fail();
        } catch (JsonException e) {
        }
        try {
            JsonIterator.deserialize("01", BigInteger.class);
            fail();
        } catch (JsonException e) {
        }
        try {
            JsonIterator.deserialize("01", BigDecimal.class);
            fail();
        } catch (JsonException e) {
        }
*/
  }

  @Test
  void test_max_int() throws IOException {
    final var iter = JsonIterator.parse("[2147483647,-2147483648]");
    iter.readArray();
    assertEquals(Integer.MAX_VALUE, iter.readInt());
    iter.readArray();
    assertEquals(Integer.MIN_VALUE, iter.readInt());
  }
}
