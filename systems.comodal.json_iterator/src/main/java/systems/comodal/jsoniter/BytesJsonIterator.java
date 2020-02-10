package systems.comodal.jsoniter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.math.BigDecimal;
import java.nio.ByteOrder;

class BytesJsonIterator extends BaseJsonIterator {

  private static final VarHandle TO_LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
  private static final long QUOTE_PATTERN = JIUtil.compileReplacePattern((byte) '"');
  private static final long ESCAPE_PATTERN = JIUtil.compileReplacePattern((byte) ('\\' & 0xFF));
  private static final long MULTI_BYTE_CHAR_PATTERN = JIUtil.compileReplacePattern((byte) 0b1000_0000);

  byte[] buf;
  private char[] charBuf;

  BytesJsonIterator(final byte[] buf, final int head, final int tail) {
    this(buf, head, tail, 32);
  }

  BytesJsonIterator(final byte[] buf, final int head, final int tail, final int charBufferLength) {
    super(head, tail);
    this.buf = buf;
    this.charBuf = new char[charBufferLength];
  }

  private static boolean containsPattern(final long input) {
    // https://richardstartin.github.io/posts/finding-bytes
    // Hacker's Delight ch. 6: https://books.google.com/books?id=VicPJYM0I5QC&lpg=PP1&pg=PA117#v=onepage&q&f=false
    return ~(((input & 0x7F7F7F7F7F7F7F7FL) + 0x7F7F7F7F7F7F7F7FL) | input | 0x7F7F7F7F7F7F7F7FL) != 0;
  }

  @Override
  public boolean supportsMarkReset() {
    return true;
  }

  @Override
  public JsonIterator reset(final byte[] buf) {
    this.buf = buf;
    this.head = 0;
    this.tail = buf.length;
    return this;
  }

  @Override
  public JsonIterator reset(final byte[] buf, final int head, final int tail) {
    this.buf = buf;
    this.head = head;
    this.tail = tail;
    return this;
  }

  @Override
  public JsonIterator reset(final char[] buf) {
    return reset(buf, 0, buf.length);
  }

  @Override
  public JsonIterator reset(final char[] buf, final int head, final int tail) {
    return new CharsJsonIterator(buf, head, tail);
  }

  @Override
  public JsonIterator reset(final InputStream in) {
    return new BufferedStreamJsonIterator(in, buf, 0, 0);
  }

  @Override
  public JsonIterator reset(final InputStream in, final int bufSize) {
    return new BufferedStreamJsonIterator(in, buf.length == bufSize ? buf : new byte[bufSize], 0, 0);
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  final String getBufferString(final int from, final int to) {
    return new String(buf, from, Math.min(to, tail) - from);
  }

  @Override
  final char nextToken() {
    byte c;
    for (int i = head; ; ) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("nextToken", "unexpected end");
        }
      }
      c = buf[i++];
      switch (c) {
        case ' ':
        case '\n':
        case '\t':
        case '\r':
          continue;
        default:
          head = i;
          return (char) (c & 0xff);
      }
    }
  }

  @Override
  final char peekToken() {
    byte c;
    for (int i = head; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("peekToken", "unexpected end");
        }
      }
      c = buf[i];
      switch (c) {
        case ' ':
        case '\n':
        case '\t':
        case '\r':
          continue;
        default:
          head = i;
          return (char) (c & 0xff);
      }
    }
  }

  byte read() {
    return buf[head++];
  }

  @Override
  final char readChar() {
    return (char) (read() & 0xff);
  }

  @Override
  final char peekChar() {
    return (char) (buf[head] & 0xff);
  }

  @Override
  final char peekChar(final int offset) {
    return (char) (buf[offset] & 0xff);
  }

  @Override
  final int peekIntDigitChar(final int offset) {
    return INT_DIGITS[buf[offset]];
  }

  private void doubleReusableCharBuffer() {
    final char[] newBuf = new char[charBuf.length << 1];
    System.arraycopy(charBuf, 0, newBuf, 0, charBuf.length);
    charBuf = newBuf;
  }

  @Override
  final int parse() {
    byte c;
    for (int j = 0; head < tail || loadMore(); ) {
      c = buf[head];
      if (c == '"') {
        head++;
        return j;
      } else if ((c ^ '\\') < 1) {
        // If a backslash is encountered, which is a beginning of an escape sequence
        // or a high bit was set - indicating an UTF-8 encoded multi-byte character,
        // there is no chance that we can decode the string without instantiating
        // a temporary buffer, so quit this loop.
        return parseMultiByteString(j);
      } else {
        head++;
        if (j == charBuf.length) {
          doubleReusableCharBuffer();
        }
        charBuf[j++] = (char) (c & 0xff);
      }
    }
    throw reportError("parse", "incomplete string");
  }

  final void skipPastSingleByteEndQuote() {
    for (byte c; head < tail || loadMore(); head++) {
      c = buf[head];
      if (c == '"') {
        head++;
        return;
      } else if ((c ^ '\\') < 1) {
        skipPastMultiByteEndQuote();
        return;
      }
    }
    throw reportError("skipPastSingleByteEndQuote", "incomplete string");
  }

  @Override
  final void skipPastEndQuote() {
    int nextOffset = head + Long.BYTES;
    if (nextOffset > tail) {
      skipPastSingleByteEndQuote();
    } else {
      for (long word, input, tmp; ; ) {
        word = (long) TO_LONG.get(buf, head);
        if (containsPattern(word ^ MULTI_BYTE_CHAR_PATTERN)
            || containsPattern(word ^ ESCAPE_PATTERN)) {
          skipPastMultiByteEndQuote();
          return;
        } else {
          input = word ^ QUOTE_PATTERN;
          tmp = ~(((input & 0x7F7F7F7F7F7F7F7FL) + 0x7F7F7F7F7F7F7F7FL) | input | 0x7F7F7F7F7F7F7F7FL);
          if (tmp != 0) {
            head += (Long.numberOfTrailingZeros(tmp << 1) >>> 3);
            return;
          } else {
            head = nextOffset;
            nextOffset += Long.BYTES;
            if (nextOffset > tail) {
              if (head < tail) {
                head = tail - Long.BYTES;
              } else if (loadMore()) {
                nextOffset = head + Long.BYTES;
                if (nextOffset > tail) {
                  skipPastSingleByteEndQuote();
                  return;
                }
              } else {
                throw reportError("skipPastEndQuote", "incomplete string");
              }
            }
          }
        }
      }
    }
  }

  @Override
  protected final String parseString() {
    int nextOffset = head + Long.BYTES;
    if (nextOffset > tail) {
      final int len = parse();
      return new String(charBuf, 0, len);
    } else {
      long word, input, tmp;
      for (int i = head; ; ) {
        word = (long) TO_LONG.get(buf, i);
        if (containsPattern(word ^ MULTI_BYTE_CHAR_PATTERN)
            || containsPattern(word ^ ESCAPE_PATTERN)) {
          final int len = parseMultiByteString(0);
          return new String(charBuf, 0, len);
        } else {
          input = word ^ QUOTE_PATTERN;
          tmp = ~(((input & 0x7F7F7F7F7F7F7F7FL) + 0x7F7F7F7F7F7F7F7FL) | input | 0x7F7F7F7F7F7F7F7FL);
          if (tmp != 0) {
            i += (Long.numberOfTrailingZeros(tmp << 1) >>> 3);
            final var str = new String(buf, head, (i - 1) - head);
            head = i;
            return str;
          } else {
            i = nextOffset;
            nextOffset += Long.BYTES;
            if (nextOffset > tail) {
              if (i < tail) {
                i = tail - Long.BYTES;
              } else if (supportsMarkReset()) { // Hack to check if reading from stream or not.
                throw reportError("parseString", "incomplete string");
              } else {
                final int len = parseMultiByteString(0);
                return new String(charBuf, 0, len);
              }
            }
          }
        }
      }
    }
  }

  @Override
  final <R> R parse(final CharBufferFunction<R> applyChars) {
    final int len = parse();
    return applyChars.apply(charBuf, 0, len);
  }

  @Override
  final <C, R> R parse(final C context, final ContextCharBufferFunction<C, R> applyChars) {
    final int len = parse();
    return applyChars.apply(context, charBuf, 0, len);
  }

  @Override
  final int parse(final CharBufferToIntFunction applyChars) {
    final int len = parse();
    return applyChars.applyAsInt(charBuf, 0, len);
  }

  @Override
  final <C> int parse(final C context, final ContextCharBufferToIntFunction<C> applyChars) {
    final int len = parse();
    return applyChars.applyAsInt(context, charBuf, 0, len);
  }

  @Override
  final boolean parse(final CharBufferPredicate testChars) {
    final int len = parse();
    return testChars.apply(charBuf, 0, len);
  }

  @Override
  final <C> boolean parse(final C context, final ContextCharBufferPredicate<C> testChars) {
    final int len = parse();
    return testChars.apply(context, charBuf, 0, len);
  }

  @Override
  final void parse(final CharBufferConsumer testChars) {
    final int len = parse();
    testChars.accept(charBuf, 0, len);
  }

  @Override
  final <C> void parse(final C context, final ContextCharBufferConsumer<C> testChars) {
    final int len = parse();
    testChars.accept(context, charBuf, 0, len);
  }

  @Override
  final boolean fieldEquals(final String field, final int offset, final int len) {
    return JsonIterator.fieldEquals(field, charBuf, 0, len);
  }

  @Override
  final boolean breakOut(final FieldBufferPredicate fieldBufferFunction, final int offset, final int len) {
    return !fieldBufferFunction.test(charBuf, 0, len, this);
  }

  @Override
  final <C> boolean breakOut(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction, final int offset, final int len) {
    return !fieldBufferFunction.test(context, charBuf, 0, len, this);
  }

  @Override
  final <C> long test(final C context, final long mask, final ContextFieldBufferMaskedPredicate<C> fieldBufferFunction, final int offset, final int len) {
    return fieldBufferFunction.test(context, mask, charBuf, 0, len, this);
  }

  @Override
  final <R> R apply(final FieldBufferFunction<R> fieldBufferFunction, final int offset, final int len) {
    return fieldBufferFunction.apply(charBuf, 0, len, this);
  }

  @Override
  final <C, R> R apply(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction, final int offset, final int len) {
    return fieldBufferFunction.apply(context, charBuf, 0, len, this);
  }

  @Override
  final BigDecimal parseBigDecimal(final CharBufferFunction<BigDecimal> parseChars) {
    return parseChars.apply(charBuf, 0, parseNumber());
  }

  private int parseMultiByteString(int j) {
    boolean isExpectingLowSurrogate = false;
    for (int bc; head < tail || loadMore(); ) {
      bc = buf[head++];
      if (bc == '"') {
        return j;
      } else if (bc == '\\') {
        if (head == tail && !loadMore()) {
          break;
        }
        bc = buf[head++];
        switch (bc) {
          case 'b':
            bc = '\b';
            break;
          case 't':
            bc = '\t';
            break;
          case 'n':
            bc = '\n';
            break;
          case 'f':
            bc = '\f';
            break;
          case 'r':
            bc = '\r';
            break;
          case '"':
          case '/':
          case '\\':
            break;
          case 'u':
            if (head == tail && !loadMore()) {
              throw reportError("parseMultiByteString", "incomplete string");
            }
            bc = (JHex.decode(buf[head++]) << 12);
            if (head == tail && !loadMore()) {
              throw reportError("parseMultiByteString", "incomplete string");
            }
            bc += (JHex.decode(buf[head++]) << 8);
            if (head == tail && !loadMore()) {
              throw reportError("parseMultiByteString", "incomplete string");
            }
            bc += (JHex.decode(buf[head++]) << 4);
            if (head == tail && !loadMore()) {
              throw reportError("parseMultiByteString", "incomplete string");
            }
            bc += JHex.decode(buf[head++]);
            if (isExpectingLowSurrogate) {
              if (Character.isLowSurrogate((char) bc)) {
                isExpectingLowSurrogate = false;
              } else {
                throw new JsonException("invalid surrogate");
              }
            } else if (Character.isHighSurrogate((char) bc)) {
              isExpectingLowSurrogate = true;
            } else if (Character.isLowSurrogate((char) bc)) {
              throw new JsonException("invalid surrogate");
            }
            break;
          default:
            throw reportError("parseMultiByteString", "invalid escape character: " + bc);
        }
      } else if ((bc & 0x80) != 0) {
        if (head == tail && !loadMore()) {
          break;
        }
        final int u2 = buf[head++];
        if ((bc & 0xE0) == 0xC0) {
          bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
        } else {
          if (head == tail && !loadMore()) {
            break;
          }
          final int u3 = buf[head++];
          if ((bc & 0xF0) == 0xE0) {
            bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
          } else {
            if (head == tail && !loadMore()) {
              break;
            }
            final int u4 = buf[head++];
            if ((bc & 0xF8) == 0xF0) {
              bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
            } else {
              throw reportError("parseMultiByteString", "invalid unicode character");
            }
            if (bc >= 0x10000) {
              // check if valid unicode
              if (bc >= 0x110000) {
                throw reportError("parseMultiByteString", "invalid unicode character");
              }
              // split surrogates
              final int sup = bc - 0x10000;
              if (charBuf.length == j) {
                doubleReusableCharBuffer();
              }
              charBuf[j++] = (char) ((sup >>> 10) + 0xd800);
              if (charBuf.length == j) {
                doubleReusableCharBuffer();
              }
              charBuf[j++] = (char) ((sup & 0x3ff) + 0xdc00);
              continue;
            }
          }
        }
      }
      if (charBuf.length == j) {
        doubleReusableCharBuffer();
      }
      charBuf[j++] = (char) bc;
    }
    throw reportError("parseMultiByteString", "incomplete string");
  }

  private void skipPastMultiByteEndQuote() {
    boolean isExpectingLowSurrogate = false;
    for (int bc; head < tail || loadMore(); ) {
      bc = buf[head++];
      if (bc == '"') {
        return;
      } else if (bc == '\\') {
        if (head == tail && !loadMore()) {
          break;
        }
        bc = buf[head++];
        switch (bc) {
          case 'b':
          case 't':
          case 'n':
          case 'f':
          case 'r':
          case '"':
          case '/':
          case '\\':
            break;
          case 'u':
            if (head == tail && !loadMore()) {
              throw reportError("skipPastMultiByteEndQuote", "incomplete string");
            }
            bc = (JHex.decode(buf[head++]) << 12);
            if (head == tail && !loadMore()) {
              throw reportError("skipPastMultiByteEndQuote", "incomplete string");
            }
            bc += (JHex.decode(buf[head++]) << 8);
            if (head == tail && !loadMore()) {
              throw reportError("skipPastMultiByteEndQuote", "incomplete string");
            }
            bc += (JHex.decode(buf[head++]) << 4);
            if (head == tail && !loadMore()) {
              throw reportError("skipPastMultiByteEndQuote", "incomplete string");
            }
            bc += JHex.decode(buf[head++]);
            if (isExpectingLowSurrogate) {
              if (Character.isLowSurrogate((char) bc)) {
                isExpectingLowSurrogate = false;
              } else {
                throw new JsonException("invalid surrogate");
              }
            } else if (Character.isHighSurrogate((char) bc)) {
              isExpectingLowSurrogate = true;
            } else if (Character.isLowSurrogate((char) bc)) {
              throw new JsonException("invalid surrogate");
            }
            break;
          default:
            throw reportError("skipPastMultiByteEndQuote", "invalid escape character: " + bc);
        }
      } else if ((bc & 0x80) != 0) {
        if (head == tail && !loadMore()) {
          break;
        }
        final int u2 = buf[head++];
        if ((bc & 0xE0) != 0xC0) {
          if (head == tail && !loadMore()) {
            break;
          }
          final int u3 = buf[head++];
          if ((bc & 0xF0) != 0xE0) {
            if (head == tail && !loadMore()) {
              break;
            }
            final int u4 = buf[head++];
            if ((bc & 0xF8) == 0xF0) {
              bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
            } else {
              throw reportError("skipPastMultiByteEndQuote", "invalid unicode character");
            }
            if (bc >= 0x10000) {
              // check if valid unicode
              if (bc >= 0x110000) {
                throw reportError("skipPastMultiByteEndQuote", "invalid unicode character");
              }
            }
          }
        }
      }
    }
    throw reportError("skipPastMultiByteEndQuote", "incomplete string");
  }

  @Override
  final String parsedNumberAsString(final int len) {
    return new String(charBuf, 0, len);
  }

  @Override
  final <R> R parseNumber(final CharBufferFunction<R> applyChars, final int len) {
    return applyChars.apply(charBuf, 0, len);
  }

  @Override
  final <C, R> R parseNumber(final C context,
                             final ContextCharBufferFunction<C, R> applyChars,
                             final int len) {
    return applyChars.apply(context, charBuf, 0, len);
  }

  @Override
  final int parseNumber(final CharBufferToIntFunction applyChars, final int len) {
    return applyChars.applyAsInt(charBuf, 0, len);
  }

  @Override
  final <C> int parseNumber(final C context, final ContextCharBufferToIntFunction<C> applyChars, final int len) {
    return applyChars.applyAsInt(context, charBuf, 0, len);
  }

  @Override
  final int parseNumber() {
    char c;
    for (int i = head, len = 0; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          head = tail;
          return len;
        }
      }
      if (len == charBuf.length) {
        doubleReusableCharBuffer();
      }
      switch ((c = peekChar(i))) {
        case ' ':
          continue;
        case '.':
        case 'e':
        case 'E':
          // dot found
        case '-':
        case '+':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          charBuf[len++] = c;
          continue;
        default:
          head = i;
          return len;
      }
    }
  }
}
