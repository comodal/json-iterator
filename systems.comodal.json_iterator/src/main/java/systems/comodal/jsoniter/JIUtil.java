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
}
