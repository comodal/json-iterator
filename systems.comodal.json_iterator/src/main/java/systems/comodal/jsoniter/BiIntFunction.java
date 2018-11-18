package systems.comodal.jsoniter;

@FunctionalInterface
public interface BiIntFunction<T, R> {

  R apply(final int value, final T input);
}
