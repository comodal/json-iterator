package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

final class PercentPriceFilterVal implements PercentPriceFilter {

  private final BigDecimal multiplierUp;
  private final BigDecimal multiplierDown;
  private final int avgPriceMins;

  PercentPriceFilterVal(final BigDecimal multiplierUp,
                        final BigDecimal multiplierDown,
                        final int avgPriceMins) {
    this.multiplierUp = multiplierUp;
    this.multiplierDown = multiplierDown;
    this.avgPriceMins = avgPriceMins;
  }

  @Override
  public BigDecimal getMultiplierUp() {
    return multiplierUp;
  }

  @Override
  public BigDecimal getMultiplierDown() {
    return multiplierDown;
  }

  @Override
  public int getAvgPriceMins() {
    return avgPriceMins;
  }

  static final class PercentPriceFilterBuilder implements PercentPriceFilter.Builder {

    private BigDecimal multiplierUp;
    private BigDecimal multiplierDown;
    private int avgPriceMins;

    PercentPriceFilterBuilder() {
    }

    @Override
    public PercentPriceFilter create() {
      return new PercentPriceFilterVal(multiplierUp, multiplierDown, avgPriceMins);
    }

    @Override
    public Builder multiplierUp(final BigDecimal multiplierUp) {
      this.multiplierUp = multiplierUp;
      return this;
    }

    @Override
    public Builder multiplierDown(final BigDecimal multiplierDown) {
      this.multiplierDown = multiplierDown;
      return this;
    }

    @Override
    public Builder avgPriceMins(final int avgPriceMins) {
      this.avgPriceMins = avgPriceMins;
      return this;
    }
  }
}
