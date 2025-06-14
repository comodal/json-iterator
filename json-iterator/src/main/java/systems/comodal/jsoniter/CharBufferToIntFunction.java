package systems.comodal.jsoniter;

@FunctionalInterface
public interface CharBufferToIntFunction {

  int applyAsInt(final char[] buf, final int offset, final int len);
}
