package systems.comodal.jsoniter;

@FunctionalInterface
public interface ContextCharBufferPredicate<C> {

  boolean apply(final C context, final char[] buf, final int offset, final int len);
}
