package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface PercentPriceFilter {

  static PercentPriceFilter create(final BigDecimal multiplierUp,
                                   final BigDecimal multiplierDown,
                                   final int avgPriceMins) {
    return new PercentPriceFilterVal(multiplierUp, multiplierDown, avgPriceMins);
  }

  static PercentPriceFilter.Builder build() {
    return new PercentPriceFilterVal.PercentPriceFilterBuilder();
  }

  BigDecimal getMultiplierUp();

  BigDecimal getMultiplierDown();

  int getAvgPriceMins();

  interface Builder {

    PercentPriceFilter create();

    Builder multiplierUp(final BigDecimal multiplierUp);

    Builder multiplierDown(final BigDecimal multiplierDown);

    Builder avgPriceMins(final int avgPriceMins);
  }
}
