package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import static systems.comodal.jsoniter.JIUtil.fieldHashCode;
import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

final class IocLoopCompareStringFieldToCharsIfNHashN implements JsonIterParser<ExchangeInfo> {

  IocLoopCompareStringFieldToCharsIfNHashN() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return ji.testObject(ExchangeInfo.build(), EXCHANGE_INFO_PARSER).create();
  }

  private static final ContextFieldBufferPredicate<Filter.Builder> EXCHANGE_INFO_SYMBOLS_FILTERS_PARSER = (builder, buf, offset, len, ji) -> {
    switch (fieldHashCode(buf, offset, len < 11 ? offset + len : offset + 11)) {
      case 1774057787:
        if (fieldEquals("avgPriceMins", buf, offset, len)) {
          builder.avgPriceMins(ji.readInt());
          return true;
        }
        break;
      case 1428493549:
        if (fieldEquals("stepSize", buf, offset, len)) {
          builder.stepSize(ji.readBigDecimal());
          return true;
        }
        break;
      case 562576035:
        if (fieldEquals("multiplierDown", buf, offset, len)) {
          builder.multiplierDown(ji.readBigDecimal());
          return true;
        }
        break;
      case 1936822078:
        if (fieldEquals("tickSize", buf, offset, len)) {
          builder.tickSize(ji.readBigDecimal());
          return true;
        }
        break;
      case -1843234570:
        if (fieldEquals("applyToMarket", buf, offset, len)) {
          builder.applyToMarket(ji.readBoolean());
          return true;
        }
        break;
      case -1354466382:
        if (fieldEquals("minNotional", buf, offset, len)) {
          builder.minNotional(ji.readBigDecimal());
          return true;
        }
        break;
      case -1382007273:
        if (fieldEquals("minPrice", buf, offset, len)) {
          builder.minPrice(ji.readBigDecimal());
          return true;
        }
        break;
      case 102976443:
        if (fieldEquals("limit", buf, offset, len)) {
          builder.limit(ji.readInt());
          return true;
        }
        break;
      case 115580410:
        if (fieldEquals("maxNumAlgoOrders", buf, offset, len)) {
          builder.maxNumAlgoOrders(ji.readInt());
          return true;
        }
        break;
      case 394189381:
        if (fieldEquals("maxPrice", buf, offset, len)) {
          builder.maxPrice(ji.readBigDecimal());
          return true;
        }
        break;
      case 562576052:
        if (fieldEquals("multiplierUp", buf, offset, len)) {
          builder.multiplierUp(ji.readBigDecimal());
          return true;
        }
        break;
      case -1081151822:
        if (fieldEquals("maxQty", buf, offset, len)) {
          builder.maxQty(ji.readBigDecimal());
          return true;
        }
        break;
      case -1553050926:
        if (fieldEquals("filterType", buf, offset, len)) {
          builder.type(ji.readString());
          return true;
        }
        break;
      case -1074061564:
        if (fieldEquals("minQty", buf, offset, len)) {
          builder.minQty(ji.readBigDecimal());
          return true;
        }
        break;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo.symbols.filters field '%s'.", new String(buf, offset, len)));
  };

  private static final ContextFieldBufferPredicate<ProductSymbol.Builder> EXCHANGE_INFO_SYMBOLS_PARSER = (builder, buf, offset, len, ji) -> {
    switch (fieldHashCode(buf, offset, len < 10 ? offset + len : offset + 10)) {
      case 1268588845:
        if (fieldEquals("quotePrecision", buf, offset, len)) {
          builder.quoteAssetPrecision(ji.readInt());
          return true;
        }
        break;
      case 1670198925:
        if (fieldEquals("isSpotTradingAllowed", buf, offset, len)) {
          // TODO
          ji.skip();
          return true;
        }
        break;
      case -887523944:
        if (fieldEquals("symbol", buf, offset, len)) {
          builder.symbol(ji.readString());
          return true;
        }
        break;
      case 2002745310:
        if (fieldEquals("icebergAllowed", buf, offset, len)) {
          builder.icebergAllowed(ji.readBoolean());
          return true;
        }
        break;
      case 2044200973:
        if (fieldEquals("ocoAllowed", buf, offset, len)) {
          // TODO
          ji.skip();
          return true;
        }
        break;
      case 746406347:
        if (fieldEquals("orderTypes", buf, offset, len)) {
          while (ji.readArray()) {
            builder.orderType(ji.readString());
          }
          return true;
        }
        break;
      case -854547461:
        if (fieldEquals("filters", buf, offset, len)) {
          while (ji.readArray()) {
            builder.filter(ji.testObject(Filter.build(), EXCHANGE_INFO_SYMBOLS_FILTERS_PARSER));
          }
          return true;
        }
        break;
      case -1844146529:
        if (fieldEquals("baseAsset", buf, offset, len)) {
          builder.baseAsset(ji.readString());
          return true;
        }
        break;
      case -1333967471:
        if (fieldEquals("baseAssetPrecision", buf, offset, len)) {
          builder.baseAssetPrecision(ji.readInt());
          return true;
        }
        break;
      case 1254779348:
        if (fieldEquals("quoteAsset", buf, offset, len)) {
          builder.quoteAsset(ji.readString());
          return true;
        }
        break;
      case -892481550:
        if (fieldEquals("status", buf, offset, len)) {
          builder.status(ji.readString());
          return true;
        }
        break;
      case -549878762:
        if (fieldEquals("isMarginTradingAllowed", buf, offset, len)) {
          // TODO
          ji.skip();
          return true;
        }
        break;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo.symbols field '%s'.", new String(buf, offset, len)));
  };

  private static final ContextFieldBufferPredicate<RateLimit.Builder> EXCHANGE_INFO_RATE_LIMITS_PARSER = (builder, buf, offset, len, ji) -> {
    switch (fieldHashCode(buf, offset, len < 9 ? offset + len : offset + 9)) {
      case 198711995:
        if (fieldEquals("rateLimitType", buf, offset, len)) {
          builder.type(ji.readString());
          return true;
        }
        break;
      case 503100457:
        if (fieldEquals("intervalNum", buf, offset, len)) {
          builder.interval(ji.readLong());
          return true;
        }
        break;
      case 102976443:
        if (fieldEquals("limit", buf, offset, len)) {
          builder.limit(ji.readInt());
          return true;
        }
        break;
      case 570418373:
        if (fieldEquals("interval", buf, offset, len)) {
          builder.intervalUnit(ji.readString());
          return true;
        }
        break;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo.rateLimits field '%s'.", new String(buf, offset, len)));
  };

  private static final ContextFieldBufferPredicate<ExchangeInfo.Builder> EXCHANGE_INFO_PARSER = (builder, buf, offset, len, ji) -> {
    switch (fieldHashCode(buf, offset, len < 2 ? offset + len : offset + 2)) {
      case 3666:
        if (fieldEquals("serverTime", buf, offset, len)) {
          builder.serverTime(ji.readLong());
          return true;
        }
        break;
      case 3251:
        if (fieldEquals("exchangeFilters", buf, offset, len)) {
          while (ji.readArray()) {
            ji.skip();
          }
          return true;
        }
        break;
      case 3701:
        if (fieldEquals("timezone", buf, offset, len)) {
          builder.timezone(ji.readString());
          return true;
        }
        break;
      case 3686:
        if (fieldEquals("symbols", buf, offset, len)) {
          while (ji.readArray()) {
            builder.productSymbol(ji.testObject(ProductSymbol.build(), EXCHANGE_INFO_SYMBOLS_PARSER).create());
          }
          return true;
        }
        break;
      case 3631:
        if (fieldEquals("rateLimits", buf, offset, len)) {
          while (ji.readArray()) {
            builder.rateLimit(ji.testObject(RateLimit.build(), EXCHANGE_INFO_RATE_LIMITS_PARSER).create());
          }
          return true;
        }
        break;
    }
    throw new IllegalStateException(String.format("Unexpected exchangeInfo field '%s'.", new String(buf, offset, len)));
  };
}
