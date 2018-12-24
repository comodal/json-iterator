package systems.comodal.jsoniter.jmh.data.exchange;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

import static java.time.temporal.ChronoUnit.*;

final class RateLimitVal implements RateLimit {

  private final Type type;
  private final Duration interval;
  private final int limit;

  private RateLimitVal(final Type type, final Duration interval, final int limit) {
    this.type = type;
    this.interval = interval;
    this.limit = limit;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Duration getInterval() {
    return interval;
  }

  @Override
  public int getLimit() {
    return limit;
  }

  static final class RateLimitBuilder implements RateLimit.Builder {

    private Type type;
    private TemporalUnit intervalUnit;
    private long interval;
    private int limit;

    RateLimitBuilder() {
    }

    @Override
    public RateLimit create() {
      return new RateLimitVal(type, Duration.of(interval, intervalUnit), limit);
    }

    @Override
    public Builder type(final String type) {
      this.type = Type.valueOf(type);
      return this;
    }

    @Override
    public Builder intervalUnit(final String intervalUnit) {
      if (intervalUnit.equals("SECOND")) {
        this.intervalUnit = SECONDS;
      } else if (intervalUnit.equals("MINUTE")) {
        this.intervalUnit = MINUTES;
      } else if (intervalUnit.equals("DAY")) {
        this.intervalUnit = DAYS;
      } else {
        throw new IllegalStateException("Unhandled rate limit interval unit " + intervalUnit);
      }
      return this;
    }

    @Override
    public Builder interval(final long interval) {
      this.interval = interval;
      return this;
    }

    @Override
    public Builder limit(final int limit) {
      this.limit = limit;
      return this;
    }
  }
}
