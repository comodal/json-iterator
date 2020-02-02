package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

final class IocLoopCharSwitch implements JsonIterParser<ExchangeInfo> {

  IocLoopCharSwitch() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_CHAR_FIELD_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<Filter.Builder> FILTER_CHAR_FIELD_PARSER = (filter, buf, offset, len, ji) -> {
    switch (buf[offset]) {
      case 'f' -> filter.type(ji.readString());
      case 'p' -> filter.minPrice(ji.readBigDecimal());
      case 'P' -> filter.maxPrice(ji.readBigDecimal());
      case 't' -> filter.tickSize(ji.readBigDecimal());
      case 'u' -> filter.multiplierUp(ji.readBigDecimal());
      case 'd' -> filter.multiplierDown(ji.readBigDecimal());
      case 'm' -> filter.avgPriceMins(ji.readInt());
      case 'q' -> filter.minQty(ji.readBigDecimal());
      case 'Q' -> filter.maxQty(ji.readBigDecimal());
      case 's' -> filter.stepSize(ji.readBigDecimal());
      case 'n' -> filter.minNotional(ji.readBigDecimal());
      case 'a' -> filter.applyToMarket(ji.readBoolean());
      case 'l' -> filter.limit(ji.readInt());
      case 'A' -> filter.maxNumAlgoOrders(ji.readInt());
      default -> throw new IllegalStateException("Unhandled filter field " + buf[offset]);
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_CHAR_FIELD_PARSER = (symbol, buf, offset, len, ji) -> {
    switch (buf[offset]) {
      case 'p' -> symbol.symbol(ji.readString());
      case 's' -> symbol.status(ji.readString());
      case 'b' -> symbol.baseAsset(ji.readString());
      case 'B' -> symbol.baseAssetPrecision(ji.readInt());
      case 'q' -> symbol.quoteAsset(ji.readString());
      case 'Q' -> symbol.quoteAssetPrecision(ji.readInt());
      case 't' -> {
        while (ji.readArray()) {
          symbol.orderType(ji.readString());
        }
      }
      case 'i' -> symbol.icebergAllowed(ji.readBoolean());
      case 'f' -> {
        while (ji.readArray()) {
          symbol.filter(ji.testObject(Filter.build(), FILTER_CHAR_FIELD_PARSER));
        }
      }
      default -> throw new IllegalStateException("Unhandled symbol field " + buf[offset]);
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_CHAR_FIELD_PARSER = (rateLimit, buf, offset, len, ji) -> {
    switch (buf[offset]) {
      case 't' -> rateLimit.type(ji.readString());
      case 'i' -> rateLimit.intervalUnit(ji.readString());
      case 'n' -> rateLimit.interval(ji.readLong());
      case 'l' -> rateLimit.limit(ji.readInt());
      default -> throw new IllegalStateException("Unhandled rate limit field " + buf[offset]);
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_CHAR_FIELD_PARSER = (info, buf, offset, len, ji) -> {
    switch (buf[offset]) {
      case 't' -> info.timezone(ji.readString());
      case 's' -> info.serverTime(ji.readLong());
      case 'r' -> {
        while (ji.readArray()) {
          info.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_CHAR_FIELD_PARSER).create());
        }
      }
      case 'e' -> {
        while (ji.readArray()) {
          ji.skip();
        }
      }
      case 'p' -> {
        while (ji.readArray()) {
          info.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_CHAR_FIELD_PARSER).create());
        }
      }
      default -> throw new IllegalStateException("Unhandled field " + buf[offset]);
    }
    return true;
  };
}
