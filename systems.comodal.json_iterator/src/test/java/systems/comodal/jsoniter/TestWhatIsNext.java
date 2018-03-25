package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestWhatIsNext {

  @Test
  void test() throws IOException {
    JsonIterator parser = JsonIterator.parse("{}");
    assertEquals(ValueType.OBJECT, parser.whatIsNext());
  }
}
