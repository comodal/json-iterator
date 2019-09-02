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

  static final class LotSizeFilterBuilder implements Builder {

    private BigDecimal minQty;
    private BigDecimal maxQty;
    private BigDecimal stepSize;

    LotSizeFilterBuilder() {
    }

    @Override
    public LotSizeFilter create() {
      return new LotSizeFilterVal(minQty, maxQty, stepSize);
    }

    @Override
    public Builder minQty(final BigDecimal minQty) {
      this.minQty = minQty;
      return this;
    }

    @Override
    public Builder maxQty(final BigDecimal maxQty) {
      this.maxQty = maxQty;
      return this;
    }

    @Override
    public Builder stepSize(final BigDecimal stepSize) {
      this.stepSize = stepSize;
      return this;
    }
  }
}
