package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestIO {

  @Test
  void test_read_byte() throws IOException {
    final var ji = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("1".getBytes()), 64);
    assertEquals('1', ji.read());
    assertThrows(JsonException.class, ji::read);
  }

  @Test
  void test_read_bytes() throws IOException {
    final var ji = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("12".getBytes()), 64);
    assertEquals('1', ji.read());
    assertEquals('2', ji.read());
    assertThrows(JsonException.class, ji::read);
  }

  @Test
  void test_unread_byte() throws IOException {
    final var ji = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("12".getBytes()), 64);
    assertEquals('1', ji.read());
    assertEquals('2', ji.read());
    ji.unread();
    assertEquals('2', ji.read());
    ji.unread();
    ji.unread();
    assertEquals('1', ji.read());
  }

}
