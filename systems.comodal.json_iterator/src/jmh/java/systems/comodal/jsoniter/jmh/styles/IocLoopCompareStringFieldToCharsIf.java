package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.FieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import java.io.IOException;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

final class IocLoopCompareStringFieldToCharsIf implements JsonIterParser<ExchangeInfo> {

  IocLoopCompareStringFieldToCharsIf() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) throws IOException {
    return parseExchangeInfo(ji);
  }

  static ExchangeInfo parseExchangeInfo(final JsonIterator ji) throws IOException {
    return ji.consumeObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create();
  }

  private static final FieldBufferPredicate<Filter.Builder> FILTER_IF_PARSER = (filter, len, buf, ji) -> {
    if (fieldEquals("filterType", buf, len)) {
      filter.type(ji.readString());
      return true;
    }
    if (fieldEquals("minPrice", buf, len)) {
      filter.minPrice(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("maxPrice", buf, len)) {
      filter.maxPrice(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("tickSize", buf, len)) {
      filter.tickSize(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("multiplierUp", buf, len)) {
      filter.multiplierUp(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("multiplierDown", buf, len)) {
      filter.multiplierDown(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("avgPriceMins", buf, len)) {
      filter.avgPriceMins(ji.readInt());
      return true;
    }
    if (fieldEquals("minQty", buf, len)) {
      filter.minQty(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("maxQty", buf, len)) {
      filter.maxQty(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("stepSize", buf, len)) {
      filter.stepSize(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("minNotional", buf, len)) {
      filter.minNotional(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("applyToMarket", buf, len)) {
      filter.applyToMarket(ji.readBoolean());
      return true;
    }
    if (fieldEquals("limit", buf, len)) {
      filter.limit(ji.readInt());
      return true;
    }
    if (fieldEquals("maxNumAlgoOrders", buf, len)) {
      filter.maxNumAlgoOrders(ji.readInt());
      return true;
    }
    throw new IllegalStateException("Unhandled filter field " + new String(buf, 0, len));
  };

  private static final FieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_IF_PARSER = (symbol, len, buf, ji) -> {
    if (fieldEquals("symbol", buf, len)) {
      symbol.symbol(ji.readString());
      return true;
    }
    if (fieldEquals("status", buf, len)) {
      symbol.status(ji.readString());
      return true;
    }
    if (fieldEquals("baseAsset", buf, len)) {
      symbol.baseAsset(ji.readString());
      return true;
    }
    if (fieldEquals("baseAssetPrecision", buf, len)) {
      symbol.baseAssetPrecision(ji.readInt());
      return true;
    }
    if (fieldEquals("quoteAsset", buf, len)) {
      symbol.quoteAsset(ji.readString());
      return true;
    }
    if (fieldEquals("quotePrecision", buf, len)) {
      symbol.quoteAssetPrecision(ji.readInt());
      return true;
    }
    if (fieldEquals("orderTypes", buf, len)) {
      while (ji.readArray()) {
        symbol.orderType(ji.readString());
      }
      return true;
    }
    if (fieldEquals("icebergAllowed", buf, len)) {
      symbol.icebergAllowed(ji.readBoolean());
      return true;
    }
    if (fieldEquals("filters", buf, len)) {
      while (ji.readArray()) {
        symbol.filter(ji.consumeObject(Filter.build(), FILTER_IF_PARSER));
      }
      return true;
    }
    throw new IllegalStateException("Unhandled symbol field " + new String(buf, 0, len));
  };

  private static final FieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_IF_PARSER = (rateLimit, len, buf, ji) -> {
    if (fieldEquals("rateLimitType", buf, len)) {
      rateLimit.type(ji.readString());
      return true;
    }
    if (fieldEquals("interval", buf, len)) {
      rateLimit.intervalUnit(ji.readString());
      return true;
    }
    if (fieldEquals("intervalNum", buf, len)) {
      rateLimit.interval(ji.readLong());
      return true;
    }
    if (fieldEquals("limit", buf, len)) {
      rateLimit.limit(ji.readInt());
      return true;
    }
    throw new IllegalStateException("Unhandled rate limit field " + new String(buf, 0, len));
  };

  private static final FieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_IF_PARSER = (info, len, buf, ji) -> {
    if (fieldEquals("timezone", buf, len)) {
      info.timezone(ji.readString());
      return true;
    }
    if (fieldEquals("serverTime", buf, len)) {
      info.serverTime(ji.readLong());
      return true;
    }
    if (fieldEquals("rateLimits", buf, len)) {
      while (ji.readArray()) {
        info.rateLimit(ji.consumeObject(RateLimit.build(), RATE_LIMIT_IF_PARSER).create());
      }
      return true;
    }
    if (fieldEquals("exchangeFilters", buf, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
      return true;
    }
    if (fieldEquals("symbols", buf, len)) {
      while (ji.readArray()) {
        info.productSymbol(ji.consumeObject(ProductSymbol.build(), PRODUCT_SYMBOL_IF_PARSER).create());
      }
      return true;
    }
    throw new IllegalStateException("Unhandled field " + new String(buf, 0, len));
  };
}
