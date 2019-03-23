package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferPredicate {

  boolean apply(final int len, final char[] buf) throws IOException;
}
