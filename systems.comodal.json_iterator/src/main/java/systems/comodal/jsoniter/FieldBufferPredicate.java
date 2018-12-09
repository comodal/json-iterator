package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface FieldBufferPredicate<C> {

  boolean apply(final C context, final int len, final char[] buf, final JsonIterator jsonIterator) throws IOException;
}
