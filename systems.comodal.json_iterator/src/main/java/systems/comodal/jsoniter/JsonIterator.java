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

  static JsonIterator parse(final byte[] buf) {
    return new BytesJsonIterator(buf, 0, buf.length);
  }

  static JsonIterator parse(final byte[] buf, final int head, final int tail) {
    return new BytesJsonIterator(buf, head, tail);
  }

  static JsonIterator parse(final String str) {
    return parse(str.getBytes());
  }

  boolean supportsMarkReset();

  int mark();

  JsonIterator reset(final int mark);

  JsonIterator reset(final byte[] buf);

  JsonIterator reset(final byte[] buf, final int head, final int tail);

  JsonIterator reset(final InputStream in);

  JsonIterator reset(final InputStream in, final int bufSize);

  String currentBuffer();

  ValueType whatIsNext() throws IOException;

  JsonIterator skip() throws IOException;

  Object read() throws IOException;

  boolean readNull() throws IOException;

  boolean readBoolean() throws IOException;

  short readShort() throws IOException;

  int readInt() throws IOException;

  long readLong() throws IOException;

  boolean readArray() throws IOException;

  String readNumberAsString() throws IOException;

  String readString() throws IOException;

  /**
   * Construct an Object of your choice directly from the char[] representing the next String value.
   *
   * @param applyChars This array buffer is reused throughout the life of this iterator.
   * @param <T>        Resulting Object Type.
   * @return Object constructed from applyChars.
   * @throws IOException needed in case the underlying data is a stream.
   */
  <T> T readChars(final BiIntFunction<char[], T> applyChars) throws IOException;

  default String readObject() throws IOException {
    return readObjField();
  }

  String readObjField() throws IOException;

  float readFloat() throws IOException;

  double readDouble() throws IOException;

  BigDecimal readBigDecimal() throws IOException;

  BigDecimal readBigDecimalStripTrailingZeroes() throws IOException;

  BigInteger readBigInteger() throws IOException;
}
