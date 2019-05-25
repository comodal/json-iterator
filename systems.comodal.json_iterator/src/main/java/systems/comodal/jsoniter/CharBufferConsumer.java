package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferConsumer {

  void accept(final char[] buf, final int offset, final int len) throws IOException;
}
