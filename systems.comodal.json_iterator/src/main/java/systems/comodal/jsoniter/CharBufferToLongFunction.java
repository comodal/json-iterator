package systems.comodal.jsoniter;

@FunctionalInterface
public interface CharBufferToLongFunction {

  long applyAsLong(final char[] buf, final int offset, final int len);
}
