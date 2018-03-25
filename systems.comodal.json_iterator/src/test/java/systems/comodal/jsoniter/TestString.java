package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class TestString {

  @Test
  void test_ascii_string() throws IOException {
    JsonIterator iter = JsonIterator.parse("'hello''world'".replace('\'', '"'));
    assertEquals("hello", iter.readString());
    assertEquals("world", iter.readString());
  }

  @Test
  void test_ascii_string_with_escape() throws IOException {
    JsonIterator iter = JsonIterator.parse("'he\\tllo'".replace('\'', '"'));
    assertEquals("he\tllo", iter.readString());
  }

  @Test
  void test_utf8_string() throws IOException {
    JsonIterator iter = JsonIterator.parse("'中文'".replace('\'', '"'));
    assertEquals("中文", iter.readString());
  }

  @Test
  void test_incomplete_escape() {
    JsonIterator iter = JsonIterator.parse("\"\\");
    assertThrows(JsonException.class, iter::readString);
  }

  @Test
  void test_surrogate() throws IOException {
    JsonIterator iter = JsonIterator.parse("\"\ud83d\udc4a\"");
    assertEquals("\ud83d\udc4a", iter.readString());
  }

  @Test
  void test_larger_than_buffer() throws IOException {
    JsonIterator iter = JsonIterator.parse("'0123456789012345678901234567890123'".replace('\'', '"'));
    assertEquals("0123456789012345678901234567890123", iter.readString());
  }

  @Test
  void test_string_across_buffer() throws IOException {
    JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream("'hello''world'".replace('\'', '"').getBytes()), 2);
    assertEquals("hello", iter.readString());
    assertEquals("world", iter.readString());
  }

  @Test
  void test_utf8() throws IOException {
    byte[] bytes = {'"', (byte) 0xe4, (byte) 0xb8, (byte) 0xad, (byte) 0xe6, (byte) 0x96, (byte) 0x87, '"'};
    JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
    assertEquals("中文", iter.readString());
  }

  @Test
  void test_normal_escape() throws IOException {
    byte[] bytes = {'"', (byte) '\\', (byte) 't', '"'};
    JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
    assertEquals("\t", iter.readString());
  }

  @Test
  void test_unicode_escape() throws IOException {
    byte[] bytes = {'"', (byte) '\\', (byte) 'u', (byte) '4', (byte) 'e', (byte) '2', (byte) 'd', '"'};
    JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
    assertEquals("中", iter.readString());
  }

  @Test
  void test_null_string() throws IOException {
    JsonIterator iter = JsonIterator.parse("null".replace('\'', '"'));
    assertNull(iter.readString());
  }

  @Test
  void test_long_string() throws IOException {
    JsonIterator iter = JsonIterator.parse("\"[\\\"LL\\\",\\\"MM\\\\\\/LW\\\",\\\"JY\\\",\\\"S\\\",\\\"C\\\",\\\"IN\\\",\\\"ME \\\\\\/ LE\\\"]\"");
    assertEquals("[\"LL\",\"MM\\/LW\",\"JY\",\"S\",\"C\",\"IN\",\"ME \\/ LE\"]", iter.readString());
  }

  @Test
  void test_long_string_in_streaming() throws IOException {
    JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream("\"[\\\"LL\\\",\\\"MM\\\\\\/LW\\\",\\\"JY\\\",\\\"S\\\",\\\"C\\\",\\\"IN\\\",\\\"ME \\\\\\/ LE\\\"]\"".getBytes()), 2);
    assertEquals("[\"LL\",\"MM\\/LW\",\"JY\",\"S\",\"C\",\"IN\",\"ME \\/ LE\"]", iter.readString());
  }
}
