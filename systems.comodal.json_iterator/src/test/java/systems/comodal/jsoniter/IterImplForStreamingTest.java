package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IterImplForStreamingTest {

  @Test
  void testReadMaxDouble() throws Exception {
    final var maxDouble = "1.7976931348623157e+308";
    final var iter = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream(maxDouble.getBytes()), 32);
    final var numberChars = iter.readNumber();
    assertEquals(maxDouble, new String(numberChars.chars, 0, numberChars.charsLength));
  }
}