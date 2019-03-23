package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface FieldBufferFunction<R> {

  R apply(final int len, final char[] buf, final JsonIterator jsonIterator) throws IOException;
}
