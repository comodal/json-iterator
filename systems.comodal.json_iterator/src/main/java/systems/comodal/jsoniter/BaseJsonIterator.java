package systems.comodal.jsoniter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import static java.time.Instant.ofEpochSecond;
import static systems.comodal.jsoniter.ContextFieldBufferMaskedPredicate.BREAK_OUT;
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
    if (c == ',') {
      final var field = readField();
      if ((c = nextToken()) != ':') {
        throw reportError("readObjField", "expected :, but " + c);
      }
      return field;
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final var field = parse(READ_STRING_FUNCTION);
        if ((c = nextToken()) != ':') {
          throw reportError("readObjField", "expected :, but " + c);
        }
        return field;
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
      if ((c = nextToken()) != ':') {
        throw reportError("skipObjField", "expected :, but " + c);
      }
      return this;
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        parse();
        if ((c = nextToken()) != ':') {
          throw reportError("skipObjField", "expected :, but " + c);
        }
        return this;
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
      throw reportError("skipObjField", "expected [\\{\\}n], but found: " + c);
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
        throw reportError("testObject", "expected [\\{\\}n], but found: " + c);
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
        throw reportError("testObject", "expected [\\{\\}n], but found: " + c);
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
        throw reportError("testObject", "expected [\\{\\}n], but found: " + c);
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
      }
      return apply(fieldBufferFunction, offset, len);
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final int offset = head;
        final int len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("applyObject", "expected :, but " + c);
        }
        return apply(fieldBufferFunction, offset, len);
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
      throw reportError("applyObject", "expected [\\{\\}n], but found: " + c);
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
      }
      return apply(context, fieldBufferFunction, offset, len);
    } else if (c == '{') {
      c = nextToken();
      if (c == '"') {
        final int offset = head;
        final int len = parse();
        if ((c = nextToken()) != ':') {
          throw reportError("applyObject", "expected :, but " + c);
        }
        return apply(context, fieldBufferFunction, offset, len);
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
      throw reportError("applyObject", "expected [\\{\\}n], but found: " + c);
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

  private static final int SECONDS_PER_HOUR = 60 * 60;
  /**
   * The number of days in a 400 year cycle.
   */
  private static final int DAYS_PER_CYCLE = 146097;
  /**
   * The number of days from year zero to year 1970.
   * There are five 400 year cycles from year zero to 2000.
   * There are 7 leap years from 1970 to 2000.
   */
  private static final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

  private static long toEpochSecond(final long year,
                                    final long month,
                                    final int day,
                                    final int hour,
                                    final int minute,
                                    final int second) {
    long total = 365 * year;
    if (year >= 0) {
      total += (year + 3) / 4 - (year + 99) / 100 + (year + 399) / 400;
    } else {
      total -= year / -4 - year / -100 + year / -400;
    }
    total += ((367 * month - 362) / 12);
    total += day - 1;
    if (month > 2) {
      total--;
      if (!IsoChronology.INSTANCE.isLeapYear(year)) {
        total--;
      }
    }
    return (86400 * (total - DAYS_0000_TO_1970))
        + (hour * SECONDS_PER_HOUR)
        + (minute * 60)
        + second;
  }

  private static DateTimeParseException throwDateTimeParseException(final String context,
                                                                    final char[] buf,
                                                                    final int begin,
                                                                    final int len,
                                                                    final int offset) {
    final var dateTime = new String(buf, begin, len);
    throw new DateTimeParseException(context + '[' + dateTime + ']', dateTime, offset);
  }

  private static final CharBufferFunction<Instant> RFC_1123_INSTANT_PARSER = (buf, offset, len) -> {
    int i = offset + 5;
    int day = INT_DIGITS[buf[i]];
    if (day == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid day ", buf, offset, len, i - offset);
    }
    int ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid day ", buf, offset, len, i - offset);
    }
    day = (day << 3) + (day << 1) + ind;

    i += 2;
    char a = buf[i];
    char b = buf[++i];
    char c = buf[++i];
    final int month;
    if (a == 'J') {
      if (b == 'a' && c == 'n') {
        month = 1;
      } else if (b == 'u') {
        if (c == 'l') {
          month = 7;
        } else if (c == 'n') {
          month = 6;
        } else {
          throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
        }
      } else {
        throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
      }
    } else if (a == 'M') {
      if (b == 'a') {
        if (c == 'r') {
          month = 3;
        } else if (c == 'y') {
          month = 5;
        } else {
          throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
        }
      } else {
        throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
      }
    } else if (a == 'A') {
      if (b == 'p' && c == 'r') {
        month = 4;
      } else if (b == 'u' && c == 'g') {
        month = 8;
      } else {
        throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
      }
    } else if (a == 'F' && b == 'e' && c == 'b') {
      month = 2;
    } else if (a == 'S' && b == 'e' && c == 'p') {
      month = 9;
    } else if (a == 'O' && b == 'c' && c == 't') {
      month = 10;
    } else if (a == 'N' && b == 'o' && c == 'v') {
      month = 11;
    } else if (a == 'D' && b == 'e' && c == 'c') {
      month = 12;
    } else {
      throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
    }
    i += 2;
    int year = INT_DIGITS[buf[i]];
    if (year == INVALID_CHAR_FOR_NUMBER) {

      throw throwDateTimeParseException("Invalid year ", buf, offset, len, 0);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid year ", buf, offset, len, i - offset);
    }
    year = (year << 3) + (year << 1) + ind;
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid year ", buf, offset, len, i - offset);
    }
    year = (year << 3) + (year << 1) + ind;
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid year ", buf, offset, len, i - offset);
    }
    year = (year << 3) + (year << 1) + ind;
    i += 2;
    int hour = INT_DIGITS[buf[i]];
    if (hour == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid hour ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid hour ", buf, offset, len, i - offset);
    }
    hour = (hour << 3) + (hour << 1) + ind;
    i += 2;
    int minute = INT_DIGITS[buf[i]];
    if (minute == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid minute ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid minute ", buf, offset, len, i - offset);
    }
    minute = (minute << 3) + (minute << 1) + ind;
    i += 2;
    int second = INT_DIGITS[buf[i]];
    if (second == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid second ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid second ", buf, offset, len, i - offset);
    }
    second = (second << 3) + (second << 1) + ind;
    i += 2;
    final var zone = ZoneId.of(new String(buf, i, (offset + len) - i));
    return ZonedDateTime.of(year, month, day, hour, minute, second, 0, zone).toInstant();
  };

  private static final CharBufferFunction<Instant> INSTANT_PARSER = (buf, offset, len) -> {
    if (len < 19) {
      if (len == 0) {
        return null;
      } else {
        throw throwDateTimeParseException(String.format("Invalid length, %d, expected at least 19 characters", len), buf, offset, len, 0);
      }
    }
    int i = offset;
    char c = buf[i];
    int year = INT_DIGITS[c];
    if (year == INVALID_CHAR_FOR_NUMBER) {
      if (c == 'S' || c == 'T' || c == 'M' || c == 'W' || c == 'F') {
        return RFC_1123_INSTANT_PARSER.apply(buf, offset, len);
      } else {
        throw throwDateTimeParseException("Invalid year ", buf, offset, len, 0);
      }
    }
    int ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid year ", buf, offset, len, i - offset);
    }
    year = (year << 3) + (year << 1) + ind;
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid year ", buf, offset, len, i - offset);
    }
    year = (year << 3) + (year << 1) + ind;
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid year ", buf, offset, len, i - offset);
    }
    year = (year << 3) + (year << 1) + ind;
    i += 2;
    int month = INT_DIGITS[buf[i]];
    if (month == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid month ", buf, offset, len, i - offset);
    }
    month = (month << 3) + (month << 1) + ind;
    i += 2;
    int day = INT_DIGITS[buf[i]];
    if (day == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid day ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid day ", buf, offset, len, i - offset);
    }
    day = (day << 3) + (day << 1) + ind;
    i += 2;
    int hour = INT_DIGITS[buf[i]];
    if (hour == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid hour ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid hour ", buf, offset, len, i - offset);
    }
    hour = (hour << 3) + (hour << 1) + ind;
    i += 2;
    int minute = INT_DIGITS[buf[i]];
    if (minute == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid minute ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid minute ", buf, offset, len, i - offset);
    }
    minute = (minute << 3) + (minute << 1) + ind;
    i += 2;
    int second = INT_DIGITS[buf[i]];
    if (second == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid second ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid second ", buf, offset, len, i - offset);
    }
    second = (second << 3) + (second << 1) + ind;
    final int max = offset + len;
    if (++i == max) {
      return ofEpochSecond(toEpochSecond(year, month, day, hour, minute, second), 0);
    }
    c = buf[i];
    if (c == '.') {
      c = buf[++i];
    } else {
      final int offsetSeconds;
      if (c == 'Z') {
        offsetSeconds = 0;
      } else if (c == '-') {
        offsetSeconds = -parseOffset(buf, i, offset, len, max);
      } else if (c == '+') {
        offsetSeconds = parseOffset(buf, i, offset, len, max);
      } else {
        throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
      }
      return ofEpochSecond(toEpochSecond(year, month, day, hour, minute, second) - offsetSeconds, 0);
    }
    int nano = INT_DIGITS[c];
    if (nano == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
    }
    int nanoDigitCount = 1;
    int offsetSeconds = 0;
    while (++i < max) {
      c = buf[i];
      ind = INT_DIGITS[c];
      if (ind == INVALID_CHAR_FOR_NUMBER) {
        if (c == 'Z') {
          break;
        } else if (c == '-') {
          offsetSeconds = -parseOffset(buf, i, offset, len, max);
          break;
        } else if (c == '+') {
          offsetSeconds = parseOffset(buf, i, offset, len, max);
          break;
        } else {
          throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
        }
      }
      nano = (nano << 3) + (nano << 1) + ind;
      nanoDigitCount++;
    }
    while (nanoDigitCount++ < 9) {
      nano = (nano << 3) + (nano << 1);
    }
    return ofEpochSecond(toEpochSecond(year, month, day, hour, minute, second) - offsetSeconds, nano);
  };

  private static int parseOffset(final char[] buf,
                                 int i,
                                 final int offset,
                                 final int len,
                                 final int max) {
    int hourOffset = INT_DIGITS[buf[++i]];
    if (hourOffset == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
    }
    int ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
    }
    hourOffset = SECONDS_PER_HOUR * ((hourOffset << 3) + (hourOffset << 1) + ind);
    if (++i == max) {
      return hourOffset;
    }
    int minuteOffset = INT_DIGITS[buf[++i]];
    if (minuteOffset == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
    }
    ind = INT_DIGITS[buf[++i]];
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw throwDateTimeParseException("Invalid offset ", buf, offset, len, i - offset);
    }
    minuteOffset = 60 * ((minuteOffset << 3) + (minuteOffset << 1) + ind);
    return hourOffset + minuteOffset;
  }

  @Override
  public final Instant readDateTime() {
    return applyChars(INSTANT_PARSER);
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
      } else if (c == '\\') {
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
        skipString();
        i = head - 1; // it will be i++ soon
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
        skipString();
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
