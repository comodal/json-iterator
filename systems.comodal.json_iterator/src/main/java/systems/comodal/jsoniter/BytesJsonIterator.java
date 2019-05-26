package systems.comodal.jsoniter;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

class BytesJsonIterator extends BaseJsonIterator {

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
  final char nextToken() throws IOException {
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
          return (char) c;
      }
    }
  }

  @Override
  final char peekToken() throws IOException {
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
          return (char) c;
      }
    }
  }

  byte read() throws IOException {
    return buf[head++];
  }

  @Override
  final char readChar() throws IOException {
    return (char) read();
  }

  @Override
  final int readAsInt() throws IOException {
    return read();
  }

  @Override
  final char peekChar() {
    return (char) buf[head];
  }

  @Override
  final char peekChar(final int offset) {
    return (char) buf[offset];
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
  final int parse() throws IOException {
    byte c; // try fast path first
    int i = head;
    final int bound = Math.min(tail - head, charBuf.length);
    for (int j = 0; j < bound; j++) {
      c = buf[i++];
      if (c == '"') {
        head = i;
        return j;
      }
      // If we encounter a backslash, which is a beginning of an escape sequence
      // or a high bit was set - indicating an UTF-8 encoded multi-byte character,
      // there is no chance that we can decode the string without instantiating
      // a temporary buffer, so quit this loop
      if ((c ^ '\\') < 1) {
        break;
      }
      charBuf[j] = (char) c;
    }
    final int alreadyCopied;
    if (i > head) {
      alreadyCopied = (i - head) - 1;
      head = i - 1;
    } else {
      alreadyCopied = 0;
    }
    return readStringSlowPath(alreadyCopied);
  }

  @Override
  final <R> R parse(final CharBufferFunction<R> applyChars) throws IOException {
    final int len = parse();
    return applyChars.apply(charBuf, 0, len);
  }

  @Override
  final <C, R> R parse(final C context, final ContextCharBufferFunction<C, R> applyChars) throws IOException {
    final int len = parse();
    return applyChars.apply(context, charBuf, 0, len);
  }

  @Override
  final boolean parse(final CharBufferPredicate testChars) throws IOException {
    final int len = parse();
    return testChars.apply(charBuf, 0, len);
  }

  @Override
  final <C> boolean parse(final C context, final ContextCharBufferPredicate<C> testChars) throws IOException {
    final int len = parse();
    return testChars.apply(context, charBuf, 0, len);
  }

  @Override
  final void parse(final CharBufferConsumer testChars) throws IOException {
    final int len = parse();
    testChars.accept(charBuf, 0, len);
  }

  @Override
  final <C> void parse(final C context, final ContextCharBufferConsumer<C> testChars) throws IOException {
    final int len = parse();
    testChars.accept(context, charBuf, 0, len);
  }

  @Override
  final boolean fieldEquals(final String field, final int offset, final int len) {
    return JsonIterator.fieldEquals(field, charBuf, 0, len);
  }

  @Override
  final boolean test(final FieldBufferPredicate fieldBufferFunction, final int offset, final int len) throws IOException {
    return fieldBufferFunction.test(charBuf, 0, len, this);
  }

  @Override
  final <C> boolean test(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction, final int offset, final int len) throws IOException {
    return fieldBufferFunction.test(context, charBuf, 0, len, this);
  }

  @Override
  final <R> R apply(final FieldBufferFunction<R> fieldBufferFunction, final int offset, final int len) throws IOException {
    return fieldBufferFunction.apply(charBuf, 0, len, this);
  }

  @Override
  final <C, R> R apply(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction, final int offset, final int len) throws IOException {
    return fieldBufferFunction.apply(context, charBuf, 0, len, this);
  }

  @Override
  final BigDecimal applyNumberChars(final CharBufferFunction<BigDecimal> parseChars) throws IOException {
    return parseChars.apply(charBuf, 0, parseNumber());
  }

  private int readStringSlowPath(int j) throws IOException {
    try {
      boolean isExpectingLowSurrogate = false;
      for (int bc; ; ) {
        bc = readAsInt();
        if (bc == '"') {
          return j;
        }
        if (bc == '\\') {
          bc = readAsInt();
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
              bc = (JHex.decode(readAsInt()) << 12) + (JHex.decode(readAsInt()) << 8) + (JHex.decode(readAsInt()) << 4) + JHex.decode(readAsInt());
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
              throw reportError("readStringSlowPath", "invalid escape character: " + bc);
          }
        } else if ((bc & 0x80) != 0) {
          final int u2 = readAsInt();
          if ((bc & 0xE0) == 0xC0) {
            bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
          } else {
            final int u3 = readAsInt();
            if ((bc & 0xF0) == 0xE0) {
              bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
            } else {
              final int u4 = readAsInt();
              if ((bc & 0xF8) == 0xF0) {
                bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
              } else {
                throw reportError("readStringSlowPath", "invalid unicode character");
              }
              if (bc >= 0x10000) {
                // check if valid unicode
                if (bc >= 0x110000) {
                  throw reportError("readStringSlowPath", "invalid unicode character");
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
    } catch (final IndexOutOfBoundsException e) {
      throw reportError("readStringSlowPath", "incomplete string");
    }
  }

  @Override
  final String parsedNumberAsString(final int len) {
    return new String(charBuf, 0, len);
  }

  @Override
  final int parseNumber() throws IOException {
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
      final char c = peekChar(i);
      switch (c) {
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
