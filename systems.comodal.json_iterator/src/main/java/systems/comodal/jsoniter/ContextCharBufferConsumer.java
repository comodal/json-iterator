package systems.comodal.jsoniter;

@FunctionalInterface
public interface ContextCharBufferConsumer<C> {

  void accept(final C context, final char[] buf, final int offset, final int len);
}
