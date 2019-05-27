package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestIO {

  @Test
  void test_read_byte() {
    final var ji = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("1".getBytes()), 64);
    assertEquals('1', ji.read());
    assertThrows(JsonException.class, ji::read);
  }

  @Test
  void test_read_bytes() {
    final var ji = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("12".getBytes()), 64);
    assertEquals('1', ji.read());
    assertEquals('2', ji.read());
    assertThrows(JsonException.class, ji::read);
  }
}
