package systems.comodal.jsoniter;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static systems.comodal.jsoniter.ValueType.*;

class BytesJsonIterator implements JsonIterator {

  private static final long[] POW10 = {
      1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
      1000000000, 10000000000L, 100000000000L, 1000000000000L,
      10000000000000L, 100000000000000L, 1000000000000000L};

  static final int[] INT_DIGITS = new int[127];
  static final int INVALID_CHAR_FOR_NUMBER = -1;

  static {
    Arrays.fill(INT_DIGITS, INVALID_CHAR_FOR_NUMBER);
    for (int i = '0'; i <= '9'; ++i) {
      INT_DIGITS[i] = (i - '0');
    }
  }

  byte[] buf;
  int head;
  int tail;

  char[] reusableChars;

  BytesJsonIterator(final byte[] buf, final int head, final int tail) {
    this.buf = buf;
    this.head = head;
    this.tail = tail;
    this.reusableChars = new char[32];
  }

  @Override
  public boolean supportsMarkReset() {
    return true;
  }

  @Override
  public int mark() {
    return head;
  }

  @Override
  public JsonIterator reset(final int mark) {
    this.head = mark;
    return this;
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

  final void unreadByte() {
    if (head == 0) {
      throw reportError("unreadByte", "unread too many bytes");
    }
    head--;
  }

  final JsonException reportError(final String op, final String msg) {
    int peekStart = head - 10;
    if (peekStart < 0) {
      peekStart = 0;
    }
    int peekSize = head - peekStart;
    if (head > tail) {
      peekSize = tail - peekStart;
    }
    final String peek = new String(buf, peekStart, peekSize);
    throw new JsonException(op + ": " + msg + ", head: " + head + ", peek: " + peek + ", buf: " + new String(buf));
  }

  @Override
  public String currentBuffer() {
    int peekStart = head - 10;
    if (peekStart < 0) {
      peekStart = 0;
    }
    final var peek = new String(buf, peekStart, head - peekStart);
    return "head: " + head + ", peek: " + peek + ", buf: " + new String(buf);
  }

  @Override
  public final boolean readNull() throws IOException {
    final byte c = nextToken();
    if (c != 'n') {
      unreadByte();
      return false;
    }
    skipFixedBytes(3); // null
    return true;
  }

  @Override
  public final boolean readBoolean() throws IOException {
    final byte c = nextToken();
    if ('t' == c) {
      skipFixedBytes(3); // true
      return true;
    }
    if ('f' == c) {
      skipFixedBytes(4); // false
      return false;
    }
    throw reportError("readBoolean", "expect t or f, found: " + c);
  }

  @Override
  public final short readShort() throws IOException {
    final int v = readInt();
    if (Short.MIN_VALUE <= v && v <= Short.MAX_VALUE) {
      return (short) v;
    }
    throw reportError("readShort", "short overflow: " + v);
  }

  @Override
  public final int readInt() throws IOException {
    final byte c = nextToken();
    if (c == '-') {
      return readInt(readByte());
    }
    final int val = readInt(c);
    if (val == Integer.MIN_VALUE) {
      throw reportError("readInt", "value is too large for int");
    }
    return -val;
  }

  @Override
  public final long readLong() throws IOException {
    byte c = nextToken();
    if (c == '-') {
      c = readByte();
      if (INT_DIGITS[c] == 0) {
        assertNotLeadingZero();
        return 0;
      }
      return readLong(c);
    }
    if (INT_DIGITS[c] == 0) {
      assertNotLeadingZero();
      return 0;
    }
    final long val = readLong(c);
    if (val == Long.MIN_VALUE) {
      throw reportError("readLong", "value is too large for long");
    }
    return -val;
  }

  @Override
  public final boolean readArray() throws IOException {
    final byte c = nextToken();
    if (c == '[') {
      if (nextToken() == ']') {
        return false;
      }
      unreadByte();
      return true;
    }
    if (c == ']' || c == 'n') {
      return false;
    }
    if (c == ',') {
      return true;
    }
    throw reportError("readArray", "expect [ or , or n or ], but found: " + (char) c);
  }

  public final JsonIterator openArray() throws IOException {
    final byte c = nextToken();
    if (c == '[') {
      return this;
    }
    throw reportError("readArray", "expected '[' but found: " + (char) c);
  }

  public final JsonIterator continueArray() throws IOException {
    final byte c = nextToken();
    if (c == ',') {
      return this;
    }
    throw reportError("readArray", "expected ',' but found: " + (char) c);
  }

  public final JsonIterator closeArray() throws IOException {
    final byte c = nextToken();
    if (c == ']') {
      return this;
    }
    throw reportError("readArray", "expected ']' but found: " + (char) c);
  }

  @Override
  public final String readNumberAsString() throws IOException {
    final var numberChars = readNumber();
    return new String(numberChars.chars, 0, numberChars.charsLength);
  }

  static final class NumberChars {

    final char[] chars;
    final int charsLength;
    final boolean dotFound;

    NumberChars(final char[] chars, final int charsLength, final boolean dotFound) {
      this.chars = chars;
      this.charsLength = charsLength;
      this.dotFound = dotFound;
    }
  }

  NumberChars readNumber() throws IOException {
    int j = 0;
    boolean dotFound = false;
    for (int i = head; i < tail; i++) {
      if (j == reusableChars.length) {
        doubleReusableCharBuffer();
      }
      final byte c = buf[i];
      switch (c) {
        case ' ':
          continue;
        case '.':
        case 'e':
        case 'E':
          dotFound = true;
          // fallthrough
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
          reusableChars[j++] = (char) c;
          break;
        default:
          head = i;
          return new NumberChars(reusableChars, j, dotFound);
      }
    }
    head = tail;
    return new NumberChars(reusableChars, j, dotFound);
  }

  private void readArrayCB(final List<Object> attachment) throws IOException {
    byte c = nextToken();
    if (c == '[') {
      c = nextToken();
      if (c != ']') {
        unreadByte();
        do {
          attachment.add(read());
        } while (nextToken() == ',');
        return;
      }
      return;
    }
    if (c == 'n') {
      return;
    }
    throw reportError("readArrayCB", "expect [ or n, but found: " + (char) c);
  }

  private static final CharBufferFunction<String> READ_STRING_FUNCTION = (count, _reusableChars) -> new String(_reusableChars, 0, count);

  @Override
  public final String readString() throws IOException {
    return readChars(READ_STRING_FUNCTION);
  }

  @Override
  public final <T> T readChars(final CharBufferFunction<T> applyChars) throws IOException {
    final byte c = nextToken();
    if (c != '"') {
      if (c == 'n') {
        skipFixedBytes(3);
        return null;
      }
      throw reportError("readString", "expect string or null, but " + (char) c);
    }
    final int count = parse();
    return applyChars.apply(count, reusableChars);
  }

  @Override
  public final boolean testChars(final CharBufferPredicate testChars) throws IOException {
    final byte c = nextToken();
    if (c != '"') {
      if (c == 'n') {
        skipFixedBytes(3);
        return false;
      }
      throw reportError("readString", "expect string or null, but " + (char) c);
    }
    final int count = parse();
    return testChars.apply(count, reusableChars);
  }

  private int parse() throws IOException {
    byte c;// try fast path first
    int i = head;
    final int bound = updateStringCopyBound(reusableChars.length);
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
      reusableChars[j] = (char) c;
    }
    int alreadyCopied = 0;
    if (i > head) {
      alreadyCopied = i - head - 1;
      head = i - 1;
    }
    return readStringSlowPath(alreadyCopied);
  }

  int updateStringCopyBound(final int bound) {
    return bound;
  }

  @Override
  public final String readObjField() throws IOException {
    byte c = nextToken();
    switch (c) {
      case 'n':
        skipFixedBytes(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          unreadByte();
          final var field = readString();
          if ((c = nextToken()) != ':') {
            throw reportError("readObject", "expect , but " + ((char) c));
          }
          return field;
        }
        if (c == '}') {
          return null; // end of object
        }
        throw reportError("readObject", `expect " after {`);
      case ',':
        final var field = readString();
        if ((c = nextToken()) != ':') {
          throw reportError("readObject", "expect , but " + ((char) c));
        }
        return field;
      case '}':
        return null; // end of object
      default:
        throw reportError("readObject", "expect { or , or } or n, but found: " + (char) c);
    }
  }

  public final JsonIterator skipObjField() throws IOException {
    byte c = nextToken();
    switch (c) {
      case 'n':
        skipFixedBytes(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          parse();
          if ((c = nextToken()) != ':') {
            throw reportError("readObject", "expect :, but " + ((char) c));
          }
          return this;
        }
        if (c == '}') { // end of object
          return null;
        }
        throw reportError("readObject", `expect " after {`);
      case ',':
        c = nextToken();
        if (c != '"') {
          throw reportError("readObject", "expect string field, but " + (char) c);
        }
        parse();
        if ((c = nextToken()) != ':') {
          throw reportError("readObject", "expect :, but " + ((char) c));
        }
        return this;
      case '}': // end of object
        return null;
      default:
        throw reportError("readObject", "expect { or , or } or n, but found: " + (char) c);
    }
  }

  @Override
  public final <C> C consumeObject(final C context, final FieldBufferPredicate<C> fieldBufferFunction) throws IOException {
    for (byte c = nextToken(); ; c = nextToken()) {
      switch (c) {
        case 'n':
          skipFixedBytes(3);
          return context;
        case '{':
          c = nextToken();
          if (c == '"') {
            final int count = parse();
            if ((c = nextToken()) != ':') {
              throw reportError("readObject", "expect :, but " + ((char) c));
            }
            if (fieldBufferFunction.apply(context, count, reusableChars, this)) {
              continue;
            }
            return context;
          }
          if (c == '}') { // end of object
            return context;
          }
          throw reportError("readObject", `expect " after {`);
        case ',':
          c = nextToken();
          if (c != '"') {
            throw reportError("readObject", "expect string field, but " + (char) c);
          }
          final int count = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("readObject", "expect :, but " + ((char) c));
          }
          if (fieldBufferFunction.apply(context, count, reusableChars, this)) {
            continue;
          }
          return context;
        case '}': // end of object
          return context;
        default:
          throw reportError("readObject", "expect { or , or } or n, but found: " + (char) c);
      }
    }
  }

  @Override
  public final <R, C> R applyObjField(final C context, final FieldBufferFunction<C, R> fieldBufferFunction) throws IOException {
    byte c = nextToken();
    switch (c) {
      case 'n':
        skipFixedBytes(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          final int count = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("readObject", "expect :, but " + ((char) c));
          }
          return fieldBufferFunction.apply(context, count, reusableChars, this);
        }
        if (c == '}') { // end of object
          return null;
        }
        throw reportError("readObject", `expect " after {`);
      case ',':
        c = nextToken();
        if (c != '"') {
          throw reportError("readObject", "expect string field, but " + (char) c);
        }
        final int count = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("readObject", "expect :, but " + ((char) c));
        }
        return fieldBufferFunction.apply(context, count, reusableChars, this);
      case '}': // end of object
        return null;
      default:
        throw reportError("readObject", "expect { or , or } or n, but found: " + (char) c);
    }
  }

  private void readObjectCB(final Map<String, Object> attachment) throws IOException {
    byte c = nextToken();
    if ('{' == c) {
      c = nextToken();
      if ('"' == c) {
        unreadByte();
        var field = readString();
        if (nextToken() != ':') {
          throw reportError("readObject", "expect :");
        }
        attachment.put(field, read());
        while (nextToken() == ',') {
          field = readString();
          if (nextToken() != ':') {
            throw reportError("readObject", "expect :");
          }
          attachment.put(field, read());
        }
        return;
      }
      if ('}' == c) {
        return;
      }
      throw reportError("readObjectCB", `expect " after {`);
    }
    if ('n' == c) {
      skipFixedBytes(3);
      return;
    }
    throw reportError("readObjectCB", "expect { or n");
  }

  @Override
  public final float readFloat() throws IOException {
    return (float) readDouble();
  }

  private static final CharBufferFunction<BigDecimal> READ_BIG_DECIMAL_FUNCTION = (count, chars) -> new BigDecimal(chars, 0, count);
  private static final CharBufferFunction<BigDecimal> READ_BIG_DECIMAL_STRIP_TRAILING_ZEROES_FUNCTION = (count, chars) -> {
    if (count == 1) {
      return chars[count] == '0'
          ? BigDecimal.ZERO
          : new BigDecimal(chars, 0, count);
    }
    if (chars[count - 1] != '0') {
      return new BigDecimal(chars, 0, count);
    }
    int i = count - 2;
    char c = chars[i];
    while (c == '0') {
      if (i == 0) {
        return BigDecimal.ZERO;
      }
      c = chars[--i];
    }
    for (int j = i; c != '.'; c = chars[--j]) {
      if ((c == 'e') || (c == 'E')) {
        return new BigDecimal(chars, 0, count).stripTrailingZeros();
      }
      if (j == 0) { // Not a decimal
        return new BigDecimal(chars, 0, count);
      }
    }
    return new BigDecimal(chars, 0, i + 1);
  };

  @Override
  public final BigDecimal readBigDecimal() throws IOException {
    return readBigDecimal(READ_BIG_DECIMAL_FUNCTION);
  }

  @Override
  public final BigDecimal readBigDecimalStripTrailingZeroes() throws IOException {
    return readBigDecimal(READ_BIG_DECIMAL_STRIP_TRAILING_ZEROES_FUNCTION);
  }

  private BigDecimal readBigDecimal(final CharBufferFunction<BigDecimal> parseChars) throws IOException {
    // skip whitespace by read next
    final var valueType = whatIsNext();
    if (valueType == STRING) {
      return readChars(parseChars);
    }
    if (valueType == NUMBER) {
      final var numberChars = readNumber();
      return parseChars.apply(numberChars.charsLength, numberChars.chars);
    }
    if (valueType == NULL) {
      skip();
      return null;
    }
    throw reportError("readBigInteger", "Must be a number or a string, found " + valueType);
  }

  private static final CharBufferFunction<BigInteger> READ_BIG_INTEGER_FUNCTION = (count, chars) -> new BigInteger(new String(chars, 0, count));

  @Override
  public final BigInteger readBigInteger() throws IOException {
    // skip whitespace by read next
    final var valueType = whatIsNext();
    if (valueType == NUMBER) {
      return new BigInteger(readNumberAsString());
    }
    if (valueType == STRING) {
      return readChars(READ_BIG_INTEGER_FUNCTION);
    }
    if (valueType == NULL) {
      skip();
      return null;
    }
    throw reportError("readBigInteger", "Must be a number or a string, found " + valueType);
  }

  @Override
  public final Object read() throws IOException {
    try {
      final var valueType = whatIsNext();
      switch (valueType) {
        case STRING:
          return readString();
        case NUMBER:
          final var numberChars = readNumber();
          final var doubleNumber = Double.parseDouble(new String(numberChars.chars, 0, numberChars.charsLength));
          if (numberChars.dotFound) {
            return doubleNumber;
          }
          if (doubleNumber == Math.floor(doubleNumber) && !Double.isInfinite(doubleNumber)) {
            final long longNumber = (long) doubleNumber;
            if (longNumber <= Integer.MAX_VALUE && longNumber >= Integer.MIN_VALUE) {
              return (int) longNumber;
            }
            return longNumber;
          }
          return doubleNumber;
        case NULL:
          skipFixedBytes(4);
          return null;
        case BOOLEAN:
          return readBoolean();
        case ARRAY:
          final var list = new ArrayList<>(4);
          readArrayCB(list);
          return list;
        case OBJECT:
          final var map = new HashMap<String, Object>(4);
          readObjectCB(map);
          return map;
        default:
          throw reportError("read", "unexpected value type: " + valueType);
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw reportError("read", "premature end");
    }
  }

  @Override
  public final ValueType whatIsNext() throws IOException {
    final var valueType = VALUE_TYPES[nextToken()];
    unreadByte();
    return valueType;
  }

  @Override
  public final JsonIterator skip() throws IOException {
    final byte c = nextToken();
    return switch (c) {
      case '"' ->skipString();
      case '-','0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->skipUntilBreak();
      case 't','n' ->skipFixedBytes(3); // true or null
      case 'f' ->skipFixedBytes(4); // false
      case '[' ->skipArray();
      case '{' ->skipObject();
      default ->throw reportError("skip", "do not know how to skip: " + c);
    } ;
  }

  JsonIterator skipArray() throws IOException {
    int level = 1;
    for (int i = head; i < tail; i++) {
      switch (buf[i]) {
        case '"': // If inside string, skip it
          head = i + 1;
          skipString();
          i = head - 1; // it will be i++ soon
          break;
        case '[': // If open symbol, increase level
          level++;
          break;
        case ']': // If close symbol, increase level
          level--;
          // If we have returned to the original level, we're done
          if (level == 0) {
            head = i + 1;
            return this;
          }
          break;
      }
    }
    throw reportError("skipArray", "incomplete array");
  }

  JsonIterator skipObject() throws IOException {
    for (int i = head, level = 1; i < tail; i++) {
      switch (buf[i]) {
        case '"': // If inside string, skip it
          head = i + 1;
          skipString();
          i = head - 1; // it will be i++ soon
          break;
        case '{': // If open symbol, increase level
          level++;
          break;
        case '}': // If close symbol, increase level
          level--;
          // If we have returned to the original level, we're done
          if (level == 0) {
            head = i + 1;
            return this;
          }
          break;
      }
    }
    throw reportError("skipObject", "incomplete object");
  }

  JsonIterator skipString() throws IOException {
    final int end = findStringEnd();
    if (end == -1) {
      throw reportError("skipString", "incomplete string");
    }
    head = end;
    return this;
  }

  // adapted from: https://github.com/buger/jsonparser/blob/master/parser.go
  // Tries to find the end of string
  // Support if string contains escaped quote symbols.
  final int findStringEnd() {
    byte c;
    ESCAPED:
    for (int i = head; i < tail; i++) {
      c = buf[i];
      if (c == '"') {
        return i + 1;
      }
      if (c == '\\') {
        for (int numEscapes = 1; ; ++numEscapes) {
          if (++i == tail) {
            return -1;
          }
          c = buf[i];
          if (c == '"') {
            if ((numEscapes & 1) == 0) {
              return i + 1;
            }
            continue ESCAPED;
          }
          if (c != '\\') {
            continue ESCAPED;
          }
        }
      }
    }
    return -1;
  }

  JsonIterator skipUntilBreak() throws IOException {
    for (int i = head; i < tail; i++) {
      switch (buf[i]) {
        case ' ':
        case '\t':
        case '\n':
        case '\r':
        case ',':
        case '}':
        case ']':
          head = i;
          return this;
      }
    }
    head = tail;
    return this;
  }

  byte nextToken() throws IOException {
    byte c;
    for (int i = head; ; ) {
      c = buf[i++];
      switch (c) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
          continue;
        default:
          head = i;
          return c;
      }
    }
  }

  byte readByte() throws IOException {
    return buf[head++];
  }

  JsonIterator skipFixedBytes(final int n) throws IOException {
    head += n;
    return this;
  }

  int readStringSlowPath(int j) throws IOException {
    try {
      boolean isExpectingLowSurrogate = false;
      for (int i = head, bc; i < tail; ) {
        bc = buf[i++];
        if (bc == '"') {
          head = i;
          return j;
        }
        if (bc == '\\') {
          bc = buf[i++];
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
              bc = (JHex.decode(buf[i++]) << 12) +
                  (JHex.decode(buf[i++]) << 8) +
                  (JHex.decode(buf[i++]) << 4) +
                  JHex.decode(buf[i++]);
              if (Character.isHighSurrogate((char) bc)) {
                if (isExpectingLowSurrogate) {
                  throw new JsonException("invalid surrogate");
                }
                isExpectingLowSurrogate = true;
              } else if (Character.isLowSurrogate((char) bc)) {
                if (isExpectingLowSurrogate) {
                  isExpectingLowSurrogate = false;
                } else {
                  throw new JsonException("invalid surrogate");
                }
              } else if (isExpectingLowSurrogate) {
                throw new JsonException("invalid surrogate");
              }
              break;
            default:
              throw reportError("readStringSlowPath", "invalid escape character: " + bc);
          }
        } else if ((bc & 0x80) != 0) {
          final int u2 = buf[i++];
          if ((bc & 0xE0) == 0xC0) {
            bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
          } else {
            final int u3 = buf[i++];
            if ((bc & 0xF0) == 0xE0) {
              bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
            } else {
              final int u4 = buf[i++];
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
                if (reusableChars.length == j) {
                  doubleReusableCharBuffer();
                }
                reusableChars[j++] = (char) ((sup >>> 10) + 0xd800);
                if (reusableChars.length == j) {
                  doubleReusableCharBuffer();
                }
                reusableChars[j++] = (char) ((sup & 0x3ff) + 0xdc00);
                continue;
              }
            }
          }
        }
        if (reusableChars.length == j) {
          doubleReusableCharBuffer();
        }
        reusableChars[j++] = (char) bc;
      }
      throw reportError("readStringSlowPath", "incomplete string");
    } catch (IndexOutOfBoundsException e) {
      throw reportError("readString", "incomplete string");
    }
  }

  final void doubleReusableCharBuffer() {
    final char[] newBuf = new char[reusableChars.length << 1];
    System.arraycopy(reusableChars, 0, newBuf, 0, reusableChars.length);
    reusableChars = newBuf;
  }

  void assertNotLeadingZero() throws IOException {
    try {
      if (head == buf.length) {
        return;
      }
      final byte nextByte = readByte();
      unreadByte();
      if (INT_DIGITS[nextByte] == INVALID_CHAR_FOR_NUMBER) {
        return;
      }
      throw reportError("assertNotLeadingZero", "leading zero is invalid");
    } catch (final ArrayIndexOutOfBoundsException e) {
      head = tail;
    }
  }

  int readInt(final byte c) throws IOException {
    int ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    }
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readInt", "expect 0~9");
    }
    if (tail - head > 9) {
      int i = head;
      final int ind2 = INT_DIGITS[buf[i]];
      if (ind2 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        return -ind;
      }
      final int ind3 = INT_DIGITS[buf[++i]];
      if (ind3 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10 + ind2;
        return -ind;
      }
      final int ind4 = INT_DIGITS[buf[++i]];
      if (ind4 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100 + ind2 * 10 + ind3;
        return -ind;
      }
      final int ind5 = INT_DIGITS[buf[++i]];
      if (ind5 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000 + ind2 * 100 + ind3 * 10 + ind4;
        return -ind;
      }
      final int ind6 = INT_DIGITS[buf[++i]];
      if (ind6 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10000 + ind2 * 1000 + ind3 * 100 + ind4 * 10 + ind5;
        return -ind;
      }
      final int ind7 = INT_DIGITS[buf[++i]];
      if (ind7 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100000 + ind2 * 10000 + ind3 * 1000 + ind4 * 100 + ind5 * 10 + ind6;
        return -ind;
      }
      final int ind8 = INT_DIGITS[buf[++i]];
      if (ind8 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000000 + ind2 * 100000 + ind3 * 10000 + ind4 * 1000 + ind5 * 100 + ind6 * 10 + ind7;
        return -ind;
      }
      final int ind9 = INT_DIGITS[buf[++i]];
      ind = ind * 10000000 + ind2 * 1000000 + ind3 * 100000 + ind4 * 10000 + ind5 * 1000 + ind6 * 100 + ind7 * 10 + ind8;
      head = i;
      if (ind9 == INVALID_CHAR_FOR_NUMBER) {
        return -ind;
      }
    }
    return readIntSlowPath(ind);
  }

  int readIntSlowPath(int value) throws IOException {
    value = -value; // add negatives to avoid redundant checks for Integer.MIN_VALUE on each iteration
    for (int i = head, ind; i < tail; i++) {
      ind = INT_DIGITS[buf[i]];
      if (ind == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        return value;
      }
      if (value < -214748364) { // limit / 10
        throw reportError("readIntSlowPath", "value is too large for int");
      }
      value = (value << 3) + (value << 1) - ind;
      if (value >= 0) {
        throw reportError("readIntSlowPath", "value is too large for int");
      }
    }
    head = tail;
    return value;
  }

  long readLong(final byte c) throws IOException {
    long ind = INT_DIGITS[c];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readLong", "expect 0~9");
    }
    if (tail - head > 9) {
      int i = head;
      final int ind2 = INT_DIGITS[buf[i]];
      if (ind2 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        return -ind;
      }
      final int ind3 = INT_DIGITS[buf[++i]];
      if (ind3 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10 + ind2;
        return -ind;
      }
      final int ind4 = INT_DIGITS[buf[++i]];
      if (ind4 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100 + ind2 * 10 + ind3;
        return -ind;
      }
      final int ind5 = INT_DIGITS[buf[++i]];
      if (ind5 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000 + ind2 * 100 + ind3 * 10 + ind4;
        return -ind;
      }
      final int ind6 = INT_DIGITS[buf[++i]];
      if (ind6 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10000 + ind2 * 1000 + ind3 * 100 + ind4 * 10 + ind5;
        return -ind;
      }
      final int ind7 = INT_DIGITS[buf[++i]];
      if (ind7 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100000 + ind2 * 10000 + ind3 * 1000 + ind4 * 100 + ind5 * 10 + ind6;
        return -ind;
      }
      final int ind8 = INT_DIGITS[buf[++i]];
      if (ind8 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000000 + ind2 * 100000 + ind3 * 10000 + ind4 * 1000 + ind5 * 100 + ind6 * 10 + ind7;
        return -ind;
      }
      final int ind9 = INT_DIGITS[buf[++i]];
      ind = ind * 10000000 + ind2 * 1000000 + ind3 * 100000 + ind4 * 10000 + ind5 * 1000 + ind6 * 100 + ind7 * 10 + ind8;
      head = i;
      if (ind9 == INVALID_CHAR_FOR_NUMBER) {
        return -ind;
      }
    }
    return readLongSlowPath(ind);
  }

  long readLongSlowPath(long value) throws IOException {
    value = -value; // add negatives to avoid redundant checks for Long.MIN_VALUE on each iteration
    for (int i = head, ind; i < tail; i++) {
      ind = INT_DIGITS[buf[i]];
      if (ind == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        return value;
      }
      if (value < -922337203685477580L) { // limit / 10
        throw reportError("readLongSlowPath", "value is too large for long");
      }
      value = (value << 3) + (value << 1) - ind;
      if (value >= 0) {
        throw reportError("readLongSlowPath", "value is too large for long");
      }
    }
    head = tail;
    return value;
  }

  @Override
  public final double readDouble() throws IOException {
    if (nextToken() == '-') {
      return -readDoubleNoSign();
    }
    unreadByte();
    return readDoubleNoSign();
  }

  double readDoubleNoSign() throws IOException {
    int oldHead = head;
    try {
      final long value = readLong(); // without the dot & sign
      if (head == tail) {
        return value;
      }
      byte c = buf[head];
      if (c == '.') {
        head++;
        final int start = head;
        c = buf[head++];
        long decimalPart = readLong(c);
        if (decimalPart == Long.MIN_VALUE) {
          return readDoubleSlowPath();
        }
        if (head < tail && (buf[head] == 'e' || buf[head] == 'E')) {
          head = oldHead;
          return readDoubleSlowPath();
        }
        decimalPart = -decimalPart;
        final int decimalPlaces = head - start;
        if (decimalPlaces > 0 && decimalPlaces < POW10.length && (head - oldHead) < 10) {
          return value + (decimalPart / (double) POW10[decimalPlaces]);
        }
        head = oldHead;
        return readDoubleSlowPath();
      }
      if (head < tail && (buf[head] == 'e' || buf[head] == 'E')) {
        head = oldHead;
        return readDoubleSlowPath();
      }
      return value;
    } catch (final JsonException e) {
      head = oldHead;
      return readDoubleSlowPath();
    }
  }

  final double readDoubleSlowPath() throws IOException {
    try {
      final var numberChars = readNumber();
      if (numberChars.charsLength == 0 && whatIsNext() == STRING) {
        final var possibleInf = readString();
        if ("infinity".equals(possibleInf)) {
          return Double.POSITIVE_INFINITY;
        }
        if ("-infinity".equals(possibleInf)) {
          return Double.NEGATIVE_INFINITY;
        }
        throw reportError("readDoubleSlowPath", "expect number but found string: " + possibleInf);
      }
      return Double.valueOf(new String(numberChars.chars, 0, numberChars.charsLength));
    } catch (final NumberFormatException e) {
      throw reportError("readDoubleSlowPath", e.toString());
    }
  }
}
