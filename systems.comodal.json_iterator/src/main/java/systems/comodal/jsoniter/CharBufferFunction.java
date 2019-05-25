package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferFunction<R> {

  R apply(final char[] buf, final int offset, final int len) throws IOException;
}
