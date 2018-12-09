package systems.comodal.jsoniter;

@FunctionalInterface
public interface CharBufferFunction<R> {

  R apply(final int value, final char[] reusableChars);
}
