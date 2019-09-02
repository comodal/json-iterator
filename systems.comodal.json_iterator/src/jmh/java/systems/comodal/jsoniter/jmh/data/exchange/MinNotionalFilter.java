package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface MinNotionalFilter {

  static MinNotionalFilter create(final BigDecimal minNotional,
                                  final boolean applyToMarket,
                                  final int avgPriceMins) {
    return new MinNotionalFilterVal(minNotional, applyToMarket, avgPriceMins);
  }

  static MinNotionalFilter.Builder build() {
    return new MinNotionalFilterVal.MinNotionalFilterBuilder();
  }

  BigDecimal getMinNotional();

  boolean applyToMarket();

  int getAvgPriceMins();

  interface Builder {

    MinNotionalFilter create();

    Builder minNotional(final BigDecimal minNotional);

    Builder applyToMarket(final boolean applyToMarket);

    Builder avgPriceMins(final int avgPriceMins);
  }
}
