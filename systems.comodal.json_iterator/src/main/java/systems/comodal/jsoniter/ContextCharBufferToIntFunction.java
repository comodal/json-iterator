package systems.comodal.jsoniter;

@FunctionalInterface
public interface ContextCharBufferToIntFunction<C> {

  int applyAsInt(final C context, final char[] buf, final int offset, final int len);
}
