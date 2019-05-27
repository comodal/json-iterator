package systems.comodal.jsoniter;

@FunctionalInterface
public interface CharBufferFunction<R> {

  R apply(final char[] buf, final int offset, final int len);
}
