package systems.comodal.jsoniter.jmh.data.exchange;

import java.util.EnumSet;
import java.util.Set;

final class ProductSymbolVal implements ProductSymbol {

  private final String symbol;
  private final String status;
  private final String baseAsset;
  private final int baseAssetPrecision;
  private final String quoteAsset;
  private final int quoteAssetPrecision;
  private final Set<OrderType> orderTypes;
  private final boolean icebergAllowed;
  private final boolean ocoAllowed;
  private final int icebergPartsLimit;
  private final int maxNumAlgoOrders;
  private final boolean isSpotTradingAllowed;
  private final boolean isMarginTradingAllowed;
  private final PriceFilter priceFilter;
  private final PercentPriceFilter percentPriceFilter;
  private final LotSizeFilter lotSizeFilter;
  private final LotSizeFilter marketLotSizeFilter;
  private final MinNotionalFilter minNotionalFilter;

  private ProductSymbolVal(final String symbol,
                           final String status,
                           final String baseAsset,
                           final int baseAssetPrecision,
                           final String quoteAsset,
                           final int quoteAssetPrecision,
                           final Set<OrderType> orderTypes,
                           final boolean icebergAllowed,
                           final boolean ocoAllowed,
                           final boolean isSpotTradingAllowed,
                           final boolean isMarginTradingAllowed,
                           final int icebergPartsLimit,
                           final int maxNumAlgoOrders,
                           final PriceFilter priceFilter,
                           final PercentPriceFilter percentPriceFilter,
                           final LotSizeFilter lotSizeFilter,
                           final LotSizeFilter marketLotSizeFilter,
                           final MinNotionalFilter minNotionalFilter) {
    this.symbol = symbol;
    this.status = status;
    this.baseAsset = baseAsset;
    this.baseAssetPrecision = baseAssetPrecision;
    this.quoteAsset = quoteAsset;
    this.quoteAssetPrecision = quoteAssetPrecision;
    this.orderTypes = orderTypes;
    this.icebergAllowed = icebergAllowed;
    this.ocoAllowed = ocoAllowed;
    this.isSpotTradingAllowed = isSpotTradingAllowed;
    this.isMarginTradingAllowed = isMarginTradingAllowed;
    this.icebergPartsLimit = icebergPartsLimit;
    this.maxNumAlgoOrders = maxNumAlgoOrders;
    this.priceFilter = priceFilter;
    this.percentPriceFilter = percentPriceFilter;
    this.lotSizeFilter = lotSizeFilter;
    this.marketLotSizeFilter = marketLotSizeFilter;
    this.minNotionalFilter = minNotionalFilter;
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public String getBaseAsset() {
    return baseAsset;
  }

  @Override
  public int getBaseAssetPrecision() {
    return baseAssetPrecision;
  }

  @Override
  public String getQuoteAsset() {
    return quoteAsset;
  }

  @Override
  public int getQuoteAssetPrecision() {
    return quoteAssetPrecision;
  }

  @Override
  public Set<OrderType> getOrderTypes() {
    return orderTypes;
  }

  @Override
  public boolean isIcebergAllowed() {
    return icebergAllowed;
  }

  @Override
  public boolean isOcoAllowed() {
    return ocoAllowed;
  }

  @Override
  public boolean isSpotTradingAllowed() {
    return isSpotTradingAllowed;
  }

  @Override
  public boolean isMarginTradingAllowed() {
    return isMarginTradingAllowed;
  }

  @Override
  public int getIcebergPartsLimit() {
    return icebergPartsLimit;
  }

  @Override
  public int getMaxNumAlgoOrders() {
    return maxNumAlgoOrders;
  }

  @Override
  public PriceFilter getPriceFilter() {
    return priceFilter;
  }

  @Override
  public PercentPriceFilter getPercentPriceFilter() {
    return percentPriceFilter;
  }

  @Override
  public LotSizeFilter getLotSizeFilter() {
    return lotSizeFilter;
  }

  @Override
  public LotSizeFilter getMarketLotSizeFilter() {
    return marketLotSizeFilter;
  }

  @Override
  public MinNotionalFilter getMinNotionalFilter() {
    return minNotionalFilter;
  }

  static final class ProductSymbolBuilder implements ProductSymbol.Builder {

    private String symbol;
    private String status;
    private String baseAsset;
    private int baseAssetPrecision;
    private String quoteAsset;
    private int quoteAssetPrecision;
    private Set<OrderType> orderTypes;
    private boolean icebergAllowed;
    private boolean ocoAllowed;
    private boolean isSpotTradingAllowed;
    private boolean isMarginTradingAllowed;
    private int icebergPartsLimit;
    private int maxNumAlgoOrders;
    private PriceFilter priceFilter;
    private PercentPriceFilter percentPriceFilter;
    private LotSizeFilter lotSizeFilter;
    private LotSizeFilter marketLotSizeFilter;
    private MinNotionalFilter minNotionalFilter;

    ProductSymbolBuilder() {
    }

    @Override
    public ProductSymbol create() {
      return new ProductSymbolVal(symbol, status, baseAsset, baseAssetPrecision, quoteAsset, quoteAssetPrecision,
          orderTypes == null ? EnumSet.noneOf(OrderType.class) : orderTypes,
          icebergAllowed, ocoAllowed, isSpotTradingAllowed, isMarginTradingAllowed,
          icebergPartsLimit, maxNumAlgoOrders,
          priceFilter, percentPriceFilter, lotSizeFilter, marketLotSizeFilter, minNotionalFilter);
    }

    @Override
    public Builder symbol(final String symbol) {
      this.symbol = symbol;
      return this;
    }

    @Override
    public Builder status(final String status) {
      this.status = status;
      return this;
    }

    @Override
    public Builder baseAsset(final String baseAsset) {
      this.baseAsset = baseAsset;
      return this;
    }

    @Override
    public Builder baseAssetPrecision(final int baseAssetPrecision) {
      this.baseAssetPrecision = baseAssetPrecision;
      return this;
    }

    @Override
    public Builder quoteAsset(final String quoteAsset) {
      this.quoteAsset = quoteAsset;
      return this;
    }

    @Override
    public Builder quoteAssetPrecision(final int quoteAssetPrecision) {
      this.quoteAssetPrecision = quoteAssetPrecision;
      return this;
    }

    @Override
    public Builder orderType(final String orderType) {
      if (orderTypes == null) {
        orderTypes = EnumSet.of(OrderType.valueOf(orderType));
        return this;
      }
      orderTypes.add(OrderType.valueOf(orderType));
      return this;
    }

    @Override
    public Builder icebergAllowed(final boolean icebergAllowed) {
      this.icebergAllowed = icebergAllowed;
      return this;
    }

    @Override
    public Builder ocoAllowed(final boolean ocoAllowed) {
      this.ocoAllowed = ocoAllowed;
      return this;
    }

    @Override
    public Builder isSpotTradingAllowed(final boolean isSpotTradingAllowed) {
      this.isSpotTradingAllowed = isSpotTradingAllowed;
      return this;
    }

    @Override
    public Builder isMarginTradingAllowed(final boolean isMarginTradingAllowed) {
      this.isMarginTradingAllowed = isMarginTradingAllowed;
      return this;
    }

    @Override
    public Builder filter(final Filter.Builder filter) {
      filter.build(this);
      return this;
    }

    @Override
    public Builder icebergPartsLimit(final int icebergPartsLimit) {
      this.icebergPartsLimit = icebergPartsLimit;
      return this;
    }

    @Override
    public Builder maxNumAlgoOrders(final int maxNumAlgoOrders) {
      this.maxNumAlgoOrders = maxNumAlgoOrders;
      return this;
    }

    @Override
    public Builder priceFilter(final PriceFilter priceFilter) {
      this.priceFilter = priceFilter;
      return this;
    }

    @Override
    public Builder percentPriceFilter(final PercentPriceFilter percentPriceFilter) {
      this.percentPriceFilter = percentPriceFilter;
      return this;
    }

    @Override
    public Builder lotSizeFilter(final LotSizeFilter lotSizeFilter) {
      this.lotSizeFilter = lotSizeFilter;
      return this;
    }

    @Override
    public Builder marketLotSizeFilter(final LotSizeFilter marketLotSizeFilter) {
      this.marketLotSizeFilter = marketLotSizeFilter;
      return this;
    }

    @Override
    public Builder minNotionalFilter(final MinNotionalFilter minNotionalFilter) {
      this.minNotionalFilter = minNotionalFilter;
      return this;
    }
  }
}
