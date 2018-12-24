package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface PriceFilter {

  static PriceFilter create(final BigDecimal minPrice,
                            final BigDecimal maxPrice,
                            final BigDecimal tickSize) {
    return new PriceFilterVal(minPrice, maxPrice, tickSize);
  }

  BigDecimal getMinPrice();

  BigDecimal getMaxPrice();

  BigDecimal getTickSize();
}
