package systems.comodal.jsoniter;

import java.math.BigDecimal;

final class MinNotionalFilterVal implements MinNotionalFilter {

  private final BigDecimal minNotional;
  private final boolean applyToMarket;
  private final int avgPriceMins;

  MinNotionalFilterVal(final BigDecimal minNotional,
                              final boolean applyToMarket,
                              final int avgPriceMins) {
    this.minNotional = minNotional;
    this.applyToMarket = applyToMarket;
    this.avgPriceMins = avgPriceMins;
  }

  @Override
  public BigDecimal getMinNotional() {
    return minNotional;
  }

  @Override
  public boolean applyToMarket() {
    return applyToMarket;
  }

  @Override
  public int getAvgPriceMins() {
    return avgPriceMins;
  }
}
