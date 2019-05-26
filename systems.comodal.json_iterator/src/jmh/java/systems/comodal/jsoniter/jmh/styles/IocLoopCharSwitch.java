package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import java.io.IOException;

final class IocLoopCharSwitch implements JsonIterParser<ExchangeInfo> {

  IocLoopCharSwitch() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) throws IOException {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_CHAR_FIELD_PARSER).create();
  }
  
  private static final ContextFieldBufferPredicate<Filter.Builder> FILTER_CHAR_FIELD_PARSER = (filter, buf, offset, len, ji) -> {
    switch (buf[offset]) {
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
        throw new IllegalStateException("Unhandled filter field " + buf[offset]);
    }
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_CHAR_FIELD_PARSER = (symbol, buf, offset, len, ji) -> {
    switch (buf[offset]) {
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
          symbol.filter(ji.testObject(Filter.build(), FILTER_CHAR_FIELD_PARSER));
        }
        return true;
      default:
        throw new IllegalStateException("Unhandled symbol field " + buf[offset]);
    }
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_CHAR_FIELD_PARSER = (rateLimit, buf, offset, len, ji) -> {
    switch (buf[offset]) {
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
        throw new IllegalStateException("Unhandled rate limit field " + buf[offset]);
    }
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_CHAR_FIELD_PARSER = (info, buf, offset, len, ji) -> {
    switch (buf[offset]) {
      case 't':
        info.timezone(ji.readString());
        return true;
      case 's':
        info.serverTime(ji.readLong());
        return true;
      case 'r':
        while (ji.readArray()) {
          info.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_CHAR_FIELD_PARSER).create());
        }
        return true;
      case 'e':
        while (ji.readArray()) {
          ji.skip();
        }
        return true;
      case 'p':
        while (ji.readArray()) {
          info.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_CHAR_FIELD_PARSER).create());
        }
        return true;
      default:
        throw new IllegalStateException("Unhandled field " + buf[offset]);
    }
  };
}
