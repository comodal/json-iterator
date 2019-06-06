package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
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
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<Filter.Builder> FILTER_IF_PARSER = (filter, buf, offset, len, ji) -> {
    if (fieldEquals("filterType", buf, offset, len)) {
      filter.type(ji.readString());
    } else if (fieldEquals("minPrice", buf, offset, len)) {
      filter.minPrice(ji.readBigDecimal());
    } else if (fieldEquals("maxPrice", buf, offset, len)) {
      filter.maxPrice(ji.readBigDecimal());
    } else if (fieldEquals("tickSize", buf, offset, len)) {
      filter.tickSize(ji.readBigDecimal());
    } else if (fieldEquals("multiplierUp", buf, offset, len)) {
      filter.multiplierUp(ji.readBigDecimal());
    } else if (fieldEquals("multiplierDown", buf, offset, len)) {
      filter.multiplierDown(ji.readBigDecimal());
    } else if (fieldEquals("avgPriceMins", buf, offset, len)) {
      filter.avgPriceMins(ji.readInt());
    } else if (fieldEquals("minQty", buf, offset, len)) {
      filter.minQty(ji.readBigDecimal());
    } else if (fieldEquals("maxQty", buf, offset, len)) {
      filter.maxQty(ji.readBigDecimal());
    } else if (fieldEquals("stepSize", buf, offset, len)) {
      filter.stepSize(ji.readBigDecimal());
    } else if (fieldEquals("minNotional", buf, offset, len)) {
      filter.minNotional(ji.readBigDecimal());
    } else if (fieldEquals("applyToMarket", buf, offset, len)) {
      filter.applyToMarket(ji.readBoolean());
    } else if (fieldEquals("limit", buf, offset, len)) {
      filter.limit(ji.readInt());
    } else if (fieldEquals("maxNumAlgoOrders", buf, offset, len)) {
      filter.maxNumAlgoOrders(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled filter field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_IF_PARSER = (symbol, buf, offset, len, ji) -> {
    if (fieldEquals("symbol", buf, offset, len)) {
      symbol.symbol(ji.readString());
    } else if (fieldEquals("status", buf, offset, len)) {
      symbol.status(ji.readString());
    } else if (fieldEquals("baseAsset", buf, offset, len)) {
      symbol.baseAsset(ji.readString());
    } else if (fieldEquals("baseAssetPrecision", buf, offset, len)) {
      symbol.baseAssetPrecision(ji.readInt());
    } else if (fieldEquals("quoteAsset", buf, offset, len)) {
      symbol.quoteAsset(ji.readString());
    } else if (fieldEquals("quotePrecision", buf, offset, len)) {
      symbol.quoteAssetPrecision(ji.readInt());
    } else if (fieldEquals("orderTypes", buf, offset, len)) {
      while (ji.readArray()) {
        symbol.orderType(ji.readString());
      }
    } else if (fieldEquals("icebergAllowed", buf, offset, len)) {
      symbol.icebergAllowed(ji.readBoolean());
    } else if (fieldEquals("filters", buf, offset, len)) {
      while (ji.readArray()) {
        symbol.filter(ji.testObject(Filter.build(), FILTER_IF_PARSER));
      }
    } else {
      throw new IllegalStateException("Unhandled symbol field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_IF_PARSER = (rateLimit, buf, offset, len, ji) -> {
    if (fieldEquals("rateLimitType", buf, offset, len)) {
      rateLimit.type(ji.readString());
    } else if (fieldEquals("interval", buf, offset, len)) {
      rateLimit.intervalUnit(ji.readString());
    } else if (fieldEquals("intervalNum", buf, offset, len)) {
      rateLimit.interval(ji.readLong());
    } else if (fieldEquals("limit", buf, offset, len)) {
      rateLimit.limit(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled rate limit field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_IF_PARSER = (info, buf, offset, len, ji) -> {
    if (fieldEquals("timezone", buf, offset, len)) {
      info.timezone(ji.readString());
    } else if (fieldEquals("serverTime", buf, offset, len)) {
      info.serverTime(ji.readLong());
    } else if (fieldEquals("rateLimits", buf, offset, len)) {
      while (ji.readArray()) {
        info.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_IF_PARSER).create());
      }
    } else if (fieldEquals("exchangeFilters", buf, offset, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
    } else if (fieldEquals("symbols", buf, offset, len)) {
      while (ji.readArray()) {
        info.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_IF_PARSER).create());
      }
    } else {
      throw new IllegalStateException("Unhandled field " + new String(buf, offset, len));
    }
    return true;
  };
}
