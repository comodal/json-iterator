package systems.comodal.jsoniter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static systems.comodal.jsoniter.ValueType.*;

abstract class BaseJsonIterator implements JsonIterator {

  private static final int INVALID_CHAR_FOR_NUMBER = -1;
  static final int[] INT_DIGITS = INIT_INT_DIGITS.initIntDigits();

  private static final class INIT_INT_DIGITS {

    private INIT_INT_DIGITS() {
    }

    private static int[] initIntDigits() {
      final int[] intDigits = new int[127];
      Arrays.fill(intDigits, INVALID_CHAR_FOR_NUMBER);
      for (int i = '0'; i <= '9'; ++i) {
        intDigits[i] = (i - '0');
      }
      return intDigits;
    }
  }

  int head;
  int tail;


  BaseJsonIterator(final int head, final int tail) {
    this.head = head;
    this.tail = tail;
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

  abstract String getBufferString(final int from, final int to);

  final JsonException reportError(final String op, final String msg) {
    final var peek = getBufferString(head <= 10 ? 0 : head - 10, head > tail ? tail : head);
    throw new JsonException(op + ": " + msg + ", head: " + head + ", peek: " + peek + ", buf: " + getBufferString(0, 1_024));
  }

  @Override
  public final String currentBuffer() {
    final var peek = getBufferString(head <= 10 ? 0 : head - 10, head);
    return "head: " + head + ", peek: " + peek + ", buf: " + getBufferString(0, 1_024);
  }

  abstract char readChar();

  abstract int readAsInt();

  abstract char peekChar();

  abstract char peekChar(final int offset);

  abstract int peekIntDigitChar(final int offset);

  abstract char nextToken();

  abstract char peekToken();

  boolean loadMore() {
    return false;
  }

  private void skip(final int n) {
    head += n;
    if (head >= tail) {
      final int more = head - tail;
      if (!loadMore()) {
        if (more == 0) {
          head = tail;
          return;
        }
        throw reportError("skip", "unexpected end");
      }
      head += more;
    }
  }

  @Override
  public final JsonIterator openArray() {
    final char c = nextToken();
    if (c == '[') {
      return this;
    }
    throw reportError("openArray", "expected '[' but found: " + c);
  }

  @Override
  public final JsonIterator continueArray() {
    final char c = nextToken();
    if (c == ',') {
      return this;
    }
    throw reportError("continueArray", "expected ',' but found: " + c);
  }

  @Override
  public final JsonIterator closeArray() {
    final char c = nextToken();
    if (c == ']') {
      return this;
    }
    throw reportError("closeArray", "expected ']' but found: " + c);
  }

  private static final CharBufferFunction<String> READ_STRING_FUNCTION = String::new;

  @Override
  public final String readString() {
    return applyChars(READ_STRING_FUNCTION);
  }

  abstract int parse();

  @Override
  public final <R> R applyChars(final CharBufferFunction<R> applyChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(applyChars);
    }
    if (c == 'n') {
      skip(3);
      return null;
    }
    throw reportError("applyChars", "expected string or null, but " + c);
  }

  abstract <R> R parse(final CharBufferFunction<R> applyChars);

  @Override
  public final <C, R> R applyChars(final C context, final ContextCharBufferFunction<C, R> applyChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(context, applyChars);
    }
    if (c == 'n') {
      skip(3);
      return null;
    }
    throw reportError("applyChars", "expected string or null, but " + c);
  }

  abstract <C, R> R parse(final C context, final ContextCharBufferFunction<C, R> applyChars);

  @Override
  public final boolean testChars(final CharBufferPredicate testChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(testChars);
    }
    if (c == 'n') {
      skip(3);
      return false;
    }
    throw reportError("testChars", "expected string or null, but " + c);
  }

  abstract boolean parse(final CharBufferPredicate testChars);

  @Override
  public final <C> boolean testChars(final C context, final ContextCharBufferPredicate<C> testChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(context, testChars);
    }
    if (c == 'n') {
      skip(3);
      return false;
    }
    throw reportError("testChars", "expected string or null, but " + c);
  }

  abstract <C> boolean parse(final C context, final ContextCharBufferPredicate<C> testChars);

  @Override
  public final void consumeChars(final CharBufferConsumer testChars) {
    final char c = nextToken();
    if (c == '"') {
      parse(testChars);
    } else if (c == 'n') {
      skip(3);
    } else {
      throw reportError("consumeChars", "expected string or null, but " + c);
    }
  }

  abstract void parse(final CharBufferConsumer testChars);

  @Override
  public final <C> void consumeChars(final C context, final ContextCharBufferConsumer<C> testChars) {
    final char c = nextToken();
    if (c == '"') {
      parse(context, testChars);
    } else if (c == 'n') {
      skip(3);
    } else {
      throw reportError("consumeChars", "expected string or null, but " + c);
    }
  }

  abstract <C> void parse(final C context, final ContextCharBufferConsumer<C> testChars);

  abstract boolean fieldEquals(final String field, final int offset, final int len);

  @Override
  public final JsonIterator skipUntil(final String field) {
    char c;
    for (int offset, len; ; ) {
      switch ((c = nextToken())) {
        case '{':
          c = nextToken();
          if (c == '"') {
            offset = head;
            len = parse();
            if ((c = nextToken()) != ':') {
              throw reportError("skipUntil", "expected :, but " + c);
            }
            if (fieldEquals(field, offset, len)) {
              return this;
            }
            skip();
            continue;
          }
          if (c == '}') { // end of object
            return null;
          }
          throw reportError("skipUntil", "expected \" after {");
        case ',':
          c = nextToken();
          if (c != '"') {
            throw reportError("skipUntil", "expected string field, but " + c);
          }
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("skipUntil", "expected :, but " + c);
          }
          if (fieldEquals(field, offset, len)) {
            return this;
          }
          skip();
          continue;
        case '}': // end of object
          return null;
        default:
          throw reportError("skipUntil", "expected { or , or } or n, but found: " + c);
      }
    }
  }

  private boolean testField(final CharBufferPredicate testField) {
    final char c = nextToken();
    if (c != '"') {
      throw reportError("testField", "expected field string, but " + c);
    }
    return parse(testField);
  }

  @Override
  public final boolean testObjField(final CharBufferPredicate testField) {
    char c = nextToken();
    switch (c) {
      case 'n':
        skip(3);
        return false;
      case '{':
        c = nextToken();
        if (c == '"') {
          final boolean result = parse(testField);
          if ((c = nextToken()) != ':') {
            throw reportError("testObjField", "expected :, but " + c);
          }
          return result;
        }
        if (c == '}') {
          return false; // end of object
        }
        throw reportError("testObjField", "expected \" after {");
      case ',':
        final boolean result = testField(testField);
        if ((c = nextToken()) != ':') {
          throw reportError("testObjField", "expected :, but " + c);
        }
        return result;
      case '}':
        return false; // end of object
      default:
        throw reportError("testObjField", "expected { or , or } or n, but found: " + c);
    }
  }

  private String readField() {
    final char c = nextToken();
    if (c != '"') {
      throw reportError("readField", "expected field string, but " + c);
    }
    return parse(READ_STRING_FUNCTION);
  }

  @Override
  public final String readObjField() {
    char c = nextToken();
    switch (c) {
      case 'n':
        skip(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          final var field = parse(READ_STRING_FUNCTION);
          if ((c = nextToken()) != ':') {
            throw reportError("readObjField", "expected :, but " + c);
          }
          return field;
        }
        if (c == '}') {
          return null; // end of object
        }
        throw reportError("readObjField", "expected \" after {");
      case ',':
        final var field = readField();
        if ((c = nextToken()) != ':') {
          throw reportError("readObjField", "expected :, but " + c);
        }
        return field;
      case '}':
        return null; // end of object
      default:
        throw reportError("readObjField", "expected { or , or } or n, but found: " + c);
    }
  }

  public final JsonIterator skipObjField() {
    char c = nextToken();
    switch (c) {
      case 'n':
        skip(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          parse();
          if ((c = nextToken()) != ':') {
            throw reportError("skipObjField", "expected :, but " + c);
          }
          return this;
        }
        if (c == '}') { // end of object
          return null;
        }
        throw reportError("skipObjField", "expected \" after {");
      case ',':
        c = nextToken();
        if (c != '"') {
          throw reportError("skipObjField", "expected string field, but " + c);
        }
        parse();
        if ((c = nextToken()) != ':') {
          throw reportError("skipObjField", "expected :, but " + c);
        }
        return this;
      case '}': // end of object
        return null;
      default:
        throw reportError("skipObjField", "expected { or , or } or n, but found: " + c);
    }
  }

  @Override
  public final JsonIterator closeObj() {
    final char c = nextToken();
    if (c == '}') {
      return this;
    }
    throw reportError("closeObj", "expected '}' but found: " + c);
  }

  @Override
  public final void testObject(final FieldBufferPredicate fieldBufferFunction) {
    char c;
    for (int offset, len; ; ) {
      switch ((c = nextToken())) {
        case 'n':
          skip(3);
          return;
        case '{':
          c = nextToken();
          if (c == '"') {
            offset = head;
            len = parse();
            if ((c = nextToken()) != ':') {
              throw reportError("testObject", "expected :, but " + c);
            }
            if (test(fieldBufferFunction, offset, len)) {
              continue;
            }
            return;
          }
          if (c == '}') { // end of object
            return;
          }
          throw reportError("testObject", "expected \" after {");
        case ',':
          c = nextToken();
          if (c != '"') {
            throw reportError("testObject", "expected string field, but " + c);
          }
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("testObject", "expected :, but " + c);
          }
          if (test(fieldBufferFunction, offset, len)) {
            continue;
          }
          return;
        case '}': // end of object
          return;
        default:
          throw reportError("testObject", "expected { or , or } or n, but found: " + c);
      }
    }
  }

  abstract boolean test(final FieldBufferPredicate fieldBufferFunction, final int offset, final int len);

  @Override
  public final <C> C testObject(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction) {
    char c;
    for (int offset, len; ; ) {
      switch ((c = nextToken())) {
        case 'n':
          skip(3);
          return context;
        case '{':
          c = nextToken();
          if (c == '"') {
            offset = head;
            len = parse();
            if ((c = nextToken()) != ':') {
              throw reportError("testObject", "expected :, but " + c);
            }
            if (test(context, fieldBufferFunction, offset, len)) {
              continue;
            }
            return context;
          }
          if (c == '}') { // end of object
            return context;
          }
          throw reportError("testObject", "expected \" after {");
        case ',':
          c = nextToken();
          if (c != '"') {
            throw reportError("testObject", "expected string field, but " + c);
          }
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("testObject", "expected :, but " + c);
          }
          if (test(context, fieldBufferFunction, offset, len)) {
            continue;
          }
          return context;
        case '}': // end of object
          return context;
        default:
          throw reportError("testObject", "expected { or , or } or n, but found: " + c);
      }
    }
  }

  abstract <C> boolean test(final C context,
                            final ContextFieldBufferPredicate<C> fieldBufferFunction,
                            final int offset, final int len);

  @Override
  public final <R> R applyObject(final FieldBufferFunction<R> fieldBufferFunction) {
    char c = nextToken();
    switch (c) {
      case 'n':
        skip(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          final int offset = head;
          final int len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("applyObject", "expected :, but " + c);
          }
          return apply(fieldBufferFunction, offset, len);
        }
        if (c == '}') { // end of object
          return null;
        }
        throw reportError("applyObject", "expected \" after {");
      case ',':
        c = nextToken();
        if (c != '"') {
          throw reportError("applyObject", "expected string field, but " + c);
        }
        final int offset = head;
        final int len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("applyObject", "expected :, but " + c);
        }
        return apply(fieldBufferFunction, offset, len);
      case '}': // end of object
        return null;
      default:
        throw reportError("applyObject", "expected { or , or } or n, but found: " + c);
    }
  }

  abstract <R> R apply(final FieldBufferFunction<R> fieldBufferFunction, final int offset, final int len);

  @Override
  public final <C, R> R applyObject(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction) {
    char c = nextToken();
    switch (c) {
      case 'n':
        skip(3);
        return null;
      case '{':
        c = nextToken();
        if (c == '"') {
          final int offset = head;
          final int len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("applyObject", "expected :, but " + c);
          }
          return apply(context, fieldBufferFunction, offset, len);
        }
        if (c == '}') { // end of object
          return null;
        }
        throw reportError("applyObject", "expected \" after {");
      case ',':
        c = nextToken();
        if (c != '"') {
          throw reportError("applyObject", "expected string field, but " + c);
        }
        final int offset = head;
        final int len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("applyObject", "expected :, but " + c);
        }
        return apply(context, fieldBufferFunction, offset, len);
      case '}': // end of object
        return null;
      default:
        throw reportError("applyObject", "expected { or , or } or n, but found: " + c);
    }
  }

  abstract <C, R> R apply(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction, final int offset, final int len);

  @Override
  public final double readDouble() {
    try {
      return Double.parseDouble(readNumberOrNumberString());
    } catch (final NumberFormatException e) {
      throw reportError("readDouble", e.toString());
    }
  }

  @Override
  public final float readFloat() {
    try {
      return Float.parseFloat(readNumberOrNumberString());
    } catch (final NumberFormatException e) {
      throw reportError("readFloat", e.toString());
    }
  }

  private static final CharBufferFunction<BigDecimal> READ_BIG_DECIMAL_FUNCTION = BigDecimal::new;
  private static final CharBufferFunction<BigDecimal> READ_BIG_DECIMAL_STRIP_TRAILING_ZEROES_FUNCTION = (chars, offset, len) -> {
    if (len == 1) {
      return chars[offset] == '0'
          ? BigDecimal.ZERO
          : new BigDecimal(chars, offset, len);
    }
    int pos = (offset + len) - 1;
    if (chars[pos] != '0') {
      return new BigDecimal(chars, offset, len);
    }
    char c = chars[--pos];
    while (c == '0') {
      if (pos == offset) {
        return BigDecimal.ZERO;
      }
      c = chars[--pos];
    }
    for (int j = pos; c != '.'; c = chars[--j]) {
      if ((c == 'e') || (c == 'E')) {
        return new BigDecimal(chars, offset, len).stripTrailingZeros();
      }
      if (j == offset) { // Not a decimal
        return new BigDecimal(chars, offset, len);
      }
    }
    return new BigDecimal(chars, offset, (pos + 1) - offset);
  };

  @Override
  public final BigDecimal readBigDecimal() {
    return readBigDecimal(READ_BIG_DECIMAL_FUNCTION);
  }

  @Override
  public final BigDecimal readBigDecimalStripTrailingZeroes() {
    return readBigDecimal(READ_BIG_DECIMAL_STRIP_TRAILING_ZEROES_FUNCTION);
  }

  private BigDecimal readBigDecimal(final CharBufferFunction<BigDecimal> parseChars) {
    final var valueType = whatIsNext();
    if (valueType == STRING) {
      return applyChars(parseChars);
    }
    if (valueType == NUMBER) {
      return applyNumberChars(parseChars);
    }
    if (valueType == NULL) {
      skip();
      return null;
    }
    throw reportError("readBigDecimal", "Must be a number, string or null but found " + valueType);
  }

  abstract BigDecimal applyNumberChars(final CharBufferFunction<BigDecimal> parseChars);

  private static final CharBufferFunction<BigInteger> READ_BIG_INTEGER_FUNCTION = (chars, offset, len) -> new BigInteger(new String(chars, offset, len));

  @Override
  public final BigInteger readBigInteger() {
    final var valueType = whatIsNext();
    if (valueType == NUMBER) {
      return new BigInteger(readNumberAsString());
    }
    if (valueType == STRING) {
      return applyChars(READ_BIG_INTEGER_FUNCTION);
    }
    if (valueType == NULL) {
      skip();
      return null;
    }
    throw reportError("readBigInteger", "Must be a number, string or null but found " + valueType);
  }

  @Override
  public String readNumberOrNumberString() {
    final var valueType = whatIsNext();
    if (valueType == NUMBER) {
      return readNumberAsString();
    }
    if (valueType == STRING) {
      return readString();
    }
    if (valueType == NULL) {
      skip();
      return null;
    }
    throw reportError("readNumberOrNumberString", "Must be a number, string or null but found " + valueType);
  }

  @Override
  public final ValueType whatIsNext() {
    return VALUE_TYPES[peekToken()];
  }

  private void skipString() {
    char c;
    for (int i = head; ; ++i) {
      if (i >= tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("skipString", "incomplete string");
        }
      }
      c = peekChar(i);
      if (c == '"') {
        head = i + 1;
        return;
      }
      if (c == '\\') {
        if (++i == tail) {
          if (loadMore()) {
            i = head;
          } else {
            throw reportError("skipString", "incomplete string");
          }
        }
      }
    }
  }

  private void skipUntilBreak() {
    for (int i = head; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          head = tail;
          return;
        }
      }
      switch (peekChar(i)) {
        case ' ':
        case '\t':
        case '\n':
        case '\r':
        case ',':
        case '}':
        case ']':
          head = i;
          return;
      }
    }
  }

  private void skipArray() {
    for (int i = head, level = 1; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("skipArray", "incomplete array");
        }
      }
      switch (peekChar(i)) {
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
  }

  private void skipObject() {
    for (int i = head, level = 1; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("skipObject", "incomplete object");
        }
      }
      switch (peekChar(i)) {
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
  }

  @Override
  public final JsonIterator skip() {
    final char c = nextToken();
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
        skip(3); // true or null
        return this;
      case 'f':
        skip(4); // false
        return this;
      case '[':
        skipArray();
        return this;
      case '{':
        skipObject();
        return this;
      default:
        throw reportError("skip", "do not know how to skip: " + c);
    }
//    return switch (c) {
//      case '"' ->skipString();
//      case '-','0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->skipUntilBreak();
//      case 't','n' ->skip(3); // true or null
//      case 'f' ->skip(4); // false
//      case '[' ->skipArray();
//      case '{' ->skipObject();
//      default ->throw reportError("skip", "do not know how to skip: " + c);
//    } ;
  }

  @Override
  public final boolean readNull() {
    final char c = peekToken();
    if (c == 'n') {
      skip(4); // null
      return true;
    }
    return false;
  }

  @Override
  public final boolean readBoolean() {
    final char c = nextToken();
    if ('t' == c) {
      skip(3); // true
      return true;
    }
    if ('f' == c) {
      skip(4); // false
      return false;
    }
    throw reportError("readBoolean", "expected t or f, found: " + c);
  }

  private void assertNotLeadingZero() {
    if (head == tail && !loadMore()) {
      return;
    }
    final int peek = peekChar();
    if (peek >= INT_DIGITS.length || INT_DIGITS[peek] == INVALID_CHAR_FOR_NUMBER) {
      return;
    }
    throw reportError("assertNotLeadingZero", "leading zero is invalid");
  }

  private int readIntSlowPath(int value) {
    value = -value; // add negatives to avoid redundant checks for Integer.MIN_VALUE on each iteration
    for (int i = head, ind; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          head = tail;
          return value;
        }
      }
      ind = peekIntDigitChar(i);
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
  }

  private int readInt(final char c) {
    int ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    }
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readInt", "expected 0~9");
    }
    if (tail - head > 9) {
      int i = head;
      final int ind2 = peekIntDigitChar(i);
      if (ind2 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        return -ind;
      }
      final int ind3 = peekIntDigitChar(++i);
      if (ind3 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10 + ind2;
        return -ind;
      }
      final int ind4 = peekIntDigitChar(++i);
      if (ind4 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100 + ind2 * 10 + ind3;
        return -ind;
      }
      final int ind5 = peekIntDigitChar(++i);
      if (ind5 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000 + ind2 * 100 + ind3 * 10 + ind4;
        return -ind;
      }
      final int ind6 = peekIntDigitChar(++i);
      if (ind6 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10000 + ind2 * 1000 + ind3 * 100 + ind4 * 10 + ind5;
        return -ind;
      }
      final int ind7 = peekIntDigitChar(++i);
      if (ind7 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100000 + ind2 * 10000 + ind3 * 1000 + ind4 * 100 + ind5 * 10 + ind6;
        return -ind;
      }
      final int ind8 = peekIntDigitChar(++i);
      if (ind8 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000000 + ind2 * 100000 + ind3 * 10000 + ind4 * 1000 + ind5 * 100 + ind6 * 10 + ind7;
        return -ind;
      }
      final int ind9 = peekIntDigitChar(++i);
      ind = ind * 10000000 + ind2 * 1000000 + ind3 * 100000 + ind4 * 10000 + ind5 * 1000 + ind6 * 100 + ind7 * 10 + ind8;
      head = i;
      if (ind9 == INVALID_CHAR_FOR_NUMBER) {
        return -ind;
      }
    }
    return readIntSlowPath(ind);
  }

  @Override
  public final int readInt() {
    final char c = nextToken();
    if (c == '-') {
      return readInt(readChar());
    }
    final int val = readInt(c);
    if (val == Integer.MIN_VALUE) {
      throw reportError("readInt", "value is too large for int");
    }
    return -val;
  }

  @Override
  public final short readShort() {
    final int v = readInt();
    if (Short.MIN_VALUE <= v && v <= Short.MAX_VALUE) {
      return (short) v;
    }
    throw reportError("readShort", "short overflow: " + v);
  }

  private long readLongSlowPath(long value) {
    value = -value; // add negatives to avoid redundant checks for Long.MIN_VALUE on each iteration
    for (int i = head, ind; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          head = tail;
          return value;
        }
      }
      ind = peekIntDigitChar(i);
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
  }

  private long readLong(final char c) {
    long ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    }
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readLong", "expected 0~9");
    }
    if (tail - head > 9) {
      int i = head;
      final int ind2 = peekIntDigitChar(i);
      if (ind2 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        return -ind;
      }
      final int ind3 = peekIntDigitChar(++i);
      if (ind3 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10 + ind2;
        return -ind;
      }
      final int ind4 = peekIntDigitChar(++i);
      if (ind4 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100 + ind2 * 10 + ind3;
        return -ind;
      }
      final int ind5 = peekIntDigitChar(++i);
      if (ind5 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000 + ind2 * 100 + ind3 * 10 + ind4;
        return -ind;
      }
      final int ind6 = peekIntDigitChar(++i);
      if (ind6 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 10000 + ind2 * 1000 + ind3 * 100 + ind4 * 10 + ind5;
        return -ind;
      }
      final int ind7 = peekIntDigitChar(++i);
      if (ind7 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 100000 + ind2 * 10000 + ind3 * 1000 + ind4 * 100 + ind5 * 10 + ind6;
        return -ind;
      }
      final int ind8 = peekIntDigitChar(++i);
      if (ind8 == INVALID_CHAR_FOR_NUMBER) {
        head = i;
        ind = ind * 1000000 + ind2 * 100000 + ind3 * 10000 + ind4 * 1000 + ind5 * 100 + ind6 * 10 + ind7;
        return -ind;
      }
      final int ind9 = peekIntDigitChar(++i);
      ind = ind * 10000000 + ind2 * 1000000 + ind3 * 100000 + ind4 * 10000 + ind5 * 1000 + ind6 * 100 + ind7 * 10 + ind8;
      head = i;
      if (ind9 == INVALID_CHAR_FOR_NUMBER) {
        return -ind;
      }
    }
    return readLongSlowPath(ind);
  }

  @Override
  public final long readLong() {
    char c = nextToken();
    if (c == '-') {
      c = readChar();
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
  public final boolean readArray() {
    final char c = nextToken();
    if (c == '[') {
      if (peekToken() == ']') {
        head++;
        return false;
      }
      return true;
    }
    if (c == ']' || c == 'n') {
      return false;
    }
    if (c == ',') {
      return true;
    }
    throw reportError("readArray", "expected [ or , or n or ], but found: " + c);
  }

  @Override
  public final String readNumberAsString() {
    return parsedNumberAsString(parseNumber());
  }

  abstract int parseNumber();

  abstract String parsedNumberAsString(final int len);
}
