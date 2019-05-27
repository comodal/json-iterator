package systems.comodal.jsoniter;

@FunctionalInterface
public interface CharBufferPredicate {

  boolean apply(final char[] buf, final int offset, final int len);
}
