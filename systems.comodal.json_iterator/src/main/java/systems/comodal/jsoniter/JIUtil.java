package systems.comodal.jsoniter;

public final class JIUtil {

  private JIUtil() {
  }

  public static int fieldHashCode(final char[] value, int from, final int to) {
    int h = 0;
    while (from < to) {
      h = 31 * h + (value[from++] & 0xff);
    }
    return h;
  }

  public static int fieldCompare(final String field, final char[] buf, final int offset, final int len) {
    int i = len - field.length();
    if (i == 0) {
      for (int j = offset, c; i < len; i++, j++) {
        if ((c = Character.compare(buf[j], field.charAt(i))) != 0) {
          return c;
        }
      }
      return 0;
    } else {
      return i;
    }
  }

  public static long compileReplacePattern(final byte byteToFind) {
    final long pattern = byteToFind & 0xFFL;
    return pattern
        | (pattern << 8)
        | (pattern << 16)
        | (pattern << 24)
        | (pattern << 32)
        | (pattern << 40)
        | (pattern << 48)
        | (pattern << 56);
  }


  public static String escapeQuotesChecked(final String str) {
    final int len = str.length();
    int from = 0;
    do {
      from = str.indexOf('"', from);
      if (from < 0) {
        return str;
      }
      int i = from - 1;
      if (i < 0) {
        return escapeQuotes(str, from);
      }
      if (str.charAt(i) == '\\') {
        int escapes = 1;
        while (--i >= 0) {
          if (str.charAt(i) == '\\') {
            ++escapes;
          } else {
            break;
          }
        }
        if ((escapes & 1) == 0) {
          return escapeQuotes(str, from);
        }
      } else {
        return escapeQuotes(str, from);
      }
    } while (++from < len);
    return str;
  }

  public static String escapeQuotes(final String str) {
    return escapeQuotes(str, -1);
  }

  private static String escapeQuotes(final String str, final int firstUnescapedQuote) {
    final char[] chars = str.toCharArray();
    final char[] escaped = new char[chars.length << 1];

    int from, to;
    if (firstUnescapedQuote < 0) {
      from = 0;
      to = 0;
    } else if (firstUnescapedQuote > 0) {
      System.arraycopy(chars, 0, escaped, 0, firstUnescapedQuote);
      escaped[firstUnescapedQuote] = '\\';
      from = firstUnescapedQuote;
      to = firstUnescapedQuote + 1;
    } else {
      escaped[0] = '\\';
      from = 0;
      to = 1;
    }

    char c;
    for (int escapes = 0, dest = to; ; ++to) {
      if (to == chars.length) {
        if (from == 0) {
          return str;
        } else {
          final int len = to - from;
          System.arraycopy(chars, from, escaped, dest, len);
          dest += len;
          return new String(escaped, 0, dest);
        }
      } else {
        c = chars[to];
        if (c == '\\') {
          escapes++;
        } else if (c == '"' && (escapes & 1) == 0) {
          final int len = to - from;
          System.arraycopy(chars, from, escaped, dest, len);
          dest += len;
          escaped[dest++] = '\\';
          from = to;
          escapes = 0;
        } else {
          escapes = 0;
        }
      }
    }
  }
}
