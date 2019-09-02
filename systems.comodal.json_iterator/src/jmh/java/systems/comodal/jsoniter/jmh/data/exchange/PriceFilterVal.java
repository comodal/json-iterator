package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

final class PriceFilterVal implements PriceFilter {

  private final BigDecimal minPrice;
  private final BigDecimal maxPrice;
  private final BigDecimal tickSize;

  PriceFilterVal(final BigDecimal minPrice,
                 final BigDecimal maxPrice,
                 final BigDecimal tickSize) {
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.tickSize = tickSize;
  }

  @Override
  public BigDecimal getMinPrice() {
    return minPrice;
  }

  @Override
  public BigDecimal getMaxPrice() {
    return maxPrice;
  }

  @Override
  public BigDecimal getTickSize() {
    return tickSize;
  }

  static final class PriceFilterBuilder implements PriceFilter.Builder {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal tickSize;

    PriceFilterBuilder() {
    }

    @Override
    public PriceFilter create() {
      return new PriceFilterVal(minPrice, maxPrice, tickSize);
    }

    @Override
    public Builder minPrice(final BigDecimal minPrice) {
      this.minPrice = minPrice;
      return this;
    }

    @Override
    public Builder maxPrice(final BigDecimal maxPrice) {
      this.maxPrice = maxPrice;
      return this;
    }

    @Override
    public Builder tickSize(final BigDecimal tickSize) {
      this.tickSize = tickSize;
      return this;
    }
  }
}
