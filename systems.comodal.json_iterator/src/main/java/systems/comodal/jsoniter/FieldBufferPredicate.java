package systems.comodal.jsoniter;

@FunctionalInterface
public interface FieldBufferPredicate {

  boolean test(final char[] buf, final int offset, final int len, final JsonIterator jsonIterator);
}
