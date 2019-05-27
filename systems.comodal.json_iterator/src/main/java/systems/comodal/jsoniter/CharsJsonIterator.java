package systems.comodal.jsoniter;

import java.io.InputStream;
import java.math.BigDecimal;

final class CharsJsonIterator extends BaseJsonIterator {

  char[] buf;

  CharsJsonIterator(final char[] buf, final int head, final int tail) {
    super(head, tail);
    this.buf = buf;
  }

  @Override
  public boolean supportsMarkReset() {
    return true;
  }

  @Override
  public JsonIterator reset(final byte[] buf) {
    return JsonIterator.parse(buf);
  }

  @Override
  public JsonIterator reset(final byte[] buf, final int head, final int tail) {
    return JsonIterator.parse(buf, head, tail);
  }

  @Override
  public JsonIterator reset(final char[] buf) {
    this.buf = buf;
    this.head = 0;
    this.tail = buf.length;
    return this;
  }

  @Override
  public JsonIterator reset(final char[] buf, final int head, final int tail) {
    this.buf = buf;
    this.head = head;
    this.tail = tail;
    return this;
  }

  @Override
  public JsonIterator reset(final InputStream in) {
    return JsonIterator.parse(in, buf.length);
  }

  @Override
  public JsonIterator reset(final InputStream in, final int bufSize) {
    return JsonIterator.parse(in, bufSize);
  }

  @Override
  public void close() {
  }

  @Override
  String getBufferString(final int from, final int to) {
    return new String(buf, from, Math.min(to, tail) - from);
  }

  @Override
  char nextToken() {
    char c;
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

  @Override
  char peekToken() {
    char c;
    for (int i = head; ; i++) {
      c = buf[i];
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

  @Override
  char readChar() {
    return buf[head++];
  }

  @Override
  int readAsInt() {
    return buf[head++];
  }

  @Override
  char peekChar() {
    return buf[head];
  }

  @Override
  char peekChar(final int offset) {
    return buf[offset];
  }

  @Override
  int peekIntDigitChar(final int offset) {
    return INT_DIGITS[buf[offset]];
  }

  @Override
  int parse() {
    return parse(head);
  }

  private int numEscapes = 0;

  private int parse(final int from) {
    char c;
    numEscapes = 0;
    for (int i = from; ; i++) {
      if (i >= tail) {
        throw reportError("parse", "incomplete string");
      }
      c = peekChar(i);
      if (c == '"') {
        head = i + 1;
        return i - from;
      }
      if (c == '\\') {
        ++numEscapes;
        ++i;
      }
    }
  }

  @Override
  final int parseNumber() {
    for (int i = head, len = 0; ; i++) {
      if (i == tail) {
        head = tail;
        return len;
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
          len++;
          continue;
        default:
          head = i;
          return len;
      }
    }
  }

  private char[] handleEscapes(final int from, final int len) {
    final char[] chars = new char[len - numEscapes];
    char c;
    for (int i = 0, j = from; i < chars.length; i++, j++) {
      c = buf[j];
      if (c == '\\') {
        c = buf[++j];
      }
      chars[i] = c;
    }
    return chars;
  }

  @Override
  final <R> R parse(final CharBufferFunction<R> applyChars) {
    final int from = head;
    final int len = parse(from);
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(from, len);
      return applyChars.apply(chars, 0, chars.length);
    }
    return applyChars.apply(buf, from, len);
  }

  @Override
  final <C, R> R parse(final C context, final ContextCharBufferFunction<C, R> applyChars) {
    final int from = head;
    final int len = parse(from);
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(from, len);
      return applyChars.apply(context, chars, 0, chars.length);
    }
    return applyChars.apply(context, buf, from, len);
  }

  @Override
  final boolean parse(final CharBufferPredicate testChars) {
    final int from = head;
    final int len = parse(from);
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(from, len);
      return testChars.apply(chars, 0, chars.length);
    }
    return testChars.apply(buf, from, len);
  }

  @Override
  final <C> boolean parse(final C context, final ContextCharBufferPredicate<C> testChars) {
    final int from = head;
    final int len = parse(from);
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(from, len);
      return testChars.apply(context, chars, 0, chars.length);
    }
    return testChars.apply(context, buf, from, len);
  }

  @Override
  final void parse(final CharBufferConsumer testChars) {
    final int from = head;
    final int len = parse(from);
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(from, len);
      testChars.accept(chars, 0, chars.length);
    }
    testChars.accept(buf, from, len);
  }

  @Override
  final <C> void parse(final C context, final ContextCharBufferConsumer<C> testChars) {
    final int from = head;
    final int len = parse(from);
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(from, len);
      testChars.accept(context, chars, 0, chars.length);
    }
    testChars.accept(context, buf, from, len);
  }

  @Override
  final boolean fieldEquals(final String field, final int offset, final int len) {
    return JsonIterator.fieldEquals(field, buf, offset, len);
  }

  @Override
  final boolean test(final FieldBufferPredicate fieldBufferFunction, final int offset, final int len) {
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(offset, len);
      return fieldBufferFunction.test(chars, 0, chars.length, this);
    }
    return fieldBufferFunction.test(buf, offset, len, this);
  }

  @Override
  final <C> boolean test(final C context, final ContextFieldBufferPredicate<C> fieldBufferFunction, final int offset, final int len) {
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(offset, len);
      return fieldBufferFunction.test(context, chars, 0, chars.length, this);
    }
    return fieldBufferFunction.test(context, buf, offset, len, this);
  }

  @Override
  final <R> R apply(final FieldBufferFunction<R> fieldBufferFunction, final int offset, final int len) {
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(offset, len);
      return fieldBufferFunction.apply(chars, 0, chars.length, this);
    }
    return fieldBufferFunction.apply(buf, offset, len, this);
  }

  @Override
  final <C, R> R apply(final C context, final ContextFieldBufferFunction<C, R> fieldBufferFunction, final int offset, final int len) {
    if (numEscapes > 0) {
      final char[] chars = handleEscapes(offset, len);
      return fieldBufferFunction.apply(context, chars, 0, chars.length, this);
    }
    return fieldBufferFunction.apply(context, buf, offset, len, this);
  }

  @Override
  final BigDecimal applyNumberChars(final CharBufferFunction<BigDecimal> parseChars) {
    final int len = parseNumber();
    return parseChars.apply(buf, head - len, len);
  }

  @Override
  final String parsedNumberAsString(final int len) {
    return new String(buf, head - len, len);
  }
}
