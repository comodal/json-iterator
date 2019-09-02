package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferMaskedPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

final class IocLoopCompareStringFieldToCharsIfMask implements JsonIterParser<ExchangeInfo> {

  IocLoopCompareStringFieldToCharsIfMask() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create();
  }

  private static final ContextFieldBufferMaskedPredicate<Filter.Builder> FILTER_IF_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("filterType", buf, offset, len)) {
      builder.type(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("minPrice", buf, offset, len)) {
      builder.minPrice(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("maxPrice", buf, offset, len)) {
      builder.maxPrice(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("tickSize", buf, offset, len)) {
      builder.tickSize(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("multiplierUp", buf, offset, len)) {
      builder.multiplierUp(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("multiplierDown", buf, offset, len)) {
      builder.multiplierDown(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("avgPriceMins", buf, offset, len)) {
      builder.avgPriceMins(ji.readInt());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("minQty", buf, offset, len)) {
      builder.minQty(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("maxQty", buf, offset, len)) {
      builder.maxQty(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("stepSize", buf, offset, len)) {
      builder.stepSize(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("minNotional", buf, offset, len)) {
      builder.minNotional(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("applyToMarket", buf, offset, len)) {
      builder.applyToMarket(ji.readBoolean());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("limit", buf, offset, len)) {
      builder.limit(ji.readInt());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("maxNumAlgoOrders", buf, offset, len)) {
      builder.maxNumAlgoOrders(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled filter field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  private static final ContextFieldBufferMaskedPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_IF_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("symbol", buf, offset, len)) {
      builder.symbol(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("status", buf, offset, len)) {
      builder.status(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("baseAsset", buf, offset, len)) {
      builder.baseAsset(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("baseAssetPrecision", buf, offset, len)) {
      builder.baseAssetPrecision(ji.readInt());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("quoteAsset", buf, offset, len)) {
      builder.quoteAsset(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("quotePrecision", buf, offset, len)) {
      builder.quoteAssetPrecision(ji.readInt());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("orderTypes", buf, offset, len)) {
      while (ji.readArray()) {
        builder.orderType(ji.readString());
      }
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("icebergAllowed", buf, offset, len)) {
      builder.icebergAllowed(ji.readBoolean());
      return mask | 0b10000000;
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("filters", buf, offset, len)) {
      while (ji.readArray()) {
        builder.filter(ji.testObject(Filter.build(), FILTER_IF_PARSER));
      }
    } else {
      throw new IllegalStateException("Unhandled symbol field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  private static final ContextFieldBufferMaskedPredicate<RateLimit.Builder> RATE_LIMIT_IF_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("rateLimitType", buf, offset, len)) {
      builder.type(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("interval", buf, offset, len)) {
      builder.intervalUnit(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("intervalNum", buf, offset, len)) {
      builder.interval(ji.readLong());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("limit", buf, offset, len)) {
      builder.limit(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled rate limit field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  private static final ContextFieldBufferMaskedPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_IF_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("timezone", buf, offset, len)) {
      builder.timezone(ji.readString());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("serverTime", buf, offset, len)) {
      builder.serverTime(ji.readLong());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("rateLimits", buf, offset, len)) {
      while (ji.readArray()) {
        builder.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_IF_PARSER).create());
      }
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("exchangeFilters", buf, offset, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("symbols", buf, offset, len)) {
      while (ji.readArray()) {
        builder.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_IF_PARSER).create());
      }
    } else {
      throw new IllegalStateException("Unhandled field " + new String(buf, offset, len));
    }
    return mask | i;
  };
}
