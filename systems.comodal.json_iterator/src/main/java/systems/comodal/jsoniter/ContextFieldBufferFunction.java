package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextFieldBufferFunction<C, R> {

  R apply(final C context, final char[] buf, final int offset, final int len, final JsonIterator jsonIterator) throws IOException;
}
