package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestFloat {

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_positive_negative(final String mode) throws IOException {
    // positive
    assertEquals(12.3f, parseFloat(mode, "12.3,"));
    assertEquals(729212.0233f, parseFloat(mode, "729212.0233,"));
    assertEquals(12.3d, parseDouble(mode, "12.3,"));
    assertEquals(729212.0233d, parseDouble(mode, "729212.0233,"));
    // negative
    assertEquals(-12.3f, parseFloat(mode, "-12.3,"));
    assertEquals(-12.3d, parseDouble(mode, "-12.3,"));
  }

  @Test
  void test_long_double() throws IOException {
    assertEquals(4593560419846153055d, JsonIterator.parse("4593560419846153055").readDouble(), 0.1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_ieee_754(final String mode) throws IOException {
    assertEquals(0.00123f, parseFloat(mode, "123e-5,"));
    assertEquals(0.00123d, parseDouble(mode, "123e-5,"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"STREAMING", "BYTE_ARRAY"})
  void test_decimal_places(final String mode) throws IOException {
    assertEquals(Long.MAX_VALUE, parseFloat(mode, "9223372036854775807,"), 0.01f);
    assertEquals(Long.MAX_VALUE, parseDouble(mode, "9223372036854775807,"), 0.01f);
    assertEquals(Long.MIN_VALUE, parseDouble(mode, "-9223372036854775808,"), 0.01f);
    assertEquals(9923372036854775807f, parseFloat(mode, "9923372036854775807,"), 0.01f);
    assertEquals(-9923372036854775808f, parseFloat(mode, "-9923372036854775808,"), 0.01f);
    assertEquals(9923372036854775807d, parseDouble(mode, "9923372036854775807,"), 0.01f);
    assertEquals(-9923372036854775808d, parseDouble(mode, "-9923372036854775808,"), 0.01f);
    assertEquals(720368.54775807f, parseFloat(mode, "720368.54775807,"), 0.01f);
    assertEquals(-720368.54775807f, parseFloat(mode, "-720368.54775807,"), 0.01f);
    assertEquals(720368.54775807d, parseDouble(mode, "720368.54775807,"), 0.01f);
    assertEquals(-720368.54775807d, parseDouble(mode, "-720368.54775807,"), 0.01f);
    assertEquals(72036.854775807f, parseFloat(mode, "72036.854775807,"), 0.01f);
    assertEquals(72036.854775807d, parseDouble(mode, "72036.854775807,"), 0.01f);
    assertEquals(720368.54775807f, parseFloat(mode, "720368.547758075,"), 0.01f);
    assertEquals(720368.54775807d, parseDouble(mode, "720368.547758075,"), 0.01f);
  }

  @Test
  void test_combination_of_dot_and_exponent() throws IOException {
    double v = JsonIterator.parse("8.37377E9").readFloat();
    assertEquals(Double.valueOf("8.37377E9"), v, 1000d);
  }

  private float parseFloat(final String mode, final String input) throws IOException {
    if ("STREAMING".equals(mode)) {
      return JsonIterator.parse(new ByteArrayInputStream(input.getBytes()), 2).readFloat();
    }
    return JsonIterator.parse(input).readFloat();
  }

  private double parseDouble(final String mode, final String input) throws IOException {
    if ("STREAMING".equals(mode)) {
      return JsonIterator.parse(new ByteArrayInputStream(input.getBytes()), 2).readDouble();
    }
    return JsonIterator.parse(input).readDouble();
  }

  @Test
  void testBigDecimal() throws IOException {
    assertEquals(new BigDecimal("100.100"), JsonIterator.parse("100.100").readBigDecimal());
    assertEquals(new BigDecimal("100.1"), JsonIterator.parse("100.1000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("100"), JsonIterator.parse("100.000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1000"), JsonIterator.parse("1000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ONE.movePointRight(10).toPlainString(), JsonIterator.parse("1e10").readBigDecimalStripTrailingZeroes().toPlainString());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse("0000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse("0.000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse("0.0").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse("0.").readBigDecimalStripTrailingZeroes());

    assertEquals(new BigDecimal("100.100"), JsonIterator.parse(`"100.100"`).readBigDecimal());
    assertEquals(new BigDecimal("100.1"), JsonIterator.parse(`"100.1000"`).readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("100"), JsonIterator.parse(`"100.000"`).readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1000"), JsonIterator.parse(`"1000"`).readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ONE.movePointRight(10).toPlainString(), JsonIterator.parse(`"1e10"`).readBigDecimalStripTrailingZeroes().toPlainString());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse(`"0000"`).readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse(`"0.000"`).readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse(`"0.0"`).readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, JsonIterator.parse(`"0."`).readBigDecimalStripTrailingZeroes());
  }

  @Test
  void testInfinity() throws IOException {
    assertEquals(JsonIterator.parse(`"-infinity"`).readDouble(), Double.NEGATIVE_INFINITY);
    assertEquals(JsonIterator.parse(`"-infinity"`).readFloat(), Float.NEGATIVE_INFINITY);
    assertEquals(JsonIterator.parse(`"infinity"`).readDouble(), Double.POSITIVE_INFINITY);
    assertEquals(JsonIterator.parse(`"infinity"`).readFloat(), Float.POSITIVE_INFINITY);
  }
}
