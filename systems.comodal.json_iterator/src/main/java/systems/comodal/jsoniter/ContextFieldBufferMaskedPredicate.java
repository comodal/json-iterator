package systems.comodal.jsoniter;

public interface ContextFieldBufferMaskedPredicate<C> {

  long test(final C context, final long mask, final char[] buf, final int offset, final int len, final JsonIterator jsonIterator);
}

