package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface PriceFilter {

  static PriceFilter create(final BigDecimal minPrice,
                            final BigDecimal maxPrice,
                            final BigDecimal tickSize) {
    return new PriceFilterVal(minPrice, maxPrice, tickSize);
  }

  static PriceFilter.Builder build() {
    return new PriceFilterVal.PriceFilterBuilder();
  }

  BigDecimal getMinPrice();

  BigDecimal getMaxPrice();

  BigDecimal getTickSize();

  interface Builder {

    PriceFilter create();

    Builder minPrice(final BigDecimal minPrice);

    Builder maxPrice(final BigDecimal maxPrice);

    Builder tickSize(final BigDecimal tickSize);
  }
}
