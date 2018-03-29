package systems.comodal.jsoniter;

import java.util.Arrays;

final class JHex {

  private static final int INVALID = -1;
  private static final int[] DIGITS = new int['f' + 1];

  static {
    Arrays.fill(DIGITS, INVALID);
    for (int i = '0'; i <= '9'; ++i) {
      DIGITS[i] = (i - '0');
    }
    for (int i = 'a'; i <= 'f'; ++i) {
      DIGITS[i] = ((i - 'a') + 10);
    }
    for (int i = 'A'; i <= 'F'; ++i) {
      DIGITS[i] = ((i - 'A') + 10);
    }
  }

  private JHex() {
  }

  static int decode(final byte b) {
    final int val = DIGITS[b];
    if (val == INVALID) {
      throw new IndexOutOfBoundsException(b + " is not valid hex digit");
    }
    return val;
  }
}
