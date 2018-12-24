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
}
