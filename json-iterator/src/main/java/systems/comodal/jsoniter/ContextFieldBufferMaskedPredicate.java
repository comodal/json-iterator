package systems.comodal.jsoniter;

public interface ContextFieldBufferMaskedPredicate<C> {

  long BREAK_OUT = 0xffffffff_ffffffffL;

  long test(final C context, final long mask, final char[] buf, final int offset, final int len, final JsonIterator jsonIterator);
}

