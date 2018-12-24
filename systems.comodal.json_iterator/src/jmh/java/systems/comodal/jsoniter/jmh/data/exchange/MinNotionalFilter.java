package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface MinNotionalFilter {

  static MinNotionalFilter create(final BigDecimal minNotional,
                                  final boolean applyToMarket,
                                  final int avgPriceMins) {
    return new MinNotionalFilterVal(minNotional, applyToMarket, avgPriceMins);
  }

  BigDecimal getMinNotional();

  boolean applyToMarket();

  int getAvgPriceMins();
}
