package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferPredicate {

  boolean apply(final char[] buf, final int offset, final int len) throws IOException;
}
