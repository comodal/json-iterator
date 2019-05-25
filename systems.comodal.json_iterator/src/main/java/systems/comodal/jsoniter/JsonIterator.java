package systems.comodal.jsoniter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface JsonIterator extends Closeable {

  static JsonIterator parse(final InputStream in, final int bufSize) {
    return new BufferedStreamJsonIterator(in, new byte[bufSize], 0, 0);
  }

  static JsonIterator parse(final InputStream in, final int bufSize, final int charBufferLength) {
    return new BufferedStreamJsonIterator(in, new byte[bufSize], 0, 0, charBufferLength);
  }

  static JsonIterator parse(final byte[] buf) {
    return new BytesJsonIterator(buf, 0, buf.length);
  }

  static JsonIterator parse(final byte[] buf, final int charBufferLength) {
    return new BytesJsonIterator(buf, 0, buf.length, charBufferLength);
  }

  static JsonIterator parse(final byte[] buf, final int head, final int tail) {
    return new BytesJsonIterator(buf, head, tail);
  }

  static JsonIterator parse(final byte[] buf, final int head, final int tail, final int charBufferLength) {
    return new BytesJsonIterator(buf, head, tail, charBufferLength);
  }

  static JsonIterator parse(final char[] buf) {
    return new CharsJsonIterator(buf, 0, buf.length);
  }

  static JsonIterator parse(final char[] buf, final int head, final int tail) {
    return new CharsJsonIterator(buf, head, tail);
  }

  static JsonIterator parse(final String str) {
    return parse(str.getBytes());
  }

  static JsonIterator parse(final String str, final int charBufferLength) {
    return parse(str.getBytes(), charBufferLength);
  }

  static boolean fieldEquals(final String str, final char[] buf) {
    return fieldEquals(str, buf, 0, buf.length);
  }

  static boolean fieldEquals(final String str, final char[] buf, final int len) {
    return fieldEquals(str, buf, 0, len);
  }

  static boolean fieldEquals(final String str, final char[] buf, final int offset, final int len) {
    if (str.length() != len) {
      return false;
    }
    for (int i = 0, j = offset; i < len; i++, j++) {
      if (str.charAt(i) != buf[j]) {
        return false;
      }
    }
    return true;
  }

  boolean supportsMarkReset();

  int mark();

  JsonIterator reset(final int mark);

  JsonIterator reset(final byte[] buf);

  JsonIterator reset(final byte[] buf, final int head, final int tail);

  JsonIterator reset(final char[] buf);

  JsonIterator reset(final char[] buf, final int head, final int tail);

  JsonIterator reset(final InputStream in);

  JsonIterator reset(final InputStream in, final int bufSize);

  String currentBuffer();

  ValueType whatIsNext() throws IOException;

  JsonIterator skip() throws IOException;

  boolean readNull() throws IOException;

  boolean readBoolean() throws IOException;

  short readShort() throws IOException;

  int readInt() throws IOException;

  long readLong() throws IOException;

  boolean readArray() throws IOException;

  JsonIterator openArray() throws IOException;

  JsonIterator continueArray() throws IOException;

  JsonIterator closeArray() throws IOException;

  String readNumberAsString() throws IOException;

  String readString() throws IOException;

  /**
   * Construct an Object of your choice directly from the char[] representing the next String value.
   *
   * @param applyChars This array buffer is reused throughout the life of this iterator.
   * @param <R>        Resulting Object Type.
   * @return Object constructed from applyChars.
   * @throws IOException needed in case the underlying data is a stream.
   */
  <R> R applyChars(final CharBufferFunction<R> applyChars) throws IOException;

  <C, R> R applyChars(final C context, final ContextCharBufferFunction<C, R> applyChars) throws IOException;

  boolean testChars(final CharBufferPredicate testChars) throws IOException;

  <C> boolean testChars(final C context, final ContextCharBufferPredicate<C> testChars) throws IOException;

  void consumeChars(final CharBufferConsumer testChars) throws IOException;

  <C> void consumeChars(final C context, final ContextCharBufferConsumer<C> testChars) throws IOException;

  JsonIterator skipUntil(final String field) throws IOException;

  default String readObject() throws IOException {
    return readObjField();
  }

  String readObjField() throws IOException;

  boolean testObjField(final CharBufferPredicate testField) throws IOException;

  JsonIterator skipObjField() throws IOException;

  JsonIterator closeObj() throws IOException;

  <C, R> R applyObject(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction) throws IOException;

  <R> R applyObject(final FieldBufferFunction<R> fieldBufferFunction) throws IOException;

  <C> C testObject(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction) throws IOException;

  void testObject(final FieldBufferPredicate fieldBufferFunction) throws IOException;

  float readFloat() throws IOException;

  double readDouble() throws IOException;

  BigDecimal readBigDecimal() throws IOException;

  BigDecimal readBigDecimalStripTrailingZeroes() throws IOException;

  BigInteger readBigInteger() throws IOException;
}
