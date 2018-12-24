package systems.comodal.jsoniter;

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
}
