package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface FieldBufferPredicate {

  boolean test(final int len, final char[] buf, final JsonIterator jsonIterator) throws IOException;
}
