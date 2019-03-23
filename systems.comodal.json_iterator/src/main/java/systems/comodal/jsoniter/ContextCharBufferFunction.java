package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextCharBufferFunction<C, R> {

  R apply(final C context, final int len, final char[] buf) throws IOException;
}
