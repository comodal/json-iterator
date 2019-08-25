package systems.comodal.jsoniter.jmh;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import systems.comodal.jsoniter.factory.JsonIterParserFactory;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.styles.BenchCharFieldStyles;
import systems.comodal.jsoniter.jmh.styles.BenchStringFieldStyles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static systems.comodal.jsoniter.jmh.data.exchange.OrderType.*;
import static systems.comodal.jsoniter.jmh.data.exchange.RateLimit.Type.ORDERS;
import static systems.comodal.jsoniter.jmh.data.exchange.RateLimit.Type.REQUEST_WEIGHT;

final class TestExchangeInfoParsers {

  @TestFactory
  Stream<DynamicTest> testStringFieldParsers() {
    final var bench = new BenchStringFieldStyles();
    final var styles = Set.of(
        "StaticFieldOrdering",
        "IocLoopCompareStringFieldToCharsIf",
        "IocLoopCompareStringFieldToCharsIfNHashN",
        "IocLoopCompareStringFieldToCharsIfNLogN",
        "LoopStringSwitch",
        "LoopStringIf"
    );
    return styles.parallelStream()
        .map(filter -> JsonIterParserFactory.loadParser(ExchangeInfo.class, filter))
        .map(parser -> dynamicTest(parser.getClass().getSimpleName(), () -> {
          validateExchangeInfo(parser.parse(bench.getLoadedBytesJsonIterator()));
          validateExchangeInfo(parser.parse(bench.getLoadedCharsJsonIterator()));
          validateExchangeInfo(parser.parse(bench.getLoadedBytesInputStreamJsonIterator(1_024)));
        }));
  }

  @TestFactory
  Stream<DynamicTest> testCharFieldParsers() {
    final var bench = new BenchCharFieldStyles();
    final var styles = Set.of("StaticFieldOrdering", "IocLoopCharSwitch", "IocLoopCharIf");
    return styles.parallelStream()
        .map(filter -> JsonIterParserFactory.loadParser(ExchangeInfo.class, filter))
        .map(parser -> dynamicTest(parser.getClass().getSimpleName(), () -> {
          validateExchangeInfo(parser.parse(bench.getLoadedBytesJsonIterator()));
          validateExchangeInfo(parser.parse(bench.getLoadedCharsJsonIterator()));
          validateExchangeInfo(parser.parse(bench.getLoadedBytesInputStreamJsonIterator(1_024)));
        }));
  }

  private void validateExchangeInfo(final ExchangeInfo info) {
    assertEquals(TimeZone.getTimeZone("UTC"), info.getTimeZone());
    assertEquals(1544179074663L, info.getServerTime());

    final var rateLimits = info.getRateLimits();
    assertEquals(3, info.getRateLimits().size());
    var rateLimit = rateLimits.get(0);
    assertEquals(REQUEST_WEIGHT, rateLimit.getType());
    assertEquals(Duration.ofMinutes(1), rateLimit.getInterval());
    assertEquals(1_200, rateLimit.getLimit());

    rateLimit = rateLimits.get(1);
    assertEquals(ORDERS, rateLimit.getType());
    assertEquals(Duration.ofSeconds(1), rateLimit.getInterval());
    assertEquals(10, rateLimit.getLimit());

    rateLimit = rateLimits.get(2);
    assertEquals(ORDERS, rateLimit.getType());
    assertEquals(Duration.ofDays(1), rateLimit.getInterval());
    assertEquals(100_000, rateLimit.getLimit());

    final var symbols = info.getProductSymbols();
    assertEquals(419, symbols.size());

    var symbol = symbols.get(0);
    assertEquals("ETHBTC", symbol.getSymbol());
    assertEquals("TRADING", symbol.getStatus());
    assertEquals("ETH", symbol.getBaseAsset());
    assertEquals(8, symbol.getBaseAssetPrecision());
    assertEquals("BTC", symbol.getQuoteAsset());
    assertEquals(8, symbol.getQuoteAssetPrecision());
    final var expectedOrderTypes = Set.of(LIMIT, LIMIT_MAKER, MARKET, STOP_LOSS_LIMIT, TAKE_PROFIT_LIMIT);
    assertEquals(expectedOrderTypes, symbol.getOrderTypes());
    assertTrue(symbol.isIcebergAllowed());
    assertEquals(10, symbol.getIcebergPartsLimit());
    assertEquals(5, symbol.getMaxNumAlgoOrders());

    var priceFilter = symbol.getPriceFilter();
    assertEquals(new BigDecimal("0.00000000"), priceFilter.getMinPrice());
    assertEquals(new BigDecimal("0.00000000"), priceFilter.getMaxPrice());
    assertEquals(new BigDecimal("0.00000100"), priceFilter.getTickSize());

    var percentPriceFilter = symbol.getPercentPriceFilter();
    assertEquals(new BigDecimal("10"), percentPriceFilter.getMultiplierUp());
    assertEquals(new BigDecimal("0.1"), percentPriceFilter.getMultiplierDown());
    assertEquals(5, percentPriceFilter.getAvgPriceMins());

    var lotSizeFilter = symbol.getLotSizeFilter();
    assertEquals(new BigDecimal("0.00100000"), lotSizeFilter.getMinQty());
    assertEquals(new BigDecimal("100000.00000000"), lotSizeFilter.getMaxQty());
    assertEquals(new BigDecimal("0.00100000"), lotSizeFilter.getStepSize());

    var minNotionalFilter = symbol.getMinNotionalFilter();
    assertEquals(new BigDecimal("0.00100000"), minNotionalFilter.getMinNotional());
    assertTrue(minNotionalFilter.applyToMarket());
    assertEquals(5, minNotionalFilter.getAvgPriceMins());

    symbol = symbols.get(418);
    assertEquals("XLMTUSD", symbol.getSymbol());
    assertEquals("TRADING", symbol.getStatus());
    assertEquals("XLM", symbol.getBaseAsset());
    assertEquals(8, symbol.getBaseAssetPrecision());
    assertEquals("TUSD", symbol.getQuoteAsset());
    assertEquals(8, symbol.getQuoteAssetPrecision());
    assertEquals(expectedOrderTypes, symbol.getOrderTypes());
    assertTrue(symbol.isIcebergAllowed());
    assertEquals(10, symbol.getIcebergPartsLimit());
    assertEquals(5, symbol.getMaxNumAlgoOrders());

    priceFilter = symbol.getPriceFilter();
    assertEquals(new BigDecimal("0.00001000"), priceFilter.getMinPrice());
    assertEquals(new BigDecimal("10000.00000000"), priceFilter.getMaxPrice());
    assertEquals(new BigDecimal("0.00001000"), priceFilter.getTickSize());

    percentPriceFilter = symbol.getPercentPriceFilter();
    assertEquals(new BigDecimal("10"), percentPriceFilter.getMultiplierUp());
    assertEquals(new BigDecimal("0.1"), percentPriceFilter.getMultiplierDown());
    assertEquals(5, percentPriceFilter.getAvgPriceMins());

    lotSizeFilter = symbol.getLotSizeFilter();
    assertEquals(new BigDecimal("0.10000000"), lotSizeFilter.getMinQty());
    assertEquals(new BigDecimal("90000000.00000000"), lotSizeFilter.getMaxQty());
    assertEquals(new BigDecimal("0.10000000"), lotSizeFilter.getStepSize());

    minNotionalFilter = symbol.getMinNotionalFilter();
    assertEquals(new BigDecimal("1.00000000"), minNotionalFilter.getMinNotional());
    assertTrue(minNotionalFilter.applyToMarket());
    assertEquals(5, minNotionalFilter.getAvgPriceMins());
  }
}
