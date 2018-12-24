package systems.comodal.jsoniter.styles;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import systems.comodal.jsoniter.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class BenchStyles {

  private static final byte[] BENCH_LARGE_JSON;
  private static final byte[] BENCH_LARGE_COMPACT_FIELDS_JSON;

  static {
    try {
      BENCH_LARGE_JSON = new String(BenchStyles.class.getResourceAsStream("/exchangeInfo.json").readAllBytes())
          .replaceAll("\\s+", "").getBytes();
      BENCH_LARGE_COMPACT_FIELDS_JSON = new String(BenchStyles.class.getResourceAsStream("/compactFieldsExchangeInfo.json").readAllBytes())
          .replaceAll("\\s+", "").getBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final ConcurrentLinkedQueue<JsonIterator> JSON_ITERATOR_POOL = new ConcurrentLinkedQueue<>();

  private static JsonIterator createJsonIterator(final byte[] json) {
//    final var jsonIterator = JSON_ITERATOR_POOL.poll();
//    return jsonIterator == null ? JsonIterator.parse(json) : jsonIterator.reset(json);
    return JsonIterator.parse(json);
  }

  private static final FieldBufferPredicate<Filter.Builder> FILTER_CHAR_FIELD_PARSER = (filter, len, buf, ji) -> {
    switch (buf[0]) {
      case 'f':
        filter.type(ji.readString());
        return true;
      case 'p':
        filter.minPrice(ji.readBigDecimal());
        return true;
      case 'P':
        filter.maxPrice(ji.readBigDecimal());
        return true;
      case 't':
        filter.tickSize(ji.readBigDecimal());
        return true;
      case 'u':
        filter.multiplierUp(ji.readBigDecimal());
        return true;
      case 'd':
        filter.multiplierDown(ji.readBigDecimal());
        return true;
      case 'm':
        filter.avgPriceMins(ji.readInt());
        return true;
      case 'q':
        filter.minQty(ji.readBigDecimal());
        return true;
      case 'Q':
        filter.maxQty(ji.readBigDecimal());
        return true;
      case 's':
        filter.stepSize(ji.readBigDecimal());
        return true;
      case 'n':
        filter.minNotional(ji.readBigDecimal());
        return true;
      case 'a':
        filter.applyToMarket(ji.readBoolean());
        return true;
      case 'l':
        filter.limit(ji.readInt());
        return true;
      case 'A':
        filter.maxNumAlgoOrders(ji.readInt());
        return true;
      default:
        throw new IllegalStateException("Unhandled filter field " + buf[0]);
    }
  };

  private static final FieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_CHAR_FIELD_PARSER = (symbol, len, buf, ji) -> {
    switch (buf[0]) {
      case 'p':
        symbol.symbol(ji.readString());
        return true;
      case 's':
        symbol.status(ji.readString());
        return true;
      case 'b':
        symbol.baseAsset(ji.readString());
        return true;
      case 'B':
        symbol.baseAssetPrecision(ji.readInt());
        return true;
      case 'q':
        symbol.quoteAsset(ji.readString());
        return true;
      case 'Q':
        symbol.quoteAssetPrecision(ji.readInt());
        return true;
      case 't':
        while (ji.readArray()) {
          symbol.orderType(ji.readString());
        }
        return true;
      case 'i':
        symbol.icebergAllowed(ji.readBoolean());
        return true;
      case 'f':
        while (ji.readArray()) {
          symbol.filter(ji.consumeObject(Filter.build(), FILTER_CHAR_FIELD_PARSER));
        }
        return true;
      default:
        throw new IllegalStateException("Unhandled symbol field " + buf[0]);
    }
  };

  private static final FieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_CHAR_FIELD_PARSER = (rateLimit, len, buf, ji) -> {
    switch (buf[0]) {
      case 't':
        rateLimit.type(ji.readString());
        return true;
      case 'i':
        rateLimit.intervalUnit(ji.readString());
        return true;
      case 'n':
        rateLimit.interval(ji.readLong());
        return true;
      case 'l':
        rateLimit.limit(ji.readInt());
        return true;
      default:
        throw new IllegalStateException("Unhandled rate limit field " + buf[0]);
    }
  };

  private static final FieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_CHAR_FIELD_PARSER = (info, len, buf, ji) -> {
    switch (buf[0]) {
      case 't':
        info.timezone(ji.readString());
        return true;
      case 's':
        info.serverTime(ji.readLong());
        return true;
      case 'r':
        while (ji.readArray()) {
          info.rateLimit(ji.consumeObject(RateLimit.build(), RATE_LIMIT_CHAR_FIELD_PARSER).create());
        }
        return true;
      case 'e':
        while (ji.readArray()) {
          ji.skip();
        }
        return true;
      case 'p':
        while (ji.readArray()) {
          info.productSymbol(ji.consumeObject(ProductSymbol.build(), PRODUCT_SYMBOL_CHAR_FIELD_PARSER).create());
        }
        return true;
      default:
        throw new IllegalStateException("Unhandled field " + buf[0]);
    }
  };

  private static final FieldBufferPredicate<Filter.Builder> FILTER_IF_PARSER = (filter, len, buf, ji) -> {
    if (equals("filterType", buf, len)) {
      filter.type(ji.readString());
      return true;
    }
    if (equals("minPrice", buf, len)) {
      filter.minPrice(ji.readBigDecimal());
      return true;
    }
    if (equals("maxPrice", buf, len)) {
      filter.maxPrice(ji.readBigDecimal());
      return true;
    }
    if (equals("tickSize", buf, len)) {
      filter.tickSize(ji.readBigDecimal());
      return true;
    }
    if (equals("multiplierUp", buf, len)) {
      filter.multiplierUp(ji.readBigDecimal());
      return true;
    }
    if (equals("multiplierDown", buf, len)) {
      filter.multiplierDown(ji.readBigDecimal());
      return true;
    }
    if (equals("avgPriceMins", buf, len)) {
      filter.avgPriceMins(ji.readInt());
      return true;
    }
    if (equals("minQty", buf, len)) {
      filter.minQty(ji.readBigDecimal());
      return true;
    }
    if (equals("maxQty", buf, len)) {
      filter.maxQty(ji.readBigDecimal());
      return true;
    }
    if (equals("stepSize", buf, len)) {
      filter.stepSize(ji.readBigDecimal());
      return true;
    }
    if (equals("minNotional", buf, len)) {
      filter.minNotional(ji.readBigDecimal());
      return true;
    }
    if (equals("applyToMarket", buf, len)) {
      filter.applyToMarket(ji.readBoolean());
      return true;
    }
    if (equals("limit", buf, len)) {
      filter.limit(ji.readInt());
      return true;
    }
    if (equals("maxNumAlgoOrders", buf, len)) {
      filter.maxNumAlgoOrders(ji.readInt());
      return true;
    }
    throw new IllegalStateException("Unhandled filter field " + new String(buf, 0, len));
  };

  private static final FieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_IF_PARSER = (symbol, len, buf, ji) -> {
    if (equals("symbol", buf, len)) {
      symbol.symbol(ji.readString());
      return true;
    }
    if (equals("status", buf, len)) {
      symbol.status(ji.readString());
      return true;
    }
    if (equals("baseAsset", buf, len)) {
      symbol.baseAsset(ji.readString());
      return true;
    }
    if (equals("baseAssetPrecision", buf, len)) {
      symbol.baseAssetPrecision(ji.readInt());
      return true;
    }
    if (equals("quoteAsset", buf, len)) {
      symbol.quoteAsset(ji.readString());
      return true;
    }
    if (equals("quotePrecision", buf, len)) {
      symbol.quoteAssetPrecision(ji.readInt());
      return true;
    }
    if (equals("orderTypes", buf, len)) {
      while (ji.readArray()) {
        symbol.orderType(ji.readString());
      }
      return true;
    }
    if (equals("icebergAllowed", buf, len)) {
      symbol.icebergAllowed(ji.readBoolean());
      return true;
    }
    if (equals("filters", buf, len)) {
      while (ji.readArray()) {
        symbol.filter(ji.consumeObject(Filter.build(), FILTER_IF_PARSER));
      }
      return true;
    }
    throw new IllegalStateException("Unhandled symbol field " + new String(buf, 0, len));
  };

  private static final FieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_IF_PARSER = (rateLimit, len, buf, ji) -> {
    if (equals("rateLimitType", buf, len)) {
      rateLimit.type(ji.readString());
      return true;
    }
    if (equals("interval", buf, len)) {
      rateLimit.intervalUnit(ji.readString());
      return true;
    }
    if (equals("intervalNum", buf, len)) {
      rateLimit.interval(ji.readLong());
      return true;
    }
    if (equals("limit", buf, len)) {
      rateLimit.limit(ji.readInt());
      return true;
    }
    throw new IllegalStateException("Unhandled rate limit field " + new String(buf, 0, len));
  };

  private static final FieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_IF_PARSER = (info, len, buf, ji) -> {
    if (equals("timezone", buf, len)) {
      info.timezone(ji.readString());
      return true;
    }
    if (equals("serverTime", buf, len)) {
      info.serverTime(ji.readLong());
      return true;
    }
    if (equals("rateLimits", buf, len)) {
      while (ji.readArray()) {
        info.rateLimit(ji.consumeObject(RateLimit.build(), RATE_LIMIT_IF_PARSER).create());
      }
      return true;
    }
    if (equals("exchangeFilters", buf, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
      return true;
    }
    if (equals("symbols", buf, len)) {
      while (ji.readArray()) {
        info.productSymbol(ji.consumeObject(ProductSymbol.build(), PRODUCT_SYMBOL_IF_PARSER).create());
      }
      return true;
    }
    throw new IllegalStateException("Unhandled field " + new String(buf, 0, len));
  };

  static boolean equals(final String str, final char[] buf, final int len) {
    if (str.length() != len) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (str.charAt(i) != buf[i]) {
        return false;
      }
    }
    return true;
  }

  @Benchmark
  @Warmup(iterations = 4, time = 6)
  @Measurement(iterations = 5, time = 7)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void ioc_loop_char_switch(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_COMPACT_FIELDS_JSON);
    try {
      blackhole.consume(ji.consumeObject(ExchangeInfo.build(), EXCHANGE_INFO_CHAR_FIELD_PARSER).create());
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  @Benchmark
  @Warmup(iterations = 4, time = 6)
  @Measurement(iterations = 5, time = 7)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void ioc_loop_string_if(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_JSON);
    try {
      blackhole.consume(ji.consumeObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create());
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  @Benchmark
  @Warmup(iterations = 4, time = 6)
  @Measurement(iterations = 5, time = 7)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void loop_string_if(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_JSON);
    try {
      final var info = ExchangeInfo.build();
      for (var field = ji.readObjField(); field != null; field = ji.readObjField()) {
        if ("timezone".equals(field)) {
          info.timezone(ji.readString());
        } else if ("serverTime".equals(field)) {
          info.serverTime(ji.readLong());
        } else if ("rateLimits".equals(field)) {
          while (ji.readArray()) {
            final var rateLimit = RateLimit.build();
            for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
              if ("rateLimitType".equals(field)) {
                rateLimit.type(ji.readString());
              } else if ("interval".equals(field)) {
                rateLimit.intervalUnit(ji.readString());
              } else if ("intervalNum".equals(field)) {
                rateLimit.interval(ji.readLong());
              } else if ("limit".equals(field)) {
                rateLimit.limit(ji.readInt());
              } else {
                throw new IllegalStateException("Unhandled rate limit field " + field);
              }
            }
            info.rateLimit(rateLimit.create());
          }
        } else if ("exchangeFilters".equals(field)) {
          while (ji.readArray()) {
            ji.skip();
          }
        } else if ("symbols".equals(field)) {
          while (ji.readArray()) {
            final var symbol = ProductSymbol.build();
            for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
              if ("symbol".equals(field)) {
                symbol.symbol(ji.readString());
              } else if ("status".equals(field)) {
                symbol.status(ji.readString());
              } else if ("baseAsset".equals(field)) {
                symbol.baseAsset(ji.readString());
              } else if ("baseAssetPrecision".equals(field)) {
                symbol.baseAssetPrecision(ji.readInt());
              } else if ("quoteAsset".equals(field)) {
                symbol.quoteAsset(ji.readString());
              } else if ("quotePrecision".equals(field)) {
                symbol.quoteAssetPrecision(ji.readInt());
              } else if ("orderTypes".equals(field)) {
                while (ji.readArray()) {
                  symbol.orderType(ji.readString());
                }
              } else if ("icebergAllowed".equals(field)) {
                symbol.icebergAllowed(ji.readBoolean());
              } else if ("filters".equals(field)) {
                while (ji.readArray()) {
                  final var filter = Filter.build();
                  for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                    if ("filterType".equals(field)) {
                      filter.type(ji.readString());
                    } else if ("minPrice".equals(field)) {
                      filter.minPrice(ji.readBigDecimal());
                    } else if ("maxPrice".equals(field)) {
                      filter.maxPrice(ji.readBigDecimal());
                    } else if ("tickSize".equals(field)) {
                      filter.tickSize(ji.readBigDecimal());
                    } else if ("multiplierUp".equals(field)) {
                      filter.multiplierUp(ji.readBigDecimal());
                    } else if ("multiplierDown".equals(field)) {
                      filter.multiplierDown(ji.readBigDecimal());
                    } else if ("avgPriceMins".equals(field)) {
                      filter.avgPriceMins(ji.readInt());
                    } else if ("minQty".equals(field)) {
                      filter.minQty(ji.readBigDecimal());
                    } else if ("maxQty".equals(field)) {
                      filter.maxQty(ji.readBigDecimal());
                    } else if ("stepSize".equals(field)) {
                      filter.stepSize(ji.readBigDecimal());
                    } else if ("minNotional".equals(field)) {
                      filter.minNotional(ji.readBigDecimal());
                    } else if ("applyToMarket".equals(field)) {
                      filter.applyToMarket(ji.readBoolean());
                    } else if ("limit".equals(field)) {
                      filter.limit(ji.readInt());
                    } else if ("maxNumAlgoOrders".equals(field)) {
                      filter.maxNumAlgoOrders(ji.readInt());
                    } else {
                      throw new IllegalStateException("Unhandled filter field " + field);
                    }
                  }
                  symbol.filter(filter);
                }
              } else {
                throw new IllegalStateException("Unhandled symbol field " + field);
              }
            }
            info.productSymbol(symbol.create());
          }
        } else {
          throw new IllegalStateException("Unhandled field " + field);
        }
      }
      blackhole.consume(info.create());
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  @Benchmark
  @Warmup(iterations = 4, time = 6)
  @Measurement(iterations = 5, time = 7)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void loop_string_switch(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_JSON);
    try {
      final var info = ExchangeInfo.build();
      for (var field = ji.readObjField(); field != null; field = ji.readObjField()) {
        switch (field) {
          case "timezone":
            info.timezone(ji.readString());
            continue;
          case "serverTime":
            info.serverTime(ji.readLong());
            continue;
          case "rateLimits":
            while (ji.readArray()) {
              final var rateLimit = RateLimit.build();
              for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                switch (field) {
                  case "rateLimitType":
                    rateLimit.type(ji.readString());
                    continue;
                  case "interval":
                    rateLimit.intervalUnit(ji.readString());
                    continue;
                  case "intervalNum":
                    rateLimit.interval(ji.readLong());
                    continue;
                  case "limit":
                    rateLimit.limit(ji.readInt());
                    continue;
                  default:
                    throw new IllegalStateException("Unhandled rate limit field " + field);
                }
              }
              info.rateLimit(rateLimit.create());
            }
            continue;
          case "exchangeFilters":
            while (ji.readArray()) {
              ji.skip();
            }
            continue;
          case "symbols":
            while (ji.readArray()) {
              final var symbol = ProductSymbol.build();
              for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                switch (field) {
                  case "symbol":
                    symbol.symbol(ji.readString());
                    continue;
                  case "status":
                    symbol.status(ji.readString());
                    continue;
                  case "baseAsset":
                    symbol.baseAsset(ji.readString());
                    continue;
                  case "baseAssetPrecision":
                    symbol.baseAssetPrecision(ji.readInt());
                    continue;
                  case "quoteAsset":
                    symbol.quoteAsset(ji.readString());
                    continue;
                  case "quotePrecision":
                    symbol.quoteAssetPrecision(ji.readInt());
                    continue;
                  case "orderTypes":
                    while (ji.readArray()) {
                      symbol.orderType(ji.readString());
                    }
                    continue;
                  case "icebergAllowed":
                    symbol.icebergAllowed(ji.readBoolean());
                    continue;
                  case "filters":
                    while (ji.readArray()) {
                      final var filter = Filter.build();
                      for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                        switch (field) {
                          case "filterType":
                            filter.type(ji.readString());
                            continue;
                          case "minPrice":
                            filter.minPrice(ji.readBigDecimal());
                            continue;
                          case "maxPrice":
                            filter.maxPrice(ji.readBigDecimal());
                            continue;
                          case "tickSize":
                            filter.tickSize(ji.readBigDecimal());
                            continue;
                          case "multiplierUp":
                            filter.multiplierUp(ji.readBigDecimal());
                            continue;
                          case "multiplierDown":
                            filter.multiplierDown(ji.readBigDecimal());
                            continue;
                          case "avgPriceMins":
                            filter.avgPriceMins(ji.readInt());
                            continue;
                          case "minQty":
                            filter.minQty(ji.readBigDecimal());
                            continue;
                          case "maxQty":
                            filter.maxQty(ji.readBigDecimal());
                            continue;
                          case "stepSize":
                            filter.stepSize(ji.readBigDecimal());
                            continue;
                          case "minNotional":
                            filter.minNotional(ji.readBigDecimal());
                            continue;
                          case "applyToMarket":
                            filter.applyToMarket(ji.readBoolean());
                            continue;
                          case "limit":
                            filter.limit(ji.readInt());
                            continue;
                          case "maxNumAlgoOrders":
                            filter.maxNumAlgoOrders(ji.readInt());
                            continue;
                          default:
                            throw new IllegalStateException("Unhandled filter field " + field);
                        }
                      }
                      symbol.filter(filter);
                    }
                    continue;
                  default:
                    throw new IllegalStateException("Unhandled symbol field " + field);
                }
              }
              info.productSymbol(symbol.create());
            }
            continue;
          default:
            throw new IllegalStateException("Unhandled field " + field);
        }
      }
      blackhole.consume(info.create());
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  @Benchmark
  @Warmup(iterations = 4, time = 6)
  @Measurement(iterations = 5, time = 7)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void static_ordering(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_JSON);
    try {
      blackhole.consume(parseStaticOrdering(ji));
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  @Benchmark
  @Warmup(iterations = 4, time = 6)
  @Measurement(iterations = 5, time = 7)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void static_ordering_char_field(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_COMPACT_FIELDS_JSON);
    try {
      blackhole.consume(parseStaticOrdering(ji));
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  private static ExchangeInfo parseStaticOrdering(final JsonIterator ji) throws IOException {
    final var info = ExchangeInfo.build();
    info.timezone(ji.skipObjField().readString());
    info.serverTime(ji.skipObjField().readLong());
    for (ji.skipObjField(); ji.readArray(); ji.closeObj()) { // rateLimits
      info.rateLimit(RateLimit.build()
          .type(ji.skipObjField().readString())
          .intervalUnit(ji.skipObjField().readString())
          .interval(ji.skipObjField().readLong())
          .limit(ji.skipObjField().readInt())
          .create());
    }
    for (ji.skipObjField(); ji.readArray(); ) { // exchangeFilters
      ji.skip();
    }
    for (ji.skipObjField(); ji.readArray(); ji.closeObj()) { // symbols
      final var symbol = ProductSymbol.build()
          .symbol(ji.skipObjField().readString())
          .status(ji.skipObjField().readString())
          .baseAsset(ji.skipObjField().readString())
          .baseAssetPrecision(ji.skipObjField().readInt())
          .quoteAsset(ji.skipObjField().readString())
          .quoteAssetPrecision(ji.skipObjField().readInt());
      for (ji.skipObjField(); ji.readArray(); ) {
        symbol.orderType(ji.readString());
      }
      symbol.icebergAllowed(ji.skipObjField().readBoolean());

      ji.skipObjField().openArray(); // "filters": [
      ji.skipObjField().skip(); // { "filterType": "PRICE_FILTER",
      symbol.priceFilter(PriceFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "PERCENT_PRICE",
      symbol.percentPriceFilter(PercentPriceFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readInt()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "LOT_SIZE",
      symbol.lotSizeFilter(LotSizeFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "MIN_NOTIONAL",
      symbol.minNotionalFilter(MinNotionalFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBoolean(), ji.skipObjField().readInt()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "ICEBERG_PARTS",
      symbol.icebergPartsLimit(ji.skipObjField().readInt());
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "MAX_NUM_ALGO_ORDERS",
      symbol.maxNumAlgoOrders(ji.skipObjField().readInt());
      ji.closeObj().closeArray(); // } ]
      info.productSymbol(symbol.create());
    }
    return info.create();
  }
}
