package systems.comodal.jsoniter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

final class ExchangeInfoVal implements ExchangeInfo {

  private final TimeZone timeZone;
  private final long serverTime;
  private final List<RateLimit> rateLimits;
  private final List<ProductSymbol> productSymbols;

  private ExchangeInfoVal(final TimeZone timeZone,
                          final long serverTime,
                          final List<RateLimit> rateLimits,
                          final List<ProductSymbol> productSymbols) {
    this.timeZone = timeZone;
    this.serverTime = serverTime;
    this.rateLimits = rateLimits;
    this.productSymbols = productSymbols;
  }

  @Override
  public TimeZone getTimeZone() {
    return timeZone;
  }

  @Override
  public long getServerTime() {
    return serverTime;
  }

  @Override
  public List<RateLimit> getRateLimits() {
    return rateLimits;
  }

  @Override
  public List<ProductSymbol> getProductSymbols() {
    return productSymbols;
  }

  static final class ExchangeInfoBuilder implements ExchangeInfo.Builder {

    private TimeZone timeZone;
    private long serverTime;
    private List<RateLimit> rateLimits;
    private List<ProductSymbol> productSymbols;

    ExchangeInfoBuilder() {
    }

    @Override
    public ExchangeInfo create() {
      return new ExchangeInfoVal(timeZone, serverTime,
          rateLimits == null ? List.of() : rateLimits,
          productSymbols == null ? List.of() : productSymbols);
    }

    @Override
    public Builder timezone(final String timezone) {
      this.timeZone = TimeZone.getTimeZone(timezone);
      return this;
    }

    @Override
    public Builder serverTime(final long serverTime) {
      this.serverTime = serverTime;
      return this;
    }

    @Override
    public Builder rateLimit(final RateLimit rateLimit) {
      if (rateLimits == null) {
        this.rateLimits = new ArrayList<>();
      }
      rateLimits.add(rateLimit);
      return this;
    }

    @Override
    public Builder productSymbol(final ProductSymbol productSymbol) {
      if (productSymbols == null) {
        this.productSymbols = new ArrayList<>();
      }
      productSymbols.add(productSymbol);
      return this;
    }

    @Override
    public TimeZone getTimeZone() {
      return timeZone;
    }

    @Override
    public long getServerTime() {
      return serverTime;
    }

    @Override
    public List<RateLimit> getRateLimits() {
      return rateLimits;
    }

    @Override
    public List<ProductSymbol> getProductSymbols() {
      return productSymbols;
    }
  }
}
