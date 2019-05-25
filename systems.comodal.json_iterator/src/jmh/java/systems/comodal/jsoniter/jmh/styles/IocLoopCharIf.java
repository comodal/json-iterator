package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import java.io.IOException;

final class IocLoopCharIf implements JsonIterParser<ExchangeInfo> {

  IocLoopCharIf() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) throws IOException {
    return parseExchangeInfo(ji);
  }

  static ExchangeInfo parseExchangeInfo(final JsonIterator ji) throws IOException {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_CHAR_FIELD_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<Filter.Builder> FILTER_CHAR_FIELD_PARSER = (filter, buf, offset, len, ji) -> {
    final char f = buf[offset];
    if (f == 'f') {
      filter.type(ji.readString());
    } else if (f == 'p') {
      filter.minPrice(ji.readBigDecimal());
    } else if (f == 'P') {
      filter.maxPrice(ji.readBigDecimal());
    } else if (f == 't') {
      filter.tickSize(ji.readBigDecimal());
    } else if (f == 'u') {
      filter.multiplierUp(ji.readBigDecimal());
    } else if (f == 'd') {
      filter.multiplierDown(ji.readBigDecimal());
    } else if (f == 'm') {
      filter.avgPriceMins(ji.readInt());
    } else if (f == 'q') {
      filter.minQty(ji.readBigDecimal());
    } else if (f == 'Q') {
      filter.maxQty(ji.readBigDecimal());
    } else if (f == 's') {
      filter.stepSize(ji.readBigDecimal());
    } else if (f == 'n') {
      filter.minNotional(ji.readBigDecimal());
    } else if (f == 'a') {
      filter.applyToMarket(ji.readBoolean());
    } else if (f == 'l') {
      filter.limit(ji.readInt());
    } else if (f == 'A') {
      filter.maxNumAlgoOrders(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled filter field " + f);
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_CHAR_FIELD_PARSER = (symbol, buf, offset, len, ji) -> {
    final char f = buf[offset];
    if (f == 'p') {
      symbol.symbol(ji.readString());
    } else if (f == 's') {
      symbol.status(ji.readString());
    } else if (f == 'b') {
      symbol.baseAsset(ji.readString());
    } else if (f == 'B') {
      symbol.baseAssetPrecision(ji.readInt());
    } else if (f == 'q') {
      symbol.quoteAsset(ji.readString());
    } else if (f == 'Q') {
      symbol.quoteAssetPrecision(ji.readInt());
    } else if (f == 't') {
      while (ji.readArray()) {
        symbol.orderType(ji.readString());
      }
    } else if (f == 'i') {
      symbol.icebergAllowed(ji.readBoolean());
    } else if (f == 'f') {
      while (ji.readArray()) {
        symbol.filter(ji.testObject(Filter.build(), FILTER_CHAR_FIELD_PARSER));
      }
    } else {
      throw new IllegalStateException("Unhandled symbol field " + f);
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_CHAR_FIELD_PARSER = (rateLimit, buf, offset, len, ji) -> {
    final char f = buf[offset];
    if (f == 't') {
      rateLimit.type(ji.readString());
    } else if (f == 'i') {
      rateLimit.intervalUnit(ji.readString());
    } else if (f == 'n') {
      rateLimit.interval(ji.readLong());
    } else if (f == 'l') {
      rateLimit.limit(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled rate limit field " + f);
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_CHAR_FIELD_PARSER = (info, buf, offset, len, ji) -> {
    final char f = buf[offset];
    if (f == 't') {
      info.timezone(ji.readString());
    } else if (f == 's') {
      info.serverTime(ji.readLong());
    } else if (f == 'r') {
      while (ji.readArray()) {
        info.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_CHAR_FIELD_PARSER).create());
      }
    } else if (f == 'e') {
      while (ji.readArray()) {
        ji.skip();
      }
    } else if (f == 'p') {
      while (ji.readArray()) {
        info.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_CHAR_FIELD_PARSER).create());
      }
    } else {
      throw new IllegalStateException("Unhandled field " + f);
    }
    return true;
  };
}
