package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface PercentPriceFilter {

  static PercentPriceFilter create(final BigDecimal multiplierUp,
                            final BigDecimal multiplierDown,
                            final int avgPriceMins) {
    return new PercentPriceFilterVal(multiplierUp, multiplierDown, avgPriceMins);
  }

  BigDecimal getMultiplierUp();

  BigDecimal getMultiplierDown();

  int getAvgPriceMins();
}
