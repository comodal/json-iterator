package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextCharBufferConsumer<C> {

  void accept(final C context, final int len, final char[] buf) throws IOException;
}
