package systems.comodal.jsoniter.jmh.data.exchange;

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

  static final class MinNotionalFilterBuilder implements MinNotionalFilter.Builder {
    private BigDecimal minNotional;
    private boolean applyToMarket;
    private int avgPriceMins;

    MinNotionalFilterBuilder() {
    }

    @Override
    public MinNotionalFilter create() {
      return new MinNotionalFilterVal(minNotional, applyToMarket, avgPriceMins);
    }

    @Override
    public Builder minNotional(final BigDecimal minNotional) {
      this.minNotional = minNotional;
      return this;
    }

    @Override
    public Builder applyToMarket(final boolean applyToMarket) {
      this.applyToMarket = applyToMarket;
      return this;
    }

    @Override
    public Builder avgPriceMins(final int avgPriceMins) {
      this.avgPriceMins = avgPriceMins;
      return this;
    }
  }
}
