package systems.comodal.jsoniter;

public interface ProductSymbol {

  static ProductSymbol.Builder build() {
    return new ProductSymbolVal.ProductSymbolBuilder();
  }

  interface Builder {

    ProductSymbol create();
  }
}
