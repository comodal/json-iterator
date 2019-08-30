package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.math.BigDecimal;
import java.time.Instant;

import static java.math.BigDecimal.ZERO;
import static java.time.Instant.ofEpochSecond;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestFloat {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testStripTrailingZeroes(final JsonIteratorFactory factory) {
    var expected = new BigDecimal("123.456");
    assertEquals(expected, factory.create("123.456").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.4560").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.45600").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.456000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.456000000000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0123.456").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0123.4560").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0123.45600").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0123.456000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0123.456000000000").readBigDecimalStripTrailingZeroes());

    assertEquals(new BigDecimal("123456"), factory.create("123456").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1234560"), factory.create("1234560").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("12345600"), factory.create("12345600").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("123456000"), factory.create("123456000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("123456000000000"), factory.create("123456000000000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("123456"), factory.create("000123456").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1234560"), factory.create("0001234560").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("12345600"), factory.create("00012345600").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("123456000"), factory.create("000123456000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("123456000000000"), factory.create("000123456000000000").readBigDecimalStripTrailingZeroes());

    expected = new BigDecimal("0.123456");
    assertEquals(expected, factory.create("0.123456").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0.1234560").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0.12345600").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0.123456000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("0.123456000000000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("00.123456").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("00.1234560").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("00.12345600").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("00.123456000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("00.123456000000000").readBigDecimalStripTrailingZeroes());

    expected = new BigDecimal("123");
    assertEquals(expected, factory.create("123.").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.0").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.00").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("123.000000000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("000123.0").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("000123.00").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("000123.000").readBigDecimalStripTrailingZeroes());
    assertEquals(expected, factory.create("000123.000000000").readBigDecimalStripTrailingZeroes());

    assertEquals(ZERO, factory.create("0").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.0").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.00").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.000").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.000000000").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("00000").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("00000.").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("00000.0").readBigDecimalStripTrailingZeroes());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testReadMaxDouble(final JsonIteratorFactory factory) {
    var maxDouble = "1.7976931348623157e+308";
    assertEquals(maxDouble, factory.create(maxDouble).readNumberAsString());

    assertEquals(maxDouble, factory.create("\"1.7976931348623157e+308\"").readNumberOrNumberString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testApplyNumberChars(final JsonIteratorFactory factory) {
    final CharBufferFunction<Instant> fractionalEpochParser = (buf, offset, len) -> {
      final int end = offset + len;
      long seconds = 0;
      for (char c; offset < end; ) {
        if ((c = buf[offset++]) == '.') {
          int i = end - offset;
          long nanoAdjustment = 0;
          do {
            nanoAdjustment *= 10;
            nanoAdjustment += Character.digit(buf[offset++], 10);
          } while (offset < end);
          while (i++ < 9) {
            nanoAdjustment *= 10;
          }
          return ofEpochSecond(seconds, nanoAdjustment);
        }
        seconds *= 10;
        seconds += Character.digit(c, 10);
      }
      return ofEpochSecond(seconds);
    };

    var factionalEpoch = "1567130406.123";
    var instant = factory.create(factionalEpoch).applyNumberChars(fractionalEpochParser);
    assertEquals(ofEpochSecond(1567130406L, 123_000_000L), instant);

    factionalEpoch = "1567130406.123456789";
    instant = factory.create(factionalEpoch).applyNumberChars(fractionalEpochParser);
    assertEquals(ofEpochSecond(1567130406L, 123_456_789L), instant);

    factionalEpoch = "1567130406";
    instant = factory.create(factionalEpoch).applyNumberChars(fractionalEpochParser);
    assertEquals(ofEpochSecond(1567130406L), instant);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_positive_negative(final JsonIteratorFactory factory) {
    // positive
    assertEquals(12.3f, factory.create("12.3,").readFloat());
    assertEquals(729212.0233f, factory.create("729212.0233,").readFloat());
    assertEquals(12.3d, factory.create("12.3,").readDouble());
    assertEquals(729212.0233d, factory.create("729212.0233,").readDouble());
    // negative
    assertEquals(-12.3f, factory.create("-12.3,").readFloat());
    assertEquals(-12.3d, factory.create("-12.3,").readDouble());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_long_double(final JsonIteratorFactory factory) {
    assertEquals(4593560419846153055d, factory.create("4593560419846153055").readDouble());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_ieee_754(final JsonIteratorFactory factory) {
    assertEquals(0.00123f, factory.create("123e-5,").readFloat());
    assertEquals(0.00123d, factory.create("123e-5,").readDouble());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_decimal_places(final JsonIteratorFactory factory) {
    assertEquals(Long.MAX_VALUE, factory.create("9223372036854775807,").readFloat());
    assertEquals(Long.MAX_VALUE, factory.create("9223372036854775807,").readDouble());
    assertEquals(Long.MIN_VALUE, factory.create("-9223372036854775808,").readDouble());
    assertEquals(9923372036854775807f, factory.create("9923372036854775807,").readFloat());
    assertEquals(-9923372036854775808f, factory.create("-9923372036854775808,").readFloat());
    assertEquals(9923372036854775807d, factory.create("9923372036854775807,").readDouble());
    assertEquals(-9923372036854775808d, factory.create("-9923372036854775808,").readDouble());
    assertEquals(720368.54775807f, factory.create("720368.54775807,").readFloat());
    assertEquals(-720368.54775807f, factory.create("-720368.54775807,").readFloat());
    assertEquals(720368.54775807d, factory.create("720368.54775807,").readDouble());
    assertEquals(-720368.54775807d, factory.create("-720368.54775807,").readDouble());
    assertEquals(72036.854775807f, factory.create("72036.854775807,").readFloat());
    assertEquals(72036.854775807d, factory.create("72036.854775807,").readDouble());
    assertEquals(720368.547758075f, factory.create("720368.547758075,").readFloat());
    assertEquals(720368.547758075d, factory.create("720368.547758075,").readDouble());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_combination_of_dot_and_exponent(final JsonIteratorFactory factory) {
    assertEquals(Double.parseDouble("8.37377E9"), factory.create("8.37377E9").readDouble());
    assertEquals(Float.parseFloat("8.37377E9"), factory.create("8.37377E9").readFloat());
    assertEquals(Double.parseDouble("8.37377E9"), factory.create("8.37377E9").readFloat(), 1000d);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testReadBigDecimalStripTrailingZeroesCornerCase(final JsonIteratorFactory factory) {
    final var json = "{\"U\":\"2019-02-25T02:57:39.118962Z\",\"f\":\"1\"}";
    final var ji = factory.create(json);
    assertEquals("2019-02-25T02:57:39.118962Z", ji.skipObjField().readString());
    assertEquals(BigDecimal.ONE, ji.skipObjField().readBigDecimalStripTrailingZeroes());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testBigDecimal(final JsonIteratorFactory factory) {
    assertEquals(new BigDecimal("100.100"), factory.create("100.100").readBigDecimal());
    assertEquals(new BigDecimal("100.1"), factory.create("100.1000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("100"), factory.create("100.000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1000"), factory.create("1000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ONE.movePointRight(10).toPlainString(), factory.create("1e10").readBigDecimalStripTrailingZeroes().toPlainString());
    assertEquals(ZERO, factory.create("0000").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.000").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.0").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("0.").readBigDecimalStripTrailingZeroes());

    assertEquals(new BigDecimal("100.100"), factory.create("\"100.100\"").readBigDecimal());
    assertEquals(new BigDecimal("100.1"), factory.create("\"100.1000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("100"), factory.create("\"100.000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1000"), factory.create("\"1000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ONE.movePointRight(10).toPlainString(), factory.create("\"1e10\"").readBigDecimalStripTrailingZeroes().toPlainString());
    assertEquals(ZERO, factory.create("\"0000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("\"0.000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("\"0.0\"").readBigDecimalStripTrailingZeroes());
    assertEquals(ZERO, factory.create("\"0.\"").readBigDecimalStripTrailingZeroes());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testInfinity(final JsonIteratorFactory factory) {
    assertEquals(Double.NEGATIVE_INFINITY, factory.create("\"-Infinity\"").readDouble());
    assertEquals(Float.NEGATIVE_INFINITY, factory.create("\"-Infinity\"").readFloat());
    assertEquals(Double.POSITIVE_INFINITY, factory.create("\"Infinity\"").readDouble());
    assertEquals(Float.POSITIVE_INFINITY, factory.create("\"Infinity\"").readFloat());
    assertEquals(Double.NaN, factory.create("\"NaN\"").readDouble());
    assertEquals(Float.NaN, factory.create("\"NaN\"").readFloat());
  }
}
