package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextCharBufferPredicate<C> {

  boolean apply(final C context, final char[] buf, final int offset, final int len) throws IOException;
}
