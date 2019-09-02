package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.CharBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.jmh.data.exchange.*;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

final class StaticFieldOrdering implements JsonIterParser<ExchangeInfo> {

  StaticFieldOrdering() {
  }

  @Override
  public ExchangeInfo parse(final JsonIterator ji) {
    return parseExchangeInfo(ji);
  }

  private static final CharBufferPredicate HAS_MARKET_LOT_SIZE_FILTER = (buf, offset, len) -> fieldEquals("MARKET_LOT_SIZE", buf, offset, len);

  private static ExchangeInfo parseExchangeInfo(final JsonIterator ji) {
    final var info = ExchangeInfo.build();
    info.timezone(ji.skipObjField().readString());
    info.serverTime(ji.skipObjField().readLong());
    for (ji.skipObjField(); ji.readArray(); ji.closeObj()) { // rateLimits
      info.rateLimit(RateLimit.build()
          .type(ji.skipObjField().readString())
          .intervalUnit(ji.skipObjField().readString())
          .interval(ji.skipObjField().readLong())
          .limit(ji.skipObjField().readInt())
          .create());
    }
    for (ji.skipObjField(); ji.readArray(); ) { // exchangeFilters
      ji.skip();
    }

    for (ji.skipObjField(); ji.readArray(); ji.closeObj()) { // symbols
      final var symbol = ProductSymbol.build()
          .symbol(ji.skipObjField().readString())
          .status(ji.skipObjField().readString())
          .baseAsset(ji.skipObjField().readString())
          .baseAssetPrecision(ji.skipObjField().readInt())
          .quoteAsset(ji.skipObjField().readString())
          .quoteAssetPrecision(ji.skipObjField().readInt());
      for (ji.skipObjField(); ji.readArray(); ) {
        symbol.orderType(ji.readString());
      }
      symbol.icebergAllowed(ji.skipObjField().readBoolean());
      symbol.ocoAllowed(ji.skipObjField().readBoolean());
      symbol.isSpotTradingAllowed(ji.skipObjField().readBoolean());
      symbol.isMarginTradingAllowed(ji.skipObjField().readBoolean());

      ji.skipObjField().openArray(); // "filters": [
      ji.skipObjField().skip(); // { "filterType": "PRICE_FILTER",
      symbol.priceFilter(PriceFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "PERCENT_PRICE",
      symbol.percentPriceFilter(PercentPriceFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readInt()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "LOT_SIZE",
      symbol.lotSizeFilter(LotSizeFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "MIN_NOTIONAL",
      symbol.minNotionalFilter(MinNotionalFilter.create(
          ji.skipObjField().readBigDecimal(), ji.skipObjField().readBoolean(), ji.skipObjField().readInt()
      ));
      ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "ICEBERG_PARTS",
      symbol.icebergPartsLimit(ji.skipObjField().readInt());

      ji.closeObj().continueArray().skipObjField();
      if (ji.testChars(HAS_MARKET_LOT_SIZE_FILTER)) {
        symbol.marketLotSizeFilter(LotSizeFilter.create(
            ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal(), ji.skipObjField().readBigDecimal()
        ));
        ji.closeObj().continueArray().skipObjField().skip(); // } , {  "filterType": "MAX_NUM_ALGO_ORDERS",
      }

      symbol.maxNumAlgoOrders(ji.skipObjField().readInt());

      ji.closeObj().closeArray(); // } ]
      info.productSymbol(symbol.create());
    }
    return info.create();
  }
}
