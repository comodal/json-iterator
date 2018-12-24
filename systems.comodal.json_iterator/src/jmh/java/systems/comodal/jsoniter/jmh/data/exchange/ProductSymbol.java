package systems.comodal.jsoniter;

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

  int getIcebergPartsLimit();

  int getMaxNumAlgoOrders();

  PriceFilter getPriceFilter();

  PercentPriceFilter getPercentPriceFilter();

  LotSizeFilter getLotSizeFilter();

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

    Builder filter(final Filter.Builder filter);

    Builder icebergPartsLimit(final int icebergPartsLimit);

    Builder maxNumAlgoOrders(final int maxNumAlgoOrders);

    Builder priceFilter(final PriceFilter priceFilter);

    Builder percentPriceFilter(final PercentPriceFilter percentPriceFilter);

    Builder lotSizeFilter(final LotSizeFilter lotSizeFilter);

    Builder minNotionalFilter(final MinNotionalFilter minNotionalFilter);
  }
}
