package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface ContextFieldBufferFunction<C, R> {

  R apply(final C context, final int len, final char[] buf, final JsonIterator jsonIterator) throws IOException;
}
