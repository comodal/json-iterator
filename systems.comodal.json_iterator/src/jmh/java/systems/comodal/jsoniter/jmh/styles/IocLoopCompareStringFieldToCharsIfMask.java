package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.CharBufferFunction;
import systems.comodal.jsoniter.ContextFieldBufferMaskedPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.*;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;
import static systems.comodal.jsoniter.jmh.data.exchange.FilterType.*;

final class IocLoopCompareStringFieldToCharsIfMask implements JsonIterParser<ExchangeInfo> {

  IocLoopCompareStringFieldToCharsIfMask() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create();
  }

  private static final ContextFieldBufferMaskedPredicate<MinNotionalFilter.Builder> MIN_NOTIONAL_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("minNotional", buf, offset, len)) {
      builder.minNotional(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("applyToMarket", buf, offset, len)) {
      builder.applyToMarket(ji.readBoolean());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("avgPriceMins", buf, offset, len)) {
      builder.avgPriceMins(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled MIN_NOTIONAL field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  private static final ContextFieldBufferMaskedPredicate<LotSizeFilter.Builder> LOT_SIZE_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("minQty", buf, offset, len)) {
      builder.minQty(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("maxQty", buf, offset, len)) {
      builder.maxQty(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("stepSize", buf, offset, len)) {
      builder.stepSize(ji.readBigDecimal());
    } else {
      throw new IllegalStateException("Unhandled LOT_SIZE field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  private static final ContextFieldBufferMaskedPredicate<PercentPriceFilter.Builder> PERCENT_PRICE_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("multiplierUp", buf, offset, len)) {
      builder.multiplierUp(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("multiplierDown", buf, offset, len)) {
      builder.multiplierDown(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("avgPriceMins", buf, offset, len)) {
      builder.avgPriceMins(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled PERCENT_PRICE field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  private static final ContextFieldBufferMaskedPredicate<PriceFilter.Builder> PRICE_FILTER_PARSER = (builder, mask, buf, offset, len, ji) -> {
    int i = 1;
    if ((mask & i) == 0 && fieldEquals("minPrice", buf, offset, len)) {
      builder.minPrice(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("maxPrice", buf, offset, len)) {
      builder.maxPrice(ji.readBigDecimal());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("tickSize", buf, offset, len)) {
      builder.tickSize(ji.readBigDecimal());
    } else {
      throw new IllegalStateException("Unhandled PRICE_FILTER field " + new String(buf, offset, len));
    }
    return mask | i;
  };

  static final CharBufferFunction<FilterType> PARSE_FILTER_TYPE = (buf, offset, len) -> {
    if (fieldEquals("PRICE_FILTER", buf, offset, len)) {
      return PRICE_FILTER;
    } else if (fieldEquals("PERCENT_PRICE", buf, offset, len)) {
      return PERCENT_PRICE;
    } else if (fieldEquals("LOT_SIZE", buf, offset, len)) {
      return LOT_SIZE;
    } else if (fieldEquals("MIN_NOTIONAL", buf, offset, len)) {
      return MIN_NOTIONAL;
    } else if (fieldEquals("ICEBERG_PARTS", buf, offset, len)) {
      return ICEBERG_PARTS;
    } else if (fieldEquals("MARKET_LOT_SIZE", buf, offset, len)) {
      return MARKET_LOT_SIZE;
    } else if (fieldEquals("MAX_NUM_ALGO_ORDERS", buf, offset, len)) {
      return MAX_NUM_ALGO_ORDERS;
    } else {
      throw new IllegalStateException("Unhandled filter type " + new String(buf, offset, len));
    }
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
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("ocoAllowed", buf, offset, len)) {
      builder.ocoAllowed(ji.readBoolean());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("isSpotTradingAllowed", buf, offset, len)) {
      builder.isSpotTradingAllowed(ji.readBoolean());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("isMarginTradingAllowed", buf, offset, len)) {
      builder.isMarginTradingAllowed(ji.readBoolean());
    } else if ((mask & (i <<= 1)) == 0 && fieldEquals("filters", buf, offset, len)) {
      while (ji.readArray()) {
        switch (ji.skipObjField().applyChars(PARSE_FILTER_TYPE)) {
          case PRICE_FILTER:
            builder.priceFilter(ji.testObject(PriceFilter.build(), PRICE_FILTER_PARSER).create());
            continue;
          case PERCENT_PRICE:
            builder.percentPriceFilter(ji.testObject(PercentPriceFilter.build(), PERCENT_PRICE_PARSER).create());
            continue;
          case MAX_NUM_ALGO_ORDERS:
            builder.maxNumAlgoOrders(ji.skipUntil("maxNumAlgoOrders").readInt());
            ji.closeObj();
            continue;
          case LOT_SIZE:
            builder.lotSizeFilter(ji.testObject(LotSizeFilter.build(), LOT_SIZE_PARSER).create());
            continue;
          case MIN_NOTIONAL:
            builder.minNotionalFilter(ji.testObject(MinNotionalFilter.build(), MIN_NOTIONAL_PARSER).create());
            continue;
          case ICEBERG_PARTS:
            builder.icebergPartsLimit(ji.skipUntil("limit").readInt());
            ji.closeObj();
            continue;
          case MARKET_LOT_SIZE:
            builder.marketLotSizeFilter(ji.testObject(LotSizeFilter.build(), LOT_SIZE_PARSER).create());
        }
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
