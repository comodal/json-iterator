package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestIO {

  @Test
  void test_read_byte() throws IOException {
    final var iter = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("1".getBytes()), 4096);
    assertEquals('1', iter.readByte());
    assertThrows(JsonException.class, iter::readByte);
  }

  @Test
  void test_read_bytes() throws IOException {
    final var iter = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("12".getBytes()), 4096);
    assertEquals('1', iter.readByte());
    assertEquals('2', iter.readByte());
    assertThrows(JsonException.class, iter::readByte);
  }

  @Test
  void test_unread_byte() throws IOException {
    final var iter = (BufferedStreamJsonIterator) JsonIterator.parse(new ByteArrayInputStream("12".getBytes()), 4096);
    assertEquals('1', iter.readByte());
    assertEquals('2', iter.readByte());
    iter.unreadByte();
    assertEquals('2', iter.readByte());
    iter.unreadByte();
    iter.unreadByte();
    assertEquals('1', iter.readByte());
  }

}
