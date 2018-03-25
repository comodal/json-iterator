package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestBoolean {

  @Test
  void test_streaming() throws IOException {
    JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream("[true,false,null,true]".getBytes()), 3);
    iter.readArray();
    assertTrue(iter.readBoolean());
    iter.readArray();
    assertFalse(iter.readBoolean());
    iter.readArray();
    assertTrue(iter.readNull());
    iter.readArray();
    assertTrue(iter.readBoolean());
  }

  @Test
  void test_non_streaming() throws IOException {
    assertTrue(JsonIterator.parse("true").readBoolean());
    assertFalse(JsonIterator.parse("false").readBoolean());
    assertTrue(JsonIterator.parse("null").readNull());
    assertFalse(JsonIterator.parse("false").readNull());
  }
}
