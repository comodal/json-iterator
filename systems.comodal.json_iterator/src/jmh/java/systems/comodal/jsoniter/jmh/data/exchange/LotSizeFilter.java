package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface LotSizeFilter {

  static LotSizeFilter create(final BigDecimal minQty,
                              final BigDecimal maxQty,
                              final BigDecimal stepSize) {
    return new LotSizeFilterVal(minQty, maxQty, stepSize);
  }

  static LotSizeFilter.Builder build() {
    return new LotSizeFilterVal.LotSizeFilterBuilder();
  }

  BigDecimal getMinQty();

  BigDecimal getMaxQty();

  BigDecimal getStepSize();

  interface Builder {

    LotSizeFilter create();

    Builder minQty(final BigDecimal minQty);

    Builder maxQty(final BigDecimal maxQty);

    Builder stepSize(final BigDecimal stepSize);
  }
}
