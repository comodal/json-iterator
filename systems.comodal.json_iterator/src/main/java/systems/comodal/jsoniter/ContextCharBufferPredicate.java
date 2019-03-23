package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextCharBufferPredicate<C> {

  boolean apply(final C context, final int len, final char[] buf) throws IOException;
}
