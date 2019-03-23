package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferFunction<R> {

  R apply(final int len, final char[] buf) throws IOException;
}
