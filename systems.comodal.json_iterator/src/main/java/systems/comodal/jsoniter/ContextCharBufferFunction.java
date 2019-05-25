package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextCharBufferFunction<C, R> {

  R apply(final C context, final char[] buf, final int offset, final int len) throws IOException;
}
