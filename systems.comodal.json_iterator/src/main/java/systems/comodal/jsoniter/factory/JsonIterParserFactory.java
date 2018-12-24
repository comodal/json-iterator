package systems.comodal.jsoniter.factory;

import java.util.ServiceLoader;

public interface JsonIterParserFactory<T> {

  static <T> JsonIterParser<T> loadParser(final Class<? extends T> parserType) {
    return loadParser(parserType, parserType.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  static <T> JsonIterParser<T> loadParser(final Class<? extends T> parserType, final String prefixFilter) {
    return (JsonIterParser<T>) ServiceLoader.load(JsonIterParserFactory.class).stream()
        .filter(factory -> factory.type().getSimpleName().startsWith(prefixFilter))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No parser factory found filter by name beginning with " + prefixFilter))
        .get().create(parserType);
  }

  JsonIterParser<T> create(final Class<T> parserType);
}
