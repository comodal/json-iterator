package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;
import systems.comodal.jsoniter.jmh.data.exchange.Filter;
import systems.comodal.jsoniter.jmh.data.exchange.ProductSymbol;
import systems.comodal.jsoniter.jmh.data.exchange.RateLimit;

import java.io.IOException;

final class LoopStringIf implements JsonIterParser<ExchangeInfo> {

  LoopStringIf() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) throws IOException {
    return parseExchangeInfo(ji);
  }

  static ExchangeInfo parseExchangeInfo(final JsonIterator ji) throws IOException {
    final var info = ExchangeInfo.build();
    for (var field = ji.readObjField(); field != null; field = ji.readObjField()) {
      if ("timezone".equals(field)) {
        info.timezone(ji.readString());
      } else if ("serverTime".equals(field)) {
        info.serverTime(ji.readLong());
      } else if ("rateLimits".equals(field)) {
        while (ji.readArray()) {
          final var rateLimit = RateLimit.build();
          for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
            if ("rateLimitType".equals(field)) {
              rateLimit.type(ji.readString());
            } else if ("interval".equals(field)) {
              rateLimit.intervalUnit(ji.readString());
            } else if ("intervalNum".equals(field)) {
              rateLimit.interval(ji.readLong());
            } else if ("limit".equals(field)) {
              rateLimit.limit(ji.readInt());
            } else {
              throw new IllegalStateException("Unhandled rate limit field " + field);
            }
          }
          info.rateLimit(rateLimit.create());
        }
      } else if ("exchangeFilters".equals(field)) {
        while (ji.readArray()) {
          ji.skip();
        }
      } else if ("symbols".equals(field)) {
        while (ji.readArray()) {
          final var symbol = ProductSymbol.build();
          for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
            if ("symbol".equals(field)) {
              symbol.symbol(ji.readString());
            } else if ("status".equals(field)) {
              symbol.status(ji.readString());
            } else if ("baseAsset".equals(field)) {
              symbol.baseAsset(ji.readString());
            } else if ("baseAssetPrecision".equals(field)) {
              symbol.baseAssetPrecision(ji.readInt());
            } else if ("quoteAsset".equals(field)) {
              symbol.quoteAsset(ji.readString());
            } else if ("quotePrecision".equals(field)) {
              symbol.quoteAssetPrecision(ji.readInt());
            } else if ("orderTypes".equals(field)) {
              while (ji.readArray()) {
                symbol.orderType(ji.readString());
              }
            } else if ("icebergAllowed".equals(field)) {
              symbol.icebergAllowed(ji.readBoolean());
            } else if ("filters".equals(field)) {
              while (ji.readArray()) {
                final var filter = Filter.build();
                for (field = ji.readObjField(); field != null; field = ji.readObjField()) {
                  if ("filterType".equals(field)) {
                    filter.type(ji.readString());
                  } else if ("minPrice".equals(field)) {
                    filter.minPrice(ji.readBigDecimal());
                  } else if ("maxPrice".equals(field)) {
                    filter.maxPrice(ji.readBigDecimal());
                  } else if ("tickSize".equals(field)) {
                    filter.tickSize(ji.readBigDecimal());
                  } else if ("multiplierUp".equals(field)) {
                    filter.multiplierUp(ji.readBigDecimal());
                  } else if ("multiplierDown".equals(field)) {
                    filter.multiplierDown(ji.readBigDecimal());
                  } else if ("avgPriceMins".equals(field)) {
                    filter.avgPriceMins(ji.readInt());
                  } else if ("minQty".equals(field)) {
                    filter.minQty(ji.readBigDecimal());
                  } else if ("maxQty".equals(field)) {
                    filter.maxQty(ji.readBigDecimal());
                  } else if ("stepSize".equals(field)) {
                    filter.stepSize(ji.readBigDecimal());
                  } else if ("minNotional".equals(field)) {
                    filter.minNotional(ji.readBigDecimal());
                  } else if ("applyToMarket".equals(field)) {
                    filter.applyToMarket(ji.readBoolean());
                  } else if ("limit".equals(field)) {
                    filter.limit(ji.readInt());
                  } else if ("maxNumAlgoOrders".equals(field)) {
                    filter.maxNumAlgoOrders(ji.readInt());
                  } else {
                    throw new IllegalStateException("Unhandled filter field " + field);
                  }
                }
                symbol.filter(filter);
              }
            } else {
              throw new IllegalStateException("Unhandled symbol field " + field);
            }
          }
          info.productSymbol(symbol.create());
        }
      } else {
        throw new IllegalStateException("Unhandled field " + field);
      }
    }
    return info.create();
  }
}
