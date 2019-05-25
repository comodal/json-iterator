package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface FieldBufferFunction<R> {

  R apply(final char[] buf, final int offset, final int len, final JsonIterator jsonIterator) throws IOException;
}
