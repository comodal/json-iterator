package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

final class LotSizeFilterVal implements LotSizeFilter {

  private final BigDecimal minQty;
  private final BigDecimal maxQty;
  private final BigDecimal stepSize;

  LotSizeFilterVal(final BigDecimal minQty,
                   final BigDecimal maxQty,
                   final BigDecimal stepSize) {
    this.minQty = minQty;
    this.maxQty = maxQty;
    this.stepSize = stepSize;
  }

  @Override
  public BigDecimal getMinQty() {
    return minQty;
  }

  @Override
  public BigDecimal getMaxQty() {
    return maxQty;
  }

  @Override
  public BigDecimal getStepSize() {
    return stepSize;
  }
}
