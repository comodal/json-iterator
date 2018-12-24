package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface LotSizeFilter {

  static LotSizeFilter create(final BigDecimal minQty,
                              final BigDecimal maxQty,
                              final BigDecimal stepSize) {
    return new LotSizeFilterVal(minQty, maxQty, stepSize);
  }

  BigDecimal getMinQty();

  BigDecimal getMaxQty();

  BigDecimal getStepSize();
}
