package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import static systems.comodal.jsoniter.JIUtil.fieldCompare;
import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

final class IocLoopCompareStringFieldToCharsIfNLogN implements JsonIterParser<ExchangeInfo> {

  IocLoopCompareStringFieldToCharsIfNLogN() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<RateLimit.Builder> EXCHANGE_INFO_RATE_LIMITS_PARSER = (builder, buf, offset, len, ji) -> {
    int c;
    if ((c = fieldCompare("intervalNum", buf, offset, len)) == 0) {
      builder.interval(ji.readLong());
      return true;
    } else if (c < 0) {
      if ((c = fieldCompare("interval", buf, offset, len)) == 0) {
        builder.intervalUnit(ji.readString());
        return true;
      } else if (c < 0) {
        if (fieldEquals("limit", buf, offset, len)) {
          builder.limit(ji.readInt());
          return true;
        }
      }
    } else if (fieldEquals("rateLimitType", buf, offset, len)) {
      builder.type(ji.readString());
      return true;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo.rateLimits field '%s'.", new String(buf, offset, len)));
  };

  private static final ContextFieldBufferPredicate<Filter.Builder> EXCHANGE_INFO_SYMBOLS_FILTERS_PARSER = (builder, buf, offset, len, ji) -> {
    int c;
    if ((c = fieldCompare("filterType", buf, offset, len)) == 0) {
      builder.type(ji.readString());
      return true;
    } else if (c < 0) {
      if ((c = fieldCompare("maxPrice", buf, offset, len)) == 0) {
        builder.maxPrice(ji.readBigDecimal());
        return true;
      } else if (c < 0) {
        if ((c = fieldCompare("maxQty", buf, offset, len)) == 0) {
          builder.maxQty(ji.readBigDecimal());
          return true;
        } else if (c < 0) {
          if (fieldEquals("limit", buf, offset, len)) {
            builder.limit(ji.readInt());
            return true;
          }
        } else if (fieldEquals("minQty", buf, offset, len)) {
          builder.minQty(ji.readBigDecimal());
          return true;
        }
      } else if ((c = fieldCompare("stepSize", buf, offset, len)) == 0) {
        builder.stepSize(ji.readBigDecimal());
        return true;
      } else if (c < 0) {
        if (fieldEquals("minPrice", buf, offset, len)) {
          builder.minPrice(ji.readBigDecimal());
          return true;
        }
      } else if (fieldEquals("tickSize", buf, offset, len)) {
        builder.tickSize(ji.readBigDecimal());
        return true;
      }
    } else if ((c = fieldCompare("multiplierUp", buf, offset, len)) == 0) {
      builder.multiplierUp(ji.readBigDecimal());
      return true;
    } else if (c < 0) {
      if ((c = fieldCompare("avgPriceMins", buf, offset, len)) == 0) {
        builder.avgPriceMins(ji.readInt());
        return true;
      } else if (c < 0) {
        if (fieldEquals("minNotional", buf, offset, len)) {
          builder.minNotional(ji.readBigDecimal());
          return true;
        }
      }
    } else if ((c = fieldCompare("multiplierDown", buf, offset, len)) == 0) {
      builder.multiplierDown(ji.readBigDecimal());
      return true;
    } else if (c < 0) {
      if (fieldEquals("applyToMarket", buf, offset, len)) {
        builder.applyToMarket(ji.readBoolean());
        return true;
      }
    } else if (fieldEquals("maxNumAlgoOrders", buf, offset, len)) {
      builder.maxNumAlgoOrders(ji.readInt());
      return true;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo.symbols.filters field '%s'.", new String(buf, offset, len)));
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> EXCHANGE_INFO_SYMBOLS_PARSER = (builder, buf, offset, len, ji) -> {
    int c;
    if ((c = fieldCompare("quoteAsset", buf, offset, len)) == 0) {
      builder.quoteAsset(ji.readString());
      return true;
    } else if (c < 0) {
      if ((c = fieldCompare("baseAsset", buf, offset, len)) == 0) {
        builder.baseAsset(ji.readString());
        return true;
      } else if (c < 0) {
        if ((c = fieldCompare("symbol", buf, offset, len)) == 0) {
          builder.symbol(ji.readString());
          return true;
        } else if (c < 0) {
          if (fieldEquals("status", buf, offset, len)) {
            builder.status(ji.readString());
            return true;
          }
        } else if (fieldEquals("filters", buf, offset, len)) {
          while (ji.readArray()) {
            builder.filter(ji.testObject(Filter.build(), EXCHANGE_INFO_SYMBOLS_FILTERS_PARSER));
          }
          return true;
        }
      } else if ((c = fieldCompare("ocoAllowed", buf, offset, len)) == 0) {
        // TODO
        ji.skip();
        return true;
      } else if (c > 0 && fieldEquals("orderTypes", buf, offset, len)) {
        while (ji.readArray()) {
          builder.orderType(ji.readString());
        }
        return true;
      }
    } else if ((c = fieldCompare("baseAssetPrecision", buf, offset, len)) == 0) {
      builder.baseAssetPrecision(ji.readInt());
      return true;
    } else if (c < 0) {
      if ((c = fieldCompare("quotePrecision", buf, offset, len)) == 0) {
        builder.quoteAssetPrecision(ji.readInt());
        return true;
      } else if (c < 0) {
        if (fieldEquals("icebergAllowed", buf, offset, len)) {
          builder.icebergAllowed(ji.readBoolean());
          return true;
        }
      }
    } else if ((c = fieldCompare("isSpotTradingAllowed", buf, offset, len)) == 0) {
      // TODO
      ji.skip();
      return true;
    } else if (c > 0 && fieldEquals("isMarginTradingAllowed", buf, offset, len)) {
      // TODO
      ji.skip();
      return true;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo.symbols field '%s'.", new String(buf, offset, len)));
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_PARSER = (builder, buf, offset, len, ji) -> {
    int c;
    if ((c = fieldCompare("rateLimits", buf, offset, len)) == 0) {
      while (ji.readArray()) {
        builder.rateLimit(ji.testObject(RateLimit.build(), EXCHANGE_INFO_RATE_LIMITS_PARSER).create());
      }
      return true;
    } else if (c < 0) {
      if ((c = fieldCompare("timezone", buf, offset, len)) == 0) {
        builder.timezone(ji.readString());
        return true;
      } else if (c < 0) {
        if (fieldEquals("symbols", buf, offset, len)) {
          while (ji.readArray()) {
            builder.productSymbol(ji.testObject(ProductSymbol.build(), EXCHANGE_INFO_SYMBOLS_PARSER).create());
          }
          return true;
        }
      }
    } else if ((c = fieldCompare("serverTime", buf, offset, len)) == 0) {
      builder.serverTime(ji.readLong());
      return true;
    } else if (c > 0 && fieldEquals("exchangeFilters", buf, offset, len)) {
      while (ji.readArray()) {
        ji.skip();
      }
      return true;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo field '%s'.", new String(buf, offset, len)));
  };
}
