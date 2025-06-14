package systems.comodal.jsoniter;

@FunctionalInterface
public interface ContextCharBufferToLongFunction<C> {

  long applyAsLong(final C context, final char[] buf, final int offset, final int len);
}
