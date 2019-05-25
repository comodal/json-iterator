package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class TestBoolean {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_boolean_array(final JsonIteratorFactory factory) throws IOException {
    final var json = "[true,false,null,true]";
    var ji = factory.create(json);
    ji.readArray();
    assertTrue(ji.readBoolean());
    ji.readArray();
    assertFalse(ji.readBoolean());
    ji.readArray();
    assertTrue(ji.readNull());
    ji.readArray();
    assertTrue(ji.readBoolean());
    assertFalse(ji.readArray());

    ji = factory.create(json);
    assertTrue(ji.openArray().readBoolean());
    assertFalse(ji.continueArray().readBoolean());
    assertTrue(ji.continueArray().readNull());
    assertTrue(ji.continueArray().readBoolean());
    assertNotNull(ji.closeArray());
  }

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void test_booleans(final JsonIteratorFactory factory) throws IOException {
    assertTrue(factory.create("true").readBoolean());
    assertFalse(factory.create("false").readBoolean());
    assertTrue(factory.create("null").readNull());
    assertFalse(factory.create("true").readNull());
    assertFalse(factory.create("false").readNull());
  }
}
