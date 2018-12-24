package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

final class FilterBuilder implements Filter.Builder {

  private FilterType type;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private BigDecimal tickSize;
  private BigDecimal multiplierUp;
  private BigDecimal multiplierDown;
  private int avgPriceMins;
  private BigDecimal minQty;
  private BigDecimal maxQty;
  private BigDecimal stepSize;
  private BigDecimal minNotional;
  private boolean applyToMarket;

  private int limit;
  private int maxNumAlgoOrders;

  @Override
  public Filter.Builder build(final ProductSymbol.Builder builder) {
    switch (type) {
      case PRICE_FILTER:
        builder.priceFilter(new PriceFilterVal(minPrice, maxPrice, tickSize));
        return this;
      case PERCENT_PRICE:
        builder.percentPriceFilter(new PercentPriceFilterVal(multiplierUp, multiplierDown, avgPriceMins));
        return this;
      case LOT_SIZE:
        builder.lotSizeFilter(new LotSizeFilterVal(minQty, maxQty, stepSize));
        return this;
      case MIN_NOTIONAL:
        builder.minNotionalFilter(new MinNotionalFilterVal(minNotional, applyToMarket, avgPriceMins));
        return this;
      case ICEBERG_PARTS:
        builder.icebergPartsLimit(limit);
        return this;
      case MAX_NUM_ALGO_ORDERS:
        builder.maxNumAlgoOrders(maxNumAlgoOrders);
        return this;
      default:
        throw new IllegalStateException("Unhandled filter type " + type);
    }
  }

  @Override
  public Filter.Builder type(final String type) {
    this.type = FilterType.valueOf(type);
    return this;
  }

  @Override
  public Filter.Builder minPrice(final BigDecimal minPrice) {
    this.minPrice = minPrice;
    return this;
  }

  @Override
  public Filter.Builder maxPrice(final BigDecimal maxPrice) {
    this.maxPrice = maxPrice;
    return this;
  }

  @Override
  public Filter.Builder tickSize(final BigDecimal tickSize) {
    this.tickSize = tickSize;
    return this;
  }

  @Override
  public Filter.Builder multiplierUp(final BigDecimal multiplierUp) {
    this.multiplierUp = multiplierUp;
    return this;
  }

  @Override
  public Filter.Builder multiplierDown(final BigDecimal multiplierDown) {
    this.multiplierDown = multiplierDown;
    return this;
  }

  @Override
  public Filter.Builder avgPriceMins(final int avgPriceMins) {
    this.avgPriceMins = avgPriceMins;
    return this;
  }

  @Override
  public Filter.Builder minQty(final BigDecimal minQty) {
    this.minQty = minQty;
    return this;
  }

  @Override
  public Filter.Builder maxQty(final BigDecimal maxQty) {
    this.maxQty = maxQty;
    return this;
  }

  @Override
  public Filter.Builder stepSize(final BigDecimal stepSize) {
    this.stepSize = stepSize;
    return this;
  }

  @Override
  public Filter.Builder minNotional(final BigDecimal minNotional) {
    this.minNotional = minNotional;
    return this;
  }

  @Override
  public Filter.Builder applyToMarket(final boolean applyToMarket) {
    this.applyToMarket = applyToMarket;
    return this;
  }

  @Override
  public Filter.Builder limit(final int limit) {
    this.limit = limit;
    return this;
  }

  @Override
  public Filter.Builder maxNumAlgoOrders(final int maxNumAlgoOrders) {
    this.maxNumAlgoOrders = maxNumAlgoOrders;
    return this;
  }
}
