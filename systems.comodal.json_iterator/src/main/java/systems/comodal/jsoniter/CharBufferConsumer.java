package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferConsumer {

  void accept(final int len, final char[] buf) throws IOException;
}
