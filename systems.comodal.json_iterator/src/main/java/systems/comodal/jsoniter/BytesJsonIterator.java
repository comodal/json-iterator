package systems.comodal.jsoniter;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static systems.comodal.jsoniter.ValueType.VALUE_TYPES;

class BytesJsonIterator implements JsonIterator {

  static final boolean[] BREAKS = new boolean[127];

  static {
    BREAKS[' '] = true;
    BREAKS['\t'] = true;
    BREAKS['\n'] = true;
    BREAKS['\r'] = true;
    BREAKS[','] = true;
    BREAKS['}'] = true;
    BREAKS[']'] = true;
  }

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

  JsonException reportError(final String op, final String msg) {
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
    final String peek = new String(buf, peekStart, head - peekStart);
    return "head: " + head + ", peek: " + peek + ", buf: " + new String(buf);
  }

  @Override
  public boolean readNull() throws IOException {
    final byte c = nextToken();
    if (c != 'n') {
      unreadByte();
      return false;
    }
    skipFixedBytes(3); // null
    return true;
  }

  @Override
  public boolean readBoolean() throws IOException {
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
  public short readShort() throws IOException {
    final int v = readInt();
    if (Short.MIN_VALUE <= v && v <= Short.MAX_VALUE) {
      return (short) v;
    }
    throw reportError("readShort", "short overflow: " + v);
  }

  @Override
  public int readInt() throws IOException {
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
  public long readLong() throws IOException {
    byte c = nextToken();
    if (c == '-') {
      c = readByte();
      if(INT_DIGITS[c] == 0) {
        assertNotLeadingZero();
        return 0;
      }
      return readLong(c);
    }
    if(INT_DIGITS[c] == 0) {
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
  public boolean readArray() throws IOException {
    byte c = nextToken();
    switch (c) {
      case '[':
        c = nextToken();
        if (c != ']') {
          unreadByte();
          return true;
        }
        return false;
      case ']':
        return false;
      case ',':
        return true;
      case 'n':
        return false;
      default:
        throw reportError("readArray", "expect [ or , or n or ], but found: " + (char) c);
    }
  }

  @Override
  public String readNumberAsString() throws IOException {
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
        final char[] newBuf = new char[reusableChars.length * 2];
        System.arraycopy(reusableChars, 0, newBuf, 0, reusableChars.length);
        reusableChars = newBuf;
      }
      final byte c = buf[i];
      switch (c) {
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

  @Override
  public String readString() throws IOException {
    final byte c = nextToken();
    if (c != '"') {
      if (c == 'n') {
        skipFixedBytes(3);
        return null;
      }
      throw reportError("readString", "expect string or null, but " + (char) c);
    }
    final int count = parse();
    return new String(reusableChars, 0, count);
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
      // or a high bit was set - indicating an UTF-8 encoded multibyte character,
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
  public String readObject() throws IOException {
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
          if (nextToken() != ':') {
            throw reportError("readObject", "expect :");
          }
          return field;
        }
        if (c == '}') {
          return null; // end of object
        }
        throw reportError("readObject", "expect \" after {");
      case ',':
        final var field = readString();
        if (nextToken() != ':') {
          throw reportError("readObject", "expect :");
        }
        return field;
      case '}':
        return null; // end of object
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
      throw reportError("readObjectCB", "expect \" after {");
    }
    if ('n' == c) {
      skipFixedBytes(3);
      return;
    }
    throw reportError("readObjectCB", "expect { or n");
  }

  @Override
  public float readFloat() throws IOException {
    return (float) readDouble();
  }

  @Override
  public BigDecimal readBigDecimal() throws IOException {
    // skip whitespace by read next
    final var valueType = whatIsNext();
    if (valueType == ValueType.NULL) {
      skip();
      return null;
    }
    if (valueType != ValueType.NUMBER) {
      throw reportError("readBigDecimal", "not number");
    }
    final var numberChars = readNumber();
    return new BigDecimal(numberChars.chars, 0, numberChars.charsLength);
  }

  @Override
  public BigInteger readBigInteger() throws IOException {
    // skip whitespace by read next
    final var valueType = whatIsNext();
    if (valueType == ValueType.NULL) {
      skip();
      return null;
    }
    if (valueType != ValueType.NUMBER) {
      throw reportError("readBigDecimal", "not number");
    }
    final var numberChars = readNumber();
    return new BigInteger(new String(numberChars.chars, 0, numberChars.charsLength));
  }

  @Override
  public Object read() throws IOException {
    try {
      final var valueType = whatIsNext();
      switch (valueType) {
        case STRING:
          return readString();
        case NUMBER:
          final var numberChars = readNumber();
          final var number = Double.valueOf(new String(numberChars.chars, 0, numberChars.charsLength));
          if (numberChars.dotFound) {
            return number;
          }
          final double doubleNumber = number;
          if (doubleNumber == Math.floor(doubleNumber) && !Double.isInfinite(doubleNumber)) {
            final long longNumber = (long) doubleNumber;
            if (longNumber <= Integer.MAX_VALUE && longNumber >= Integer.MIN_VALUE) {
              return (int) longNumber;
            }
            return longNumber;
          }
          return number;
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
  public ValueType whatIsNext() throws IOException {
    final var valueType = VALUE_TYPES[nextToken()];
    unreadByte();
    return valueType;
  }

  @Override
  public JsonIterator skip() throws IOException {
    final byte c = nextToken();
    switch (c) {
      case '"':
        skipString();
        return this;
      case '-':
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
        skipUntilBreak();
        return this;
      case 't':
      case 'n':
        skipFixedBytes(3); // true or null
        return this;
      case 'f':
        skipFixedBytes(4); // false
        return this;
      case '[':
        skipArray();
        return this;
      case '{':
        skipObject();
        return this;
      default:
        throw reportError("IterImplSkip", "do not know how to skip: " + c);
    }
  }

  void skipArray() throws IOException {
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
            return;
          }
          break;
      }
    }
    throw reportError("skipArray", "incomplete array");
  }

  void skipObject() throws IOException {
    int level = 1;
    for (int i = head; i < tail; i++) {
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
            return;
          }
          break;
      }
    }
    throw reportError("skipObject", "incomplete object");
  }

  void skipString() throws IOException {
    final int end = findStringEnd();
    if (end == -1) {
      throw reportError("skipString", "incomplete string");
    }
    head = end;
  }

  // adapted from: https://github.com/buger/jsonparser/blob/master/parser.go
  // Tries to find the end of string
  // Support if string contains escaped quote symbols.
  final int findStringEnd() {
    boolean escaped = false;
    for (int i = head; i < tail; i++) {
      final byte c = buf[i];
      if (c == '"') {
        if (!escaped) {
          return i + 1;
        }
        int j = i - 1;
        for (; ; ) {
          if (j < head || buf[j] != '\\') {
            // even number of backslashes
            // either end of buffer, or " found
            return i + 1;
          }
          j--;
          if (j < head || buf[j] != '\\') {
            // odd number of backslashes
            // it is \" or \\\"
            break;
          }
          j--;
        }
      } else if (c == '\\') {
        escaped = true;
      }
    }
    return -1;
  }

  void skipUntilBreak() throws IOException {
    // true, false, null, number
    for (int i = head; i < tail; i++) {
      if (BREAKS[buf[i]]) {
        head = i;
        return;
      }
    }
    head = tail;
  }

  byte nextToken() throws IOException {
    for (int i = head; ; ) {
      final byte c = buf[i++];
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

  void skipFixedBytes(final int n) throws IOException {
    head += n;
  }

  int readStringSlowPath(int j) throws IOException {
    try {
      boolean isExpectingLowSurrogate = false;
      for (int i = head; i < tail; ) {
        int bc = buf[i++];
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
                  final char[] newBuf = new char[reusableChars.length * 2];
                  System.arraycopy(reusableChars, 0, newBuf, 0, reusableChars.length);
                  reusableChars = newBuf;
                }
                reusableChars[j++] = (char) ((sup >>> 10) + 0xd800);
                if (reusableChars.length == j) {
                  final char[] newBuf = new char[reusableChars.length * 2];
                  System.arraycopy(reusableChars, 0, newBuf, 0, reusableChars.length);
                  reusableChars = newBuf;
                }
                reusableChars[j++] = (char) ((sup & 0x3ff) + 0xdc00);
                continue;
              }
            }
          }
        }
        if (reusableChars.length == j) {
          final char[] newBuf = new char[reusableChars.length * 2];
          System.arraycopy(reusableChars, 0, newBuf, 0, reusableChars.length);
          reusableChars = newBuf;
        }
        reusableChars[j++] = (char) bc;
      }
      throw reportError("readStringSlowPath", "incomplete string");
    } catch (IndexOutOfBoundsException e) {
      throw reportError("readString", "incomplete string");
    }
  }

  void assertNotLeadingZero() throws IOException {
    try {
      if (head == buf.length) {
        return;
      }
      final byte nextByte = readByte();
      unreadByte();
      final int ind2 = INT_DIGITS[nextByte];
      if (ind2 == INVALID_CHAR_FOR_NUMBER) {
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
    for (int i = head; i < tail; i++) {
      final int ind = INT_DIGITS[buf[i]];
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
    for (int i = head; i < tail; i++) {
      final int ind = INT_DIGITS[buf[i]];
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
  public double readDouble() throws IOException {
    final byte c = nextToken();
    if (c == '-') {
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

  double readDoubleSlowPath() throws IOException {
    try {
      final var numberChars = readNumber();
      if (numberChars.charsLength == 0 && whatIsNext() == ValueType.STRING) {
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
