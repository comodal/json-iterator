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
  public ExchangeInfo parse(final JsonIterator ji) throws IOException {
    return parseExchangeInfo(ji);
  }

  private static ExchangeInfo parseExchangeInfo(final JsonIterator ji) throws IOException {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<Filter.Builder> FILTER_IF_PARSER = (filter, buf, offset, len, ji) -> {
    if (fieldEquals("filterType", buf, offset, len)) {
      filter.type(ji.readString());
      return true;
    }
    if (fieldEquals("minPrice", buf, offset, len)) {
      filter.minPrice(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("maxPrice", buf, offset, len)) {
      filter.maxPrice(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("tickSize", buf, offset, len)) {
      filter.tickSize(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("multiplierUp", buf, offset, len)) {
      filter.multiplierUp(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("multiplierDown", buf, offset, len)) {
      filter.multiplierDown(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("avgPriceMins", buf, offset, len)) {
      filter.avgPriceMins(ji.readInt());
      return true;
    }
    if (fieldEquals("minQty", buf, offset, len)) {
      filter.minQty(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("maxQty", buf, offset, len)) {
      filter.maxQty(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("stepSize", buf, offset, len)) {
      filter.stepSize(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("minNotional", buf, offset, len)) {
      filter.minNotional(ji.readBigDecimal());
      return true;
    }
    if (fieldEquals("applyToMarket", buf, offset, len)) {
      filter.applyToMarket(ji.readBoolean());
      return true;
    }
    if (fieldEquals("limit", buf, offset, len)) {
      filter.limit(ji.readInt());
      return true;
    }
    if (fieldEquals("maxNumAlgoOrders", buf, offset, len)) {
      filter.maxNumAlgoOrders(ji.readInt());
      return true;
    }
    throw new IllegalStateException("Unhandled filter field " + new String(buf, offset, len));
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_IF_PARSER = (symbol, buf, offset, len, ji) -> {
    if (fieldEquals("symbol", buf, offset, len)) {
      symbol.symbol(ji.readString());
      return true;
    }
    if (fieldEquals("status", buf, offset, len)) {
      symbol.status(ji.readString());
      return true;
    }
    if (fieldEquals("baseAsset", buf, offset, len)) {
      symbol.baseAsset(ji.readString());
      return true;
    }
    if (fieldEquals("baseAssetPrecision", buf, offset, len)) {
      symbol.baseAssetPrecision(ji.readInt());
      return true;
    }
    if (fieldEquals("quoteAsset", buf, offset, len)) {
      symbol.quoteAsset(ji.readString());
      return true;
    }
    if (fieldEquals("quotePrecision", buf, offset, len)) {
      symbol.quoteAssetPrecision(ji.readInt());
      return true;
    }
    if (fieldEquals("orderTypes", buf, offset, len)) {
      while (ji.readArray()) {
        symbol.orderType(ji.readString());
      }
      return true;
    }
    if (fieldEquals("icebergAllowed", buf, offset, len)) {
      symbol.icebergAllowed(ji.readBoolean());
      return true;
    }
    if (fieldEquals("filters", buf, offset, len)) {
      while (ji.readArray()) {
        symbol.filter(ji.testObject(Filter.build(), FILTER_IF_PARSER));
      }
      return true;
    }
    throw new IllegalStateException("Unhandled symbol field " + new String(buf, offset, len));
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_IF_PARSER = (rateLimit, buf, offset, len, ji) -> {
    if (fieldEquals("rateLimitType", buf, offset, len)) {
      rateLimit.type(ji.readString());
      return true;
    }
    if (fieldEquals("interval", buf, offset, len)) {
      rateLimit.intervalUnit(ji.readString());
      return true;
    }
    if (fieldEquals("intervalNum", buf, offset, len)) {
      rateLimit.interval(ji.readLong());
      return true;
    }
    if (fieldEquals("limit", buf, offset, len)) {
      rateLimit.limit(ji.readInt());
      return true;
    }
    throw new IllegalStateException("Unhandled rate limit field " + new String(buf, offset, len));
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_IF_PARSER = (info, buf, offset, len, ji) -> {
    if (fieldEquals("timezone", buf, offset, len)) {
      info.timezone(ji.readString());
      return true;
    }
    if (fieldEquals("serverTime", buf, offset, len)) {
      info.serverTime(ji.readLong());
      return true;
    }
    if (fieldEquals("rateLimits", buf, offset, len)) {
      while (ji.readArray()) {
        info.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_IF_PARSER).create());
      }
      return true;
    }
    if (fieldEquals("exchangeFilters", buf, offset, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
      return true;
    }
    if (fieldEquals("symbols", buf, offset, len)) {
      while (ji.readArray()) {
        info.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_IF_PARSER).create());
      }
      return true;
    }
    throw new IllegalStateException("Unhandled field " + new String(buf, offset, len));
  };
}
