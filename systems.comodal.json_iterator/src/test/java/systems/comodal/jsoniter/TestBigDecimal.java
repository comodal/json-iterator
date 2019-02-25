package systems.comodal.jsoniter;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestBigDecimal {

  @Test
  void testReadBigDecimalStripTrailingZeroes() throws IOException {
    final var json = "{\"U\":\"2019-02-25T02:57:39.118962Z\",\"f\":\"1\"}";
    final var ji = JsonIterator.parse(json.getBytes());

    assertEquals("2019-02-25T02:57:39.118962Z", ji.skipObjField().readString());
    assertEquals(BigDecimal.ONE, ji.skipObjField().readBigDecimalStripTrailingZeroes());
  }
}
