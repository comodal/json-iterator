package systems.comodal.jsoniter;

import java.io.IOException;

@FunctionalInterface
public interface CharBufferFunction<R> {

  R apply(final int value, final char[] reusableChars) throws IOException;
}
