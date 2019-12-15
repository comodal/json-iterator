package systems.comodal.jsoniter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;

import static systems.comodal.jsoniter.ContextFieldBufferMaskedPredicate.BREAK_OUT;
import static systems.comodal.jsoniter.ValueType.*;

abstract class BaseJsonIterator implements JsonIterator {

  static final int INVALID_CHAR_FOR_NUMBER = -1;
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
    final var peek = getBufferString(head <= 10 ? 0 : head - 10, Math.min(head, tail));
    throw new JsonException(op + ": " + msg + ", head: " + head + ", peek: " + peek + ", buf: " + getBufferString(0, 1_024));
  }

  @Override
  public final String currentBuffer() {
    final var peek = getBufferString(head <= 10 ? 0 : head - 10, head);
    return "head: " + head + ", peek: " + peek + ", buf: " + getBufferString(0, 1_024);
  }

  abstract char readChar();

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

  @Override
  public final String readString() {
    final char c = nextToken();
    if (c == '"') {
      return parseString();
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("readString", "expected string or null, but " + c);
    }
  }

  abstract int parse();

  abstract void skipPastEndQuote();

  @Override
  public final <R> R applyChars(final CharBufferFunction<R> applyChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(applyChars);
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("applyChars", "expected string or null, but " + c);
    }
  }

  abstract <R> R parse(final CharBufferFunction<R> applyChars);

  @Override
  public final <C, R> R applyChars(final C context, final ContextCharBufferFunction<C, R> applyChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(context, applyChars);
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("applyChars", "expected string or null, but " + c);
    }
  }

  abstract <C, R> R parse(final C context, final ContextCharBufferFunction<C, R> applyChars);

  @Override
  public final boolean testChars(final CharBufferPredicate testChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(testChars);
    } else if (c == 'n') {
      skip(3);
      return false;
    } else {
      throw reportError("testChars", "expected string or null, but " + c);
    }
  }

  abstract boolean parse(final CharBufferPredicate testChars);

  @Override
  public final <C> boolean testChars(final C context, final ContextCharBufferPredicate<C> testChars) {
    final char c = nextToken();
    if (c == '"') {
      return parse(context, testChars);
    } else if (c == 'n') {
      skip(3);
      return false;
    } else {
      throw reportError("testChars", "expected string or null, but " + c);
    }
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
      if ((c = nextToken()) == ',') {
        c = nextToken();
        if (c != '"') {
          throw reportError("skipUntil", "expected string field, but " + c);
        } else {
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("skipUntil", "expected :, but " + c);
          } else if (fieldEquals(field, offset, len)) {
            return this;
          } else {
            skip();
          }
        }
      } else if (c == '{') {
        c = nextToken();
        if (c == '"') {
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("skipUntil", "expected :, but " + c);
          } else if (fieldEquals(field, offset, len)) {
            return this;
          } else {
            skip();
          }
        } else if (c == '}') { // end of object
          return null;
        } else {
          throw reportError("skipUntil", "expected \" after {");
        }
      } else if (c == '}') {
        return null;
      } else {
        throw reportError("skipUntil", "expected [\\{\\}n], but found: " + c);
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
    if (c == ',') {
      final boolean result = testField(testField);
      if ((c = nextToken()) != ':') {
        throw reportError("testObjField", "expected :, but " + c);
      }
      return result;
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final boolean result = parse(testField);
        if ((c = nextToken()) != ':') {
          throw reportError("testObjField", "expected :, but " + c);
        }
        return result;
      } else if (c == '}') {
        return false; // end of object
      } else {
        throw reportError("testObjField", "expected \" after {");
      }
    } else if (c == '}') {
      return false; // end of object
    } else if (c == 'n') {
      skip(3);
      return false;
    } else {
      throw reportError("testObjField", "expected [\\{\\}n], but found: " + c);
    }
  }

  protected static final CharBufferFunction<String> READ_STRING_FUNCTION = String::new;

  protected String parseString() {
    return parse(READ_STRING_FUNCTION);
  }

  private String readField() {
    final char c = nextToken();
    if (c == '"') {
      return parseString();
    } else {
      throw reportError("readField", "expected field string, but " + c);
    }
  }

  @Override
  public final String readObjField() {
    char c = nextToken();
    if (c == ',') {
      final var field = readField();
      if ((c = nextToken()) == ':') {
        return field;
      } else {
        throw reportError("readObjField", "expected :, but " + c);
      }
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final var field = parseString();
        if ((c = nextToken()) == ':') {
          return field;
        } else {
          throw reportError("readObjField", "expected :, but " + c);
        }
      } else if (c == '}') {
        return null; // end of object
      } else {
        throw reportError("readObjField", "expected \" after {");
      }
    } else if (c == '}') {
      return null; // end of object
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("readObjField", "expected [\\{\\}n], but found: " + c);
    }
  }

  public final JsonIterator skipObjField() {
    char c = nextToken();
    if (c == ',') {
      c = nextToken();
      if (c != '"') {
        throw reportError("skipObjField", "expected string field, but " + c);
      }
      parse();
      if ((c = nextToken()) == ':') {
        return this;
      } else {
        throw reportError("skipObjField", "expected :, but " + c);
      }
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        parse();
        if ((c = nextToken()) == ':') {
          return this;
        } else {
          throw reportError("skipObjField", "expected :, but " + c);
        }
      } else if (c == '}') { // end of object
        return null;
      } else {
        throw reportError("skipObjField", "expected \" after {");
      }
    } else if (c == '}') { // end of object
      return null;
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("skipObjField", "expected [,{}n], but found: " + c);
    }
  }

  @Override
  public final JsonIterator closeObj() {
    final char c = nextToken();
    if (c == '}') {
      return this;
    } else {
      throw reportError("closeObj", "expected '}' but found: " + c);
    }
  }

  @Override
  public final void testObject(final FieldBufferPredicate fieldBufferFunction) {
    char c;
    for (int offset, len; ; ) {
      if ((c = nextToken()) == ',') {
        c = nextToken();
        if (c != '"') {
          throw reportError("testObject", "expected string field, but " + c);
        } else {
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("testObject", "expected :, but " + c);
          } else if (breakOut(fieldBufferFunction, offset, len)) {
            return;
          }
        }
      } else if (c == '{') {
        c = nextToken();
        if (c == '"') {
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("testObject", "expected :, but " + c);
          } else if (breakOut(fieldBufferFunction, offset, len)) {
            return;
          }
        } else if (c == '}') { // end of object
          return;
        } else {
          throw reportError("testObject", "expected \" after {");
        }
      } else if (c == '}') {
        return;
      } else if (c == 'n') {
        skip(3);
        return;
      } else {
        throw reportError("testObject", "expected [,{}n], but found: " + c);
      }
    }
  }

  abstract boolean breakOut(final FieldBufferPredicate fieldBufferFunction, final int offset, final int len);

  @Override
  public final <C> C testObject(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction) {
    char c;
    for (int offset, len; ; ) {
      if ((c = nextToken()) == ',') {
        c = nextToken();
        if (c != '"') {
          throw reportError("testObject", "expected string field, but " + c);
        }
        offset = head;
        len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("testObject", "expected :, but " + c);
        } else if (breakOut(context, fieldBufferFunction, offset, len)) {
          return context;
        }
      } else if (c == '{') {
        c = nextToken();
        if (c == '"') {
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("testObject", "expected :, but " + c);
          } else if (breakOut(context, fieldBufferFunction, offset, len)) {
            return context;
          }
        } else if (c == '}') { // end of object
          return context;
        } else {
          throw reportError("testObject", "expected \" after {");
        }
      } else if (c == '}') {
        return context;
      } else if (c == 'n') {
        skip(3);
        return context;
      } else {
        throw reportError("testObject", "expected [,{}n], but found: " + c);
      }
    }
  }

  abstract <C> boolean breakOut(final C context,
                                final ContextFieldBufferPredicate<C> fieldBufferFunction,
                                final int offset, final int len);

  @Override
  public final <C> C testObject(final C context, final ContextFieldBufferMaskedPredicate<C> fieldBufferFunction) {
    char c;
    long mask = 0;
    for (int offset, len; ; ) {
      if ((c = nextToken()) == ',') {
        c = nextToken();
        if (c != '"') {
          throw reportError("testObject", "expected string field, but " + c);
        }
        offset = head;
        len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("testObject", "expected :, but " + c);
        } else if ((mask = test(context, mask, fieldBufferFunction, offset, len)) == BREAK_OUT) {
          return context;
        }
      } else if (c == '{') {
        c = nextToken();
        if (c == '"') {
          offset = head;
          len = parse();
          if ((c = nextToken()) != ':') {
            throw reportError("testObject", "expected :, but " + c);
          } else if ((mask = test(context, mask, fieldBufferFunction, offset, len)) == BREAK_OUT) {
            return context;
          }
        } else if (c == '}') { // end of object
          return context;
        } else {
          throw reportError("testObject", "expected \" after {");
        }
      } else if (c == '}') {
        return context;
      } else if (c == 'n') {
        skip(3);
        return context;
      } else {
        throw reportError("testObject", "expected [,{}n], but found: " + c);
      }
    }
  }

  abstract <C> long test(final C context,
                         final long mask,
                         final ContextFieldBufferMaskedPredicate<C> fieldBufferFunction,
                         final int offset, final int len);

  @Override
  public final <R> R applyObject(final FieldBufferFunction<R> fieldBufferFunction) {
    char c = nextToken();
    if (c == ',') {
      c = nextToken();
      if (c != '"') {
        throw reportError("applyObject", "expected string field, but " + c);
      }
      final int offset = head;
      final int len = parse();
      if ((c = nextToken()) != ':') {
        throw reportError("applyObject", "expected :, but " + c);
      } else {
        return apply(fieldBufferFunction, offset, len);
      }
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final int offset = head;
        final int len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("applyObject", "expected :, but " + c);
        } else {
          return apply(fieldBufferFunction, offset, len);
        }
      } else if (c == '}') { // end of object
        return null;
      } else {
        throw reportError("applyObject", "expected \" after {");
      }
    } else if (c == '}') {
      return null;
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("applyObject", "expected [,{}n], but found: " + c);
    }
  }

  abstract <R> R apply(final FieldBufferFunction<R> fieldBufferFunction, final int offset, final int len);

  @Override
  public final <C, R> R applyObject(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction) {
    char c = nextToken();
    if (c == ',') {
      c = nextToken();
      if (c != '"') {
        throw reportError("applyObject", "expected string field, but " + c);
      }
      final int offset = head;
      final int len = parse();
      if ((c = nextToken()) != ':') {
        throw reportError("applyObject", "expected :, but " + c);
      } else {
        return apply(context, fieldBufferFunction, offset, len);
      }
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final int offset = head;
        final int len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("applyObject", "expected :, but " + c);
        } else {
          return apply(context, fieldBufferFunction, offset, len);
        }
      } else if (c == '}') { // end of object
        return null;
      } else {
        throw reportError("applyObject", "expected \" after {");
      }
    } else if (c == '}') {
      return null;
    } else if (c == 'n') {
      skip(3);
      return null;
    } else {
      throw reportError("applyObject", "expected [,{}n], but found: " + c);
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

  private static final CharBufferFunction<BigDecimal> READ_BIG_DECIMAL_FUNCTION = (chars, offset, len) -> len == 0 ? null : new BigDecimal(chars, offset, len);
  private static final CharBufferFunction<BigDecimal> READ_BIG_DECIMAL_STRIP_TRAILING_ZEROES_FUNCTION = (chars, offset, len) -> {
    if (len == 1) {
      return chars[offset] == '0'
          ? BigDecimal.ZERO
          : new BigDecimal(chars, offset, len);
    } else if (len == 0) {
      return null;
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
      } else if (j == offset) { // Not a decimal
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
    } else if (valueType == NUMBER) {
      return parseBigDecimal(parseChars);
    } else if (valueType == NULL) {
      skip();
      return null;
    } else {
      throw reportError("readBigDecimal", "Must be a number, string or null but found " + valueType);
    }
  }

  abstract BigDecimal parseBigDecimal(final CharBufferFunction<BigDecimal> parseChars);

  private static final CharBufferFunction<BigInteger> READ_BIG_INTEGER_FUNCTION = (chars, offset, len) -> new BigInteger(new String(chars, offset, len));

  @Override
  public final BigInteger readBigInteger() {
    final var valueType = whatIsNext();
    if (valueType == NUMBER) {
      return new BigInteger(readNumberAsString());
    } else if (valueType == STRING) {
      return applyChars(READ_BIG_INTEGER_FUNCTION);
    } else if (valueType == NULL) {
      skip();
      return null;
    } else {
      throw reportError("readBigInteger", "Must be a number, string or null but found " + valueType);
    }
  }

  @Override
  public final Instant readDateTime() {
    return applyChars(InstantParser.INSTANT_PARSER);
  }

  @Override
  public String readNumberOrNumberString() {
    final var valueType = whatIsNext();
    if (valueType == NUMBER) {
      return readNumberAsString();
    } else if (valueType == STRING) {
      return readString();
    } else if (valueType == NULL) {
      skip();
      return null;
    } else {
      throw reportError("readNumberOrNumberString", "Must be a number, string or null but found " + valueType);
    }
  }

  @Override
  public final ValueType whatIsNext() {
    return VALUE_TYPES[peekToken()];
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
    char c;
    for (int i = head, level = 1; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("skipArray", "incomplete array");
        }
      }
      if ((c = peekChar(i)) == '"') { // If inside string, skip it
        head = i + 1;
        skipPastEndQuote();
        i = head - 1;
      } else if (c == '[') { // If open symbol, increase level
        level++;
      } else if (c == ']') { // If close symbol, increase level
        level--;
        // If we have returned to the original level, we're done
        if (level == 0) {
          head = i + 1;
          return;
        }
      }
    }
  }

  private void skipObject() {
    char c;
    for (int i = head, level = 1; ; i++) {
      if (i == tail) {
        if (loadMore()) {
          i = head;
        } else {
          throw reportError("skipObject", "incomplete object");
        }
      }
      if ((c = peekChar(i)) == '"') { // If inside string, skip it
        head = i + 1;
        skipPastEndQuote();
        i = head - 1; // it will be i++ soon
      } else if (c == '{') { // If open symbol, increase level
        level++;
      } else if (c == '}') { // If close symbol, increase level
        level--;
        // If we have returned to the original level, we're done
        if (level == 0) {
          head = i + 1;
          return;
        }
      }
    }
  }

  @Override
  public final JsonIterator skip() {
    final char c = nextToken();
    switch (c) {
      case '"':
        skipPastEndQuote();
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
//      case '"' ->skipPastEndQuote();
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
    if (c == 't') {
      skip(3); // true
      return true;
    } else if (c == 'f') {
      skip(4); // false
      return false;
    } else {
      throw reportError("readBoolean", "expected t or f, found: " + c);
    }
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
      } else if (value < -214748364) { // limit / 10
        throw reportError("readIntSlowPath", "value is too large for int");
      } else {
        value = (value << 3) + (value << 1) - ind;
        if (value >= 0) {
          throw reportError("readIntSlowPath", "value is too large for int");
        }
      }
    }
  }

  private int readInt(final char c) {
    int ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    } else if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readInt", "expected 0~9");
    } else if (tail - head > 9) {
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
      } else if (value < -922337203685477580L) { // limit / 10
        throw reportError("readLongSlowPath", "value is too large for long");
      } else {
        value = (value << 3) + (value << 1) - ind;
        if (value >= 0) {
          throw reportError("readLongSlowPath", "value is too large for long");
        }
      }
    }
  }

  private long readLong(final char c) {
    long ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    } else if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readLong", "expected 0~9");
    } else if (tail - head > 9) {
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
    } else if (INT_DIGITS[c] == 0) {
      assertNotLeadingZero();
      return 0;
    } else {
      final long val = readLong(c);
      if (val == Long.MIN_VALUE) {
        throw reportError("readLong", "value is too large for long");
      }
      return -val;
    }
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
    } else if (c == ']' || c == 'n') {
      return false;
    } else if (c == ',') {
      return true;
    } else {
      throw reportError("readArray", "expected [ or , or n or ], but found: " + c);
    }
  }

  @Override
  public final String readNumberAsString() {
    return parsedNumberAsString(parseNumber());
  }

  abstract int parseNumber();

  abstract String parsedNumberAsString(final int len);

  @Override
  public final <R> R applyNumberChars(final CharBufferFunction<R> applyChars) {
    return parseNumber(applyChars, parseNumber());
  }

  abstract <R> R parseNumber(final CharBufferFunction<R> applyChars, final int len);

  @Override
  public final <C, R> R applyNumberChars(final C context, final ContextCharBufferFunction<C, R> applyChars) {
    return parseNumber(context, applyChars, parseNumber());
  }

  abstract <C, R> R parseNumber(final C context,
                                final ContextCharBufferFunction<C, R> applyChars,
                                final int len);
}
