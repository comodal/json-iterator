package systems.comodal.jsoniter;

import java.util.Arrays;

final class JHex {

  private static final int INVALID = -1;
  private static final int[] DIGITS = INIT_DIGITS.initDigits();

  private static final class INIT_DIGITS {

    private INIT_DIGITS() {
    }

    private static int[] initDigits() {
      final int[] digits = new int['f' + 1];
      Arrays.fill(digits, INVALID);
      for (int i = '0'; i <= '9'; ++i) {
        digits[i] = (i - '0');
      }
      for (int i = 'a'; i <= 'f'; ++i) {
        digits[i] = ((i - 'a') + 10);
      }
      for (int i = 'A'; i <= 'F'; ++i) {
        digits[i] = ((i - 'A') + 10);
      }
      return digits;
    }
  }

  private JHex() {
  }

  static int decode(final int b) {
    final int val = DIGITS[b];
    if (val == INVALID) {
      throw new IndexOutOfBoundsException(b + " is not valid hex digit");
    }
    return val;
  }
}
