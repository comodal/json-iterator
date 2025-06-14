package systems.comodal.jsoniter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import systems.comodal.jsoniter.factories.JsonIteratorFactory;

import java.time.Instant;
import java.time.ZoneOffset;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInstant {

  @ParameterizedTest
  @MethodSource("systems.comodal.jsoniter.TestFactories#factories")
  void testParseInstants(final JsonIteratorFactory factory) {
    var dateTime = "2018-03-31T13:43:19.82";
    var ji = factory.create('"' + dateTime + '"');
    assertEquals(ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(dateTime, Instant::from), ji.readDateTime());

    dateTime = "2018-03-15T01:23:44.349000Z";
    ji = factory.create('"' + dateTime + '"');
    assertEquals(Instant.parse(dateTime), ji.readDateTime());

    dateTime = "2018-04-07T18:27:12.646Z";
    ji = factory.create('"' + dateTime + '"');
    assertEquals(Instant.parse(dateTime), ji.readDateTime());

    dateTime = "2018-03-31T19:48:23.0752385Z";
    ji = factory.create('"' + dateTime + '"');
    assertEquals(Instant.parse(dateTime), ji.readDateTime());

    dateTime = "Fri, 04 Oct 2019 16:06:36 GMT";
    ji = factory.create('"' + dateTime + '"');
    assertEquals(RFC_1123_DATE_TIME.parse(dateTime, Instant::from), ji.readDateTime());
  }
}
