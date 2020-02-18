package systems.comodal.jsoniter;

import java.io.Closeable;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

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

  static JsonIterator parse(final String field) {
    return parse(field.getBytes());
  }

  static JsonIterator parse(final String field, final int charBufferLength) {
    return parse(field.getBytes(), charBufferLength);
  }

  static boolean fieldEquals(final String field, final char[] buf) {
    return fieldEquals(field, buf, 0, buf.length);
  }

  static boolean fieldEquals(final String field, final char[] buf, final int len) {
    return fieldEquals(field, buf, 0, len);
  }

  static boolean fieldEquals(final String field, final char[] buf, final int offset, final int len) {
    if (field.length() != len) {
      return false;
    }
    for (int i = 0, j = offset; i < len; i++, j++) {
      if (field.charAt(i) != buf[j]) {
        return false;
      }
    }
    return true;
  }

  static boolean fieldStartsWith(final String field, final char[] buf, final int offset, final int len) {
    final int to = field.length();
    if (to > len) {
      return false;
    }
    for (int i = 0, j = offset; i < to; i++, j++) {
      if (field.charAt(i) != buf[j]) {
        return false;
      }
    }
    return true;
  }

  static boolean fieldEqualsIgnoreCase(final String field, final char[] buf) {
    return fieldEqualsIgnoreCase(field, buf, 0, buf.length);
  }

  static boolean fieldEqualsIgnoreCase(final String field, final char[] buf, final int len) {
    return fieldEqualsIgnoreCase(field, buf, 0, len);
  }

  static boolean fieldEqualsIgnoreCase(final String field, final char[] buf, final int offset, final int len) {
    if (field.length() != len) {
      return false;
    }
    for (int i = 0, j = offset, c, d; i < len; i++, j++) {
      c = field.charAt(i);
      d = buf[j];
      if (c != d
          && Character.toLowerCase(c) != d
          && Character.toUpperCase(c) != d) {
        return false;
      }
    }
    return true;
  }

  static boolean fieldStartsWithIgnoreCase(final String field, final char[] buf, final int offset, final int len) {
    final int to = field.length();
    if (to > len) {
      return false;
    }
    for (int i = 0, j = offset, c, d; i < to; i++, j++) {
      c = field.charAt(i);
      d = buf[j];
      if (c != d
          && Character.toLowerCase(c) != d
          && Character.toUpperCase(c) != d) {
        return false;
      }
    }
    return true;
  }

  JsonIterator reset(final int mark);

  JsonIterator reset(final byte[] buf);

  JsonIterator reset(final byte[] buf, final int head, final int tail);

  JsonIterator reset(final char[] buf);

  JsonIterator reset(final char[] buf, final int head, final int tail);

  JsonIterator reset(final InputStream in);

  JsonIterator reset(final InputStream in, final int bufSize);

  String currentBuffer();

  // Object Field & Navigation Methods

  boolean supportsMarkReset();

  int mark();

  ValueType whatIsNext();

  boolean readArray();

  JsonIterator openArray();

  JsonIterator continueArray();

  JsonIterator closeArray();

  default String readObject() {
    return readObjField();
  }

  String readObjField();

  JsonIterator skipObjField();

  JsonIterator skipUntil(final String field);

  JsonIterator closeObj();

  // Value Methods

  JsonIterator skip();

  default JsonIterator skipRestOfObject() {
    while (skipObjField() != null) {
      skip();
    }
    return this;
  }

  default JsonIterator skipRestOfArray() {
    while (readArray()) {
      skip();
    }
    return this;
  }

  /**
   * Advances the iterator if the next item is 'null' and returns true.
   * Otherwise, stays in place and returns false.
   *
   * @return true if value was 'null'.
   */
  boolean readNull();

  String readString();

  String readNumberAsString();

  String readNumberOrNumberString();

  boolean readBoolean();

  short readShort();

  int readInt();

  long readLong();

  float readFloat();

  double readDouble();

  BigDecimal readBigDecimal();

  @Deprecated
  default BigDecimal readBigDecimalStripTrailingZeroes() {
    return readBigDecimalDropZeroes();
  }

  /**
   * Drops trailing decimal zeroes.
   */
  BigDecimal readBigDecimalDropZeroes();

  long readUnscaledAsLong(final int scale);

  BigInteger readBigInteger();

  /**
   * Parses ISO-like or RFC_1123_DATE_TIME formats such as:
   * - YYYY*MM*DD*HH*MM*SS.?\d{0,9}Z?
   * - YYYY*MM*DD*HH*MM*SS[+-]HH*MM
   * - Tue, 3 Jun 2008 11:05:30 GMT
   * <p>
   * Defaults to UTC if no offset is provided.
   *
   * @return the parsed Instant
   * @throws java.time.DateTimeException - on any unexpected character or length
   */
  Instant readDateTime();

  // IOC Field Value Methods

  /**
   * Construct an Object of type R directly from the char[] representing the next String value.
   * <p>
   * The function is not called for null values, instead null is directly returned.
   *
   * @param applyChars This array buffer is reused throughout the life of this iterator.
   * @param <R>        Resulting Object Type.
   * @return Object constructed from applyChars.
   */
  <R> R applyChars(final CharBufferFunction<R> applyChars);

  <C, R> R applyChars(final C context, final ContextCharBufferFunction<C, R> applyChars);

  <R> R applyNumberChars(final CharBufferFunction<R> applyChars);

  <C, R> R applyNumberChars(final C context, final ContextCharBufferFunction<C, R> applyChars);

  int applyCharsAsInt(final CharBufferToIntFunction applyChars);

  <C> int applyCharsAsInt(final C context, final ContextCharBufferToIntFunction<C> applyChars);

  int applyNumberCharsAsInt(final CharBufferToIntFunction applyChars);

  <C> int applyNumberCharsAsInt(final C context, final ContextCharBufferToIntFunction<C> applyChars);

  long applyCharsAsLong(final CharBufferToLongFunction applyChars);

  <C> long applyCharsAsLong(final C context, final ContextCharBufferToLongFunction<C> applyChars);

  long applyNumberCharsAsLong(final CharBufferToLongFunction applyChars);

  <C> long applyNumberCharsAsLong(final C context, final ContextCharBufferToLongFunction<C> applyChars);

  boolean testChars(final CharBufferPredicate testChars);

  <C> boolean testChars(final C context, final ContextCharBufferPredicate<C> testChars);

  void consumeChars(final CharBufferConsumer testChars);

  <C> void consumeChars(final C context, final ContextCharBufferConsumer<C> testChars);

  // IOC Field Methods

  boolean testObjField(final CharBufferPredicate testField);

  <C, R> R applyObject(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction);

  <R> R applyObject(final FieldBufferFunction<R> fieldBufferFunction);

  <C> C testObject(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction);

  void testObject(final FieldBufferPredicate fieldBufferFunction);

  <C> C testObject(final C context, final ContextFieldBufferMaskedPredicate<C> fieldBufferFunction);
}
