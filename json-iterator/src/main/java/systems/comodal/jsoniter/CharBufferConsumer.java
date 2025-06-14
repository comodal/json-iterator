package systems.comodal.jsoniter;

@FunctionalInterface
public interface CharBufferConsumer {

  void accept(final char[] buf, final int offset, final int len);
}
