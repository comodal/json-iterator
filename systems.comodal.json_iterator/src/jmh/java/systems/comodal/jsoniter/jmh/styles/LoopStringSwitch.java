package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import java.io.IOException;

final class LoopStringSwitch implements JsonIterParser<ExchangeInfo> {

  LoopStringSwitch() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) throws IOException {
    return parseExchangeInfo(ji);
  }

  private static ExchangeInfo parseExchangeInfo(final JsonIterator ji) {
    final var info = ExchangeInfo.build();
    for (var field = ji.readObjField(); field != null; field = ji.readObjField()) {
      switch (field) {
        case "timezone":
          info.timezone(ji.readString());
          continue;
        case "serverTime":
          info.serverTime(ji.readLong());
          continue;
        case "rateLimits":
          while (ji.readArray()) {
            final var rateLimit = RateLimit.build();
            for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
              switch (field) {
                case "rateLimitType":
                  rateLimit.type(ji.readString());
                  continue;
                case "interval":
                  rateLimit.intervalUnit(ji.readString());
                  continue;
                case "intervalNum":
                  rateLimit.interval(ji.readLong());
                  continue;
                case "limit":
                  rateLimit.limit(ji.readInt());
                  continue;
                default:
                  throw new IllegalStateException("Unhandled rate limit field " + field);
              }
            }
            info.rateLimit(rateLimit.create());
          }
          continue;
        case "exchangeFilters":
          while (ji.readArray()) {
            ji.skip();
          }
          continue;
        case "symbols":
          while (ji.readArray()) {
            final var symbol = ProductSymbol.build();
            for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
              switch (field) {
                case "symbol":
                  symbol.symbol(ji.readString());
                  continue;
                case "status":
                  symbol.status(ji.readString());
                  continue;
                case "baseAsset":
                  symbol.baseAsset(ji.readString());
                  continue;
                case "baseAssetPrecision":
                  symbol.baseAssetPrecision(ji.readInt());
                  continue;
                case "quoteAsset":
                  symbol.quoteAsset(ji.readString());
                  continue;
                case "quotePrecision":
                  symbol.quoteAssetPrecision(ji.readInt());
                  continue;
                case "orderTypes":
                  while (ji.readArray()) {
                    symbol.orderType(ji.readString());
                  }
                  continue;
                case "icebergAllowed":
                  symbol.icebergAllowed(ji.readBoolean());
                  continue;
                case "filters":
                  while (ji.readArray()) {
                    final var filter = Filter.build();
                    for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                      switch (field) {
                        case "filterType":
                          filter.type(ji.readString());
                          continue;
                        case "minPrice":
                          filter.minPrice(ji.readBigDecimal());
                          continue;
                        case "maxPrice":
                          filter.maxPrice(ji.readBigDecimal());
                          continue;
                        case "tickSize":
                          filter.tickSize(ji.readBigDecimal());
                          continue;
                        case "multiplierUp":
                          filter.multiplierUp(ji.readBigDecimal());
                          continue;
                        case "multiplierDown":
                          filter.multiplierDown(ji.readBigDecimal());
                          continue;
                        case "avgPriceMins":
                          filter.avgPriceMins(ji.readInt());
                          continue;
                        case "minQty":
                          filter.minQty(ji.readBigDecimal());
                          continue;
                        case "maxQty":
                          filter.maxQty(ji.readBigDecimal());
                          continue;
                        case "stepSize":
                          filter.stepSize(ji.readBigDecimal());
                          continue;
                        case "minNotional":
                          filter.minNotional(ji.readBigDecimal());
                          continue;
                        case "applyToMarket":
                          filter.applyToMarket(ji.readBoolean());
                          continue;
                        case "limit":
                          filter.limit(ji.readInt());
                          continue;
                        case "maxNumAlgoOrders":
                          filter.maxNumAlgoOrders(ji.readInt());
                          continue;
                        default:
                          throw new IllegalStateException("Unhandled filter field " + field);
                      }
                    }
                    symbol.filter(filter);
                  }
                  continue;
                default:
                  throw new IllegalStateException("Unhandled symbol field " + field);
              }
            }
            info.productSymbol(symbol.create());
          }
          continue;
        default:
          throw new IllegalStateException("Unhandled field " + field);
      }
    }
    return info.create();
  }
}
