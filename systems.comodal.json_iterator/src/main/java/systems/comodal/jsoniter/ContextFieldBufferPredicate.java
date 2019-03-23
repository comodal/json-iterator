package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextFieldBufferPredicate<C> {

  boolean test(final C context, final int len, final char[] buf, final JsonIterator jsonIterator) throws IOException;
}
