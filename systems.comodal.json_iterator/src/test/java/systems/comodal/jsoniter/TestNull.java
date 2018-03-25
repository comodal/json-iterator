package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;

final class TestNull {

  @Test
  void test_null_as_String() throws IOException {
    JsonIterator iter = JsonIterator.parse("{\"field\":null}");
    iter.readObject();
    assertNull(iter.readString());
  }

  @Test
  void test_null_as_Object() throws IOException {
    JsonIterator iter = JsonIterator.parse("{\"field\":null}");
    iter.readObject();
    assertNull(iter.readObject());
  }

  @Test
  void test_null_as_BigDecimal() throws IOException {
    JsonIterator iter = JsonIterator.parse("{\"field\":null}");
    iter.readObject();
    assertNull(iter.readBigDecimal());
  }

  @Test
  void test_null_as_BigInteger() throws IOException {
    JsonIterator iter = JsonIterator.parse("{\"field\":null}");
    iter.readObject();
    assertNull(iter.readBigInteger());
  }
}
