package systems.comodal.jsoniter.jmh.data.exchange;

import java.math.BigDecimal;

public interface Filter {

  static Filter.Builder build() {
    return new FilterBuilder();
  }

  interface Builder {

    Builder build(final ProductSymbol.Builder builder);

    Builder type(final String type);

    Builder minPrice(final BigDecimal minPrice);

    Builder maxPrice(final BigDecimal maxPrice);

    Builder tickSize(final BigDecimal tickSize);

    Builder multiplierUp(final BigDecimal multiplierUp);

    Builder multiplierDown(final BigDecimal multiplierDown);

    Builder avgPriceMins(final int avgPriceMins);

    Builder minQty(final BigDecimal minQty);

    Builder maxQty(final BigDecimal maxQty);

    Builder stepSize(final BigDecimal stepSize);

    Builder minNotional(final BigDecimal minNotional);

    Builder applyToMarket(final boolean applyToMarket);

    Builder limit(final int limit);

    Builder maxNumAlgoOrders(final int maxNumAlgoOrders);
  }
}
