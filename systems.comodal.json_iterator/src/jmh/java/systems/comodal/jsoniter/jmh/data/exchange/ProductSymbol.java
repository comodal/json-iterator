package systems.comodal.jsoniter.jmh.data.exchange;

import java.util.Set;

public interface ProductSymbol {

  String getSymbol();

  String getStatus();

  String getBaseAsset();

  int getBaseAssetPrecision();

  String getQuoteAsset();

  int getQuoteAssetPrecision();

  Set<OrderType> getOrderTypes();

  boolean isIcebergAllowed();

  boolean isOcoAllowed();

  boolean isSpotTradingAllowed();

  boolean isMarginTradingAllowed();

  int getIcebergPartsLimit();

  int getMaxNumAlgoOrders();

  PriceFilter getPriceFilter();

  PercentPriceFilter getPercentPriceFilter();

  LotSizeFilter getLotSizeFilter();

  LotSizeFilter getMarketLotSizeFilter();

  MinNotionalFilter getMinNotionalFilter();

  static ProductSymbol.Builder build() {
    return new ProductSymbolVal.ProductSymbolBuilder();
  }

  interface Builder {

    ProductSymbol create();

    Builder symbol(final String symbol);

    Builder status(final String status);

    Builder baseAsset(final String baseAsset);

    Builder baseAssetPrecision(final int baseAssetPrecision);

    Builder quoteAsset(final String quoteAsset);

    Builder quoteAssetPrecision(final int quoteAssetPrecision);

    Builder orderType(final String orderType);

    Builder icebergAllowed(final boolean icebergAllowed);

    Builder ocoAllowed(final boolean ocoAllowed);

    Builder isSpotTradingAllowed(final boolean isSpotTradingAllowed);

    Builder isMarginTradingAllowed(final boolean isMarginTradingAllowed);

    Builder filter(final Filter.Builder filter);

    Builder icebergPartsLimit(final int icebergPartsLimit);

    Builder maxNumAlgoOrders(final int maxNumAlgoOrders);

    Builder priceFilter(final PriceFilter priceFilter);

    Builder percentPriceFilter(final PercentPriceFilter percentPriceFilter);

    Builder lotSizeFilter(final LotSizeFilter lotSizeFilter);

    Builder marketLotSizeFilter(final LotSizeFilter marketLotSizeFilter);

    Builder minNotionalFilter(final MinNotionalFilter minNotionalFilter);
  }
}
