package systems.comodal.jsoniter;

import java.util.List;
import java.util.TimeZone;

public interface ExchangeInfo {

  TimeZone getTimeZone();

  long getServerTime();

  List<RateLimit> getRateLimits();

  static ExchangeInfo.Builder build() {
    return new ExchangeInfoVal.ExchangeInfoBuilder();
  }

  interface Builder extends ExchangeInfo {

    ExchangeInfo create();

    Builder timezone(final String timezone);

    Builder serverTime(final long serverTime);

    Builder rateLimit(final RateLimit rateLimit);
  }
}
