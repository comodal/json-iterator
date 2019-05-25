package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextFieldBufferPredicate<C> {

  boolean test(final C context, final char[] buf, final int offset, final int len, final JsonIterator jsonIterator) throws IOException;
}
