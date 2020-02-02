package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.*;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;
import static systems.comodal.jsoniter.jmh.styles.IocLoopCompareStringFieldToCharsIfMask.PARSE_FILTER_TYPE;

final class IocLoopCompareStringFieldToCharsIf implements JsonIterParser<ExchangeInfo> {

  IocLoopCompareStringFieldToCharsIf() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_IF_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<MinNotionalFilter.Builder> MIN_NOTIONAL_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("minNotional", buf, offset, len)) {
      builder.minNotional(ji.readBigDecimal());
    } else if (fieldEquals("applyToMarket", buf, offset, len)) {
      builder.applyToMarket(ji.readBoolean());
    } else if (fieldEquals("avgPriceMins", buf, offset, len)) {
      builder.avgPriceMins(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled MIN_NOTIONAL field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<LotSizeFilter.Builder> LOT_SIZE_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("minQty", buf, offset, len)) {
      builder.minQty(ji.readBigDecimal());
    } else if (fieldEquals("maxQty", buf, offset, len)) {
      builder.maxQty(ji.readBigDecimal());
    } else if (fieldEquals("stepSize", buf, offset, len)) {
      builder.stepSize(ji.readBigDecimal());
    } else {
      throw new IllegalStateException("Unhandled LOT_SIZE field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<PercentPriceFilter.Builder> PERCENT_PRICE_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("multiplierUp", buf, offset, len)) {
      builder.multiplierUp(ji.readBigDecimal());
    } else if (fieldEquals("multiplierDown", buf, offset, len)) {
      builder.multiplierDown(ji.readBigDecimal());
    } else if (fieldEquals("avgPriceMins", buf, offset, len)) {
      builder.avgPriceMins(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled PERCENT_PRICE field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<PriceFilter.Builder> PRICE_FILTER_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("minPrice", buf, offset, len)) {
      builder.minPrice(ji.readBigDecimal());
    } else if (fieldEquals("maxPrice", buf, offset, len)) {
      builder.maxPrice(ji.readBigDecimal());
    } else if (fieldEquals("tickSize", buf, offset, len)) {
      builder.tickSize(ji.readBigDecimal());
    } else {
      throw new IllegalStateException("Unhandled PRICE_FILTER field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> PRODUCT_SYMBOL_IF_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("symbol", buf, offset, len)) {
      builder.symbol(ji.readString());
    } else if (fieldEquals("status", buf, offset, len)) {
      builder.status(ji.readString());
    } else if (fieldEquals("baseAsset", buf, offset, len)) {
      builder.baseAsset(ji.readString());
    } else if (fieldEquals("baseAssetPrecision", buf, offset, len)) {
      builder.baseAssetPrecision(ji.readInt());
    } else if (fieldEquals("quoteAsset", buf, offset, len)) {
      builder.quoteAsset(ji.readString());
    } else if (fieldEquals("quotePrecision", buf, offset, len)) {
      builder.quoteAssetPrecision(ji.readInt());
    } else if (fieldEquals("orderTypes", buf, offset, len)) {
      while (ji.readArray()) {
        builder.orderType(ji.readString());
      }
    } else if (fieldEquals("icebergAllowed", buf, offset, len)) {
      builder.icebergAllowed(ji.readBoolean());
    } else if (fieldEquals("ocoAllowed", buf, offset, len)) {
      builder.ocoAllowed(ji.readBoolean());
    } else if (fieldEquals("isSpotTradingAllowed", buf, offset, len)) {
      builder.isSpotTradingAllowed(ji.readBoolean());
    } else if (fieldEquals("isMarginTradingAllowed", buf, offset, len)) {
      builder.isMarginTradingAllowed(ji.readBoolean());
    } else if (fieldEquals("filters", buf, offset, len)) {
      while (ji.readArray()) {
        switch (ji.skipObjField().applyChars(PARSE_FILTER_TYPE)) {
          case PRICE_FILTER -> builder.priceFilter(ji.testObject(PriceFilter.build(), PRICE_FILTER_PARSER).create());
          case PERCENT_PRICE -> builder.percentPriceFilter(ji.testObject(PercentPriceFilter.build(), PERCENT_PRICE_PARSER).create());
          case MAX_NUM_ALGO_ORDERS -> {
            builder.maxNumAlgoOrders(ji.skipUntil("maxNumAlgoOrders").readInt());
            ji.closeObj();
          }
          case LOT_SIZE -> builder.lotSizeFilter(ji.testObject(LotSizeFilter.build(), LOT_SIZE_PARSER).create());
          case MIN_NOTIONAL -> builder.minNotionalFilter(ji.testObject(MinNotionalFilter.build(), MIN_NOTIONAL_PARSER).create());
          case ICEBERG_PARTS -> {
            builder.icebergPartsLimit(ji.skipUntil("limit").readInt());
            ji.closeObj();
          }
          case MARKET_LOT_SIZE -> builder.marketLotSizeFilter(ji.testObject(LotSizeFilter.build(), LOT_SIZE_PARSER).create());
        }
      }
    } else {
      throw new IllegalStateException("Unhandled symbol field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> RATE_LIMIT_IF_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("rateLimitType", buf, offset, len)) {
      builder.type(ji.readString());
    } else if (fieldEquals("interval", buf, offset, len)) {
      builder.intervalUnit(ji.readString());
    } else if (fieldEquals("intervalNum", buf, offset, len)) {
      builder.interval(ji.readLong());
    } else if (fieldEquals("limit", buf, offset, len)) {
      builder.limit(ji.readInt());
    } else {
      throw new IllegalStateException("Unhandled rate limit field " + new String(buf, offset, len));
    }
    return true;
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_IF_PARSER = (builder, buf, offset, len, ji) -> {
    if (fieldEquals("timezone", buf, offset, len)) {
      builder.timezone(ji.readString());
    } else if (fieldEquals("serverTime", buf, offset, len)) {
      builder.serverTime(ji.readLong());
    } else if (fieldEquals("rateLimits", buf, offset, len)) {
      while (ji.readArray()) {
        builder.rateLimit(ji.testObject(RateLimit.build(), RATE_LIMIT_IF_PARSER).create());
      }
    } else if (fieldEquals("exchangeFilters", buf, offset, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
    } else if (fieldEquals("symbols", buf, offset, len)) {
      while (ji.readArray()) {
        builder.productSymbol(ji.testObject(ProductSymbol.build(), PRODUCT_SYMBOL_IF_PARSER).create());
      }
    } else {
      throw new IllegalStateException("Unhandled field " + new String(buf, offset, len));
    }
    return true;
  };
}
