package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

final class TestString {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_ascii_string(final JsonIteratorFactory factory) {
    var ji = factory.create("\"hello\"\"world\"");
    assertEquals("hello", ji.readString());
    assertEquals("world", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_escapes_string(final JsonIteratorFactory factory) {
    var ji = factory.create("\"even" + "\\".repeat(42) + '"');
    assertEquals("even" + "\\".repeat(21), ji.readString());

    ji = factory.create("\"odd" + "\\".repeat(11) + "\"\"");
    assertEquals("odd" + "\\".repeat(5) + '"', ji.readString());

    ji = factory.create("\"even\\\\\"");
    assertEquals("even\\", ji.readString());

    ji = factory.create("\"odd\\\\\\\"\"");
    assertEquals("odd\\\"", ji.readString());

    ji = factory.create("\"odd\\\"\"");
    assertEquals("odd\"", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_ascii_string_with_escape(final JsonIteratorFactory factory) {
    var json = "\"he\tllo\"";
    var ji = factory.create(json);
    assertEquals("he\tllo", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_utf8_string(final JsonIteratorFactory factory) {
    var ji = factory.create("\"中文\"");
    assertEquals("中文", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_incomplete_escape(final JsonIteratorFactory factory) {
    var ji = factory.create("\"\\");
    assertThrows(JsonException.class, ji::readString);
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_surrogate(final JsonIteratorFactory factory) {
    var ji = factory.create("\"\uD83D\uDC4A\"");
    assertEquals("\ud83d\udc4a", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_larger_than_buffer(final JsonIteratorFactory factory) {
    var ji = factory.create("\"0123456789012345678901234567890123\"");
    assertEquals("0123456789012345678901234567890123", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_string_across_buffer(final JsonIteratorFactory factory) {
    var ji = factory.create("\"hello\"\"world\"", 2, 2);
    assertEquals("hello", ji.readString());
    assertEquals("world", ji.readString());
  }

  @Test
  void test_utf8() {
    byte[] bytes = {'"', (byte) 0xe4, (byte) 0xb8, (byte) 0xad, (byte) 0xe6, (byte) 0x96, (byte) 0x87, '"'};
    var ji = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
    assertEquals("中文", ji.readString());

    ji = JsonIterator.parse(bytes);
    assertEquals("中文", ji.readString());
  }

  @Test
  void test_normal_escape() {
    byte[] bytes = {'"', (byte) '\\', (byte) 't', '"'};
    var ji = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
    assertEquals("\t", ji.readString());

    ji = JsonIterator.parse(bytes);
    assertEquals("\t", ji.readString());
  }

  @Test
  void test_unicode_escape() {
    byte[] bytes = {'"', (byte) '\\', (byte) 'u', (byte) '4', (byte) 'e', (byte) '2', (byte) 'd', '"'};
    var ji = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
    assertEquals("中", ji.readString());

    ji = JsonIterator.parse(bytes);
    assertEquals("中", ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_null_string(final JsonIteratorFactory factory) {
    var ji = factory.create("null");
    assertNull(ji.readString());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_long_string(final JsonIteratorFactory factory) {
    var ji = factory.create("\"[\\\"LL\\\",\\\"MM\\\\\\/LW\\\",\\\"JY\\\",\\\"S\\\",\\\"C\\\",\\\"IN\\\",\\\"ME \\\\\\/ LE\\\"]\"");
    assertEquals("[\"LL\",\"MM\\/LW\",\"JY\",\"S\",\"C\",\"IN\",\"ME \\/ LE\"]", ji.readString());
  }
}
