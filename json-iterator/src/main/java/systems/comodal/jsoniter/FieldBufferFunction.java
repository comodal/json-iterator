package systems.comodal.jsoniter;

@FunctionalInterface
public interface FieldBufferFunction<R> {

  R apply(final char[] buf, final int offset, final int len, final JsonIterator jsonIterator);
}
