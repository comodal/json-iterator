package systems.comodal.jsoniter;

import java.io.IOException;
import java.io.InputStream;

final class BufferedStreamJsonIterator extends BytesJsonIterator {

  private InputStream in;
  private int skipStartedAt; // skip should keep bytes starting at this pos

  BufferedStreamJsonIterator(final InputStream in, final byte[] buf, final int head, final int tail) {
    super(buf, head, tail);
    this.in = in;
    this.skipStartedAt = -1;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }

  @Override
  public boolean supportsMarkReset() {
    return false;
  }

  @Override
  public int mark() {
    throw new UnsupportedOperationException("Mark is not supported when using an InputStream.");
  }

  @Override
  public JsonIterator reset(final int mark) {
    throw new UnsupportedOperationException("Reset via mark is not supported when using an InputStream.");
  }

  @Override
  public JsonIterator reset(final byte[] buf) {
    return new BytesJsonIterator(buf, 0, buf.length);
  }

  @Override
  public JsonIterator reset(final byte[] buf, final int head, final int tail) {
    return new BytesJsonIterator(buf, head, tail);
  }

  @Override
  public JsonIterator reset(final InputStream in) {
    this.in = in;
    this.head = 0;
    this.tail = 0;
    this.skipStartedAt = -1;
    return this;
  }

  @Override
  public JsonIterator reset(final InputStream in, final int bufSize) {
    if (buf.length != bufSize) {
      this.buf = new byte[bufSize];
    }
    this.in = in;
    this.head = 0;
    this.tail = 0;
    this.skipStartedAt = -1;
    return this;
  }

  @Override
  long readLong(final byte c) throws IOException {
    final long ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    }
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readLong", "expect 0~9");
    }
    return readLongSlowPath(ind);
  }

  @Override
  void assertNotLeadingZero() throws IOException {
    try {
      if (head == tail && !loadMore()) {
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

  @Override
  long readLongSlowPath(long value) throws IOException {
    value = -value; // add negatives to avoid redundant checks for Long.MIN_VALUE on each iteration
    do {
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
    } while (loadMore());
    head = tail;
    return value;
  }

  @Override
  int readInt(final byte c) throws IOException {
    final int ind = INT_DIGITS[c];
    if (ind == 0) {
      assertNotLeadingZero();
      return 0;
    }
    if (ind == INVALID_CHAR_FOR_NUMBER) {
      throw reportError("readInt", "expect 0~9");
    }
    return readIntSlowPath(ind);
  }

  @Override
  int readIntSlowPath(int value) throws IOException {
    value = -value; // add negatives to avoid redundant checks for Integer.MIN_VALUE on each iteration
    do {
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
    } while (loadMore());
    head = tail;
    return value;
  }

  @Override
  double readDoubleNoSign() throws IOException {
    return readDoubleSlowPath();
  }

  @Override
  NumberChars readNumber() throws IOException {
    int j = 0;
    boolean dotFound = false;
    do {
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
    } while (loadMore());
    head = tail;
    return new NumberChars(reusableChars, j, dotFound);
  }

  @Override
  void skipArray() throws IOException {
    int level = 1;
    do {
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
    } while (loadMore());
  }

  @Override
  void skipObject() throws IOException {
    int level = 1;
    do {
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
    } while (loadMore());
  }

  @Override
  void skipString() throws IOException {
    for (; ; ) {
      int end = findStringEnd();
      if (end == -1) {
        int j = tail - 1;
        boolean escaped = true;
        // can not just look the last byte is \
        // because it could be \\ or \\\
        for (; ; ) {
          // walk backward until head
          if (j < head || buf[j] != '\\') {
            // even number of backslashes
            // either end of buffer, or " found
            escaped = false;
            break;
          }
          j--;
          if (j < head || buf[j] != '\\') {
            // odd number of backslashes
            // it is \" or \\\"
            break;
          }
          j--;
        }
        if (!loadMore()) {
          throw reportError("skipString", "incomplete string");
        }
        if (escaped) {
          head = 1; // skip the first char as last char is \
        }
      } else {
        head = end;
        return;
      }
    }
  }

  @Override
  void skipUntilBreak() throws IOException {
    // true, false, null, number
    do {
      for (int i = head; i < tail; i++) {
        if (BREAKS[buf[i]]) {
          head = i;
          return;
        }
      }
    } while (loadMore());
    head = tail;
  }

  @Override
  byte nextToken() throws IOException {
    do {
      for (int i = head; i < tail; i++) {
        final byte c = buf[i];
        switch (c) {
          case ' ':
          case '\n':
          case '\t':
          case '\r':
            continue;
          default:
            head = i + 1;
            return c;
        }
      }
    } while (loadMore());
    return 0;
  }

  @Override
  byte readByte() throws IOException {
    if (head == tail) {
      if (!loadMore()) {
        throw reportError("readByte", "no more to read");
      }
    }
    return buf[head++];
  }

  @Override
  void skipFixedBytes(final int n) throws IOException {
    head += n;
    if (head >= tail) {
      final int more = head - tail;
      if (!loadMore()) {
        if (more == 0) {
          head = tail;
          return;
        }
        throw reportError("skipFixedBytes", "unexpected end");
      }
      head += more;
    }
  }

  @Override
  int updateStringCopyBound(final int bound) {
    return bound > tail - head ? tail - head : bound;
  }

  @Override
  int readStringSlowPath(int j) throws IOException {
    boolean isExpectingLowSurrogate = false;
    for (; ; ) {
      int bc = readByte();
      if (bc == '"') {
        return j;
      }
      if (bc == '\\') {
        bc = readByte();
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
            bc = (JHex.decode(readByte()) << 12) +
                (JHex.decode(readByte()) << 8) +
                (JHex.decode(readByte()) << 4) +
                JHex.decode(readByte());
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
        final int u2 = readByte();
        if ((bc & 0xE0) == 0xC0) {
          bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
        } else {
          final int u3 = readByte();
          if ((bc & 0xF0) == 0xE0) {
            bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
          } else {
            final int u4 = readByte();
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
  }

  private boolean loadMore() throws IOException {
    if (in == null) {
      return false;
    }
    if (skipStartedAt != -1) {
      return keepSkippedBytesThenRead();
    }
    final int n = in.read(buf);
    if (n < 1) {
      if (n == -1) {
        return false;
      }
      throw reportError("loadMore", "read from input stream returned " + n);
    }
    head = 0;
    tail = n;
    return true;
  }

  private boolean keepSkippedBytesThenRead() throws IOException {
    int n;
    int offset;
    if (skipStartedAt == 0 || skipStartedAt < tail / 2) {
      final byte[] newBuf = new byte[buf.length * 2];
      offset = tail - skipStartedAt;
      System.arraycopy(buf, skipStartedAt, newBuf, 0, offset);
      buf = newBuf;
      n = in.read(buf, offset, buf.length - offset);
    } else {
      offset = tail - skipStartedAt;
      System.arraycopy(buf, skipStartedAt, buf, 0, offset);
      n = in.read(buf, offset, buf.length - offset);
    }
    skipStartedAt = 0;
    if (n < 1) {
      if (n == -1) {
        return false;
      }
      throw reportError("loadMore", "read from input stream returned " + n);
    }
    head = offset;
    tail = offset + n;
    return true;
  }

  @Override
  public String currentBuffer() {
    int peekStart = head - 10;
    if (peekStart < 0) {
      peekStart = 0;
    }
    final var peek = new String(buf, peekStart, head - peekStart);
    final var bufString = new String(buf, 0, tail);
    final var headPeekBuf = "head: " + head + ", peek: " + peek + ", buf: " + bufString;
    try {
      return headPeekBuf + ", remaining: " + new String(in.readAllBytes());
    } catch (final IOException ioEx) {
      return headPeekBuf;
    }
  }
}
