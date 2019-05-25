package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestFloat {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testReadMaxDouble(final JsonIteratorFactory factory) throws Exception {
    final var maxDouble = "1.7976931348623157e+308";
    final var ji = factory.create(maxDouble);
    assertEquals(maxDouble, ji.readNumberAsString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_positive_negative(final JsonIteratorFactory factory) throws IOException {
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
  void test_long_double(final JsonIteratorFactory factory) throws IOException {
    assertEquals(4593560419846153055d, factory.create("4593560419846153055").readDouble(), 0.1);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_ieee_754(final JsonIteratorFactory factory) throws IOException {
    assertEquals(0.00123f, factory.create("123e-5,").readFloat());
    assertEquals(0.00123d, factory.create("123e-5,").readDouble());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_decimal_places(final JsonIteratorFactory factory) throws IOException {
    assertEquals(Long.MAX_VALUE, factory.create("9223372036854775807,").readFloat(), 0.01f);
    assertEquals(Long.MAX_VALUE, factory.create("9223372036854775807,").readDouble(), 0.01f);
    assertEquals(Long.MIN_VALUE, factory.create("-9223372036854775808,").readDouble(), 0.01f);
    assertEquals(9923372036854775807f, factory.create("9923372036854775807,").readFloat(), 0.01f);
    assertEquals(-9923372036854775808f, factory.create("-9923372036854775808,").readFloat(), 0.01f);
    assertEquals(9923372036854775807d, factory.create("9923372036854775807,").readDouble(), 0.01f);
    assertEquals(-9923372036854775808d, factory.create("-9923372036854775808,").readDouble(), 0.01f);
    assertEquals(720368.54775807f, factory.create("720368.54775807,").readFloat(), 0.01f);
    assertEquals(-720368.54775807f, factory.create("-720368.54775807,").readFloat(), 0.01f);
    assertEquals(720368.54775807d, factory.create("720368.54775807,").readDouble(), 0.01f);
    assertEquals(-720368.54775807d, factory.create("-720368.54775807,").readDouble(), 0.01f);
    assertEquals(72036.854775807f, factory.create("72036.854775807,").readFloat(), 0.01f);
    assertEquals(72036.854775807d, factory.create("72036.854775807,").readDouble(), 0.01f);
    assertEquals(720368.54775807f, factory.create("720368.547758075,").readFloat(), 0.01f);
    assertEquals(720368.54775807d, factory.create("720368.547758075,").readDouble(), 0.01f);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_combination_of_dot_and_exponent(final JsonIteratorFactory factory) throws IOException {
    assertEquals(Double.valueOf("8.37377E9"), factory.create("8.37377E9").readFloat(), 1000d);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testReadBigDecimalStripTrailingZeroesCornerCase(final JsonIteratorFactory factory) throws IOException {
    final var json = "{\"U\":\"2019-02-25T02:57:39.118962Z\",\"f\":\"1\"}";
    final var ji = factory.create(json);
    assertEquals("2019-02-25T02:57:39.118962Z", ji.skipObjField().readString());
    assertEquals(BigDecimal.ONE, ji.skipObjField().readBigDecimalStripTrailingZeroes());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testBigDecimal(final JsonIteratorFactory factory) throws IOException {
    assertEquals(new BigDecimal("100.100"), factory.create("100.100").readBigDecimal());
    assertEquals(new BigDecimal("100.1"), factory.create("100.1000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("100"), factory.create("100.000").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1000"), factory.create("1000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ONE.movePointRight(10).toPlainString(), factory.create("1e10").readBigDecimalStripTrailingZeroes().toPlainString());
    assertEquals(BigDecimal.ZERO, factory.create("0000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, factory.create("0.000").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, factory.create("0.0").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, factory.create("0.").readBigDecimalStripTrailingZeroes());

    assertEquals(new BigDecimal("100.100"), factory.create("\"100.100\"").readBigDecimal());
    assertEquals(new BigDecimal("100.1"), factory.create("\"100.1000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("100"), factory.create("\"100.000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(new BigDecimal("1000"), factory.create("\"1000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ONE.movePointRight(10).toPlainString(), factory.create("\"1e10\"").readBigDecimalStripTrailingZeroes().toPlainString());
    assertEquals(BigDecimal.ZERO, factory.create("\"0000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, factory.create("\"0.000\"").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, factory.create("\"0.0\"").readBigDecimalStripTrailingZeroes());
    assertEquals(BigDecimal.ZERO, factory.create("\"0.\"").readBigDecimalStripTrailingZeroes());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testInfinity(final JsonIteratorFactory factory) throws IOException {
    assertEquals(Double.NEGATIVE_INFINITY, factory.create("\"-infinity\"").readDouble());
    assertEquals(Float.NEGATIVE_INFINITY, factory.create("\"-infinity\"").readFloat());
    assertEquals(Double.POSITIVE_INFINITY, factory.create("\"infinity\"").readDouble());
    assertEquals(Float.POSITIVE_INFINITY, factory.create("\"infinity\"").readFloat());
  }
}
