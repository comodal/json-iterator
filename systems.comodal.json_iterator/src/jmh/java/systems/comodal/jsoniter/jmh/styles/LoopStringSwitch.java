package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

final class LoopStringSwitch implements JsonIterParser<ExchangeInfo> {

  LoopStringSwitch() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return parseExchangeInfo(ji);
  }

  private static ExchangeInfo parseExchangeInfo(final JsonIterator ji) {
    final var info = ExchangeInfo.build();
    for (var field = ji.readObjField(); field != null; field = ji.readObjField()) {
      switch (field) {
        case "timezone" -> info.timezone(ji.readString());
        case "serverTime" -> info.serverTime(ji.readLong());
        case "rateLimits" -> {
          while (ji.readArray()) {
            final var rateLimit = RateLimit.build();
            for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
              switch (field) {
                case "rateLimitType" -> rateLimit.type(ji.readString());
                case "interval" -> rateLimit.intervalUnit(ji.readString());
                case "intervalNum" -> rateLimit.interval(ji.readLong());
                case "limit" -> rateLimit.limit(ji.readInt());
                default -> throw new IllegalStateException("Unhandled rate limit field " + field);
              }
            }
            info.rateLimit(rateLimit.create());
          }
        }
        case "exchangeFilters" -> {
          while (ji.readArray()) {
            ji.skip();
          }
        }
        case "symbols" -> {
          while (ji.readArray()) {
            final var symbol = ProductSymbol.build();
            for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
              switch (field) {
                case "symbol" -> symbol.symbol(ji.readString());
                case "status" -> symbol.status(ji.readString());
                case "baseAsset" -> symbol.baseAsset(ji.readString());
                case "baseAssetPrecision" -> symbol.baseAssetPrecision(ji.readInt());
                case "quoteAsset" -> symbol.quoteAsset(ji.readString());
                case "quotePrecision" -> symbol.quoteAssetPrecision(ji.readInt());
                case "orderTypes" -> {
                  while (ji.readArray()) {
                    symbol.orderType(ji.readString());
                  }
                }
                case "icebergAllowed" -> symbol.icebergAllowed(ji.readBoolean());
                case "ocoAllowed" -> symbol.ocoAllowed(ji.readBoolean());
                case "isSpotTradingAllowed" -> symbol.isSpotTradingAllowed(ji.readBoolean());
                case "isMarginTradingAllowed" -> symbol.isMarginTradingAllowed(ji.readBoolean());
                case "filters" -> {
                  while (ji.readArray()) {
                    final var filter = Filter.build();
                    for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                      switch (field) {
                        case "filterType" -> filter.type(ji.readString());
                        case "minPrice" -> filter.minPrice(ji.readBigDecimal());
                        case "maxPrice" -> filter.maxPrice(ji.readBigDecimal());
                        case "tickSize" -> filter.tickSize(ji.readBigDecimal());
                        case "multiplierUp" -> filter.multiplierUp(ji.readBigDecimal());
                        case "multiplierDown" -> filter.multiplierDown(ji.readBigDecimal());
                        case "avgPriceMins" -> filter.avgPriceMins(ji.readInt());
                        case "minQty" -> filter.minQty(ji.readBigDecimal());
                        case "maxQty" -> filter.maxQty(ji.readBigDecimal());
                        case "stepSize" -> filter.stepSize(ji.readBigDecimal());
                        case "minNotional" -> filter.minNotional(ji.readBigDecimal());
                        case "applyToMarket" -> filter.applyToMarket(ji.readBoolean());
                        case "limit" -> filter.limit(ji.readInt());
                        case "maxNumAlgoOrders" -> filter.maxNumAlgoOrders(ji.readInt());
                        default -> throw new IllegalStateException("Unhandled filter field " + field);
                      }
                    }
                    symbol.filter(filter);
                  }
                }
                default -> throw new IllegalStateException("Unhandled symbol field " + field);
              }
            }
            info.productSymbol(symbol.create());
          }
        }
        default -> throw new IllegalStateException("Unhandled field " + field);
      }
    }
    return info.create();
  }
}
