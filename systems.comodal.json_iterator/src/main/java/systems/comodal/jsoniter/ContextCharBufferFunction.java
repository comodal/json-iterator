package systems.comodal.jsoniter;

@FunctionalInterface
public interface ContextCharBufferFunction<C, R> {

  R apply(final C context, final char[] buf, final int offset, final int len);
}
