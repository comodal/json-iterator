package systems.comodal.jsoniter.jmh.types;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.CharBuffer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static systems.comodal.jsoniter.InstantParser.INSTANT_PARSER;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ParseInstantBench {

  static final TemporalQuery<Instant> INSTANT_TEMPORAL_QUERY = Instant::from;

  private static final String instantString = "2018-06-20T15:21:13.964000Z";
  private static final String timeJson = '"' + instantString + '"';
  private static final char[] timeJsonChars = timeJson.toCharArray();
  private static final int len = timeJsonChars.length - 2;

  @Benchmark
  public void jiParser(final Blackhole blackhole) {
    final var instant = INSTANT_PARSER.apply(timeJsonChars, 1, len);
    blackhole.consume(instant);
  }

  @Benchmark
  public void instantParseCharBuffer(final Blackhole blackhole) {
    final var instant = Instant.parse(CharBuffer.wrap(timeJsonChars, 1, len));
    blackhole.consume(instant);
  }

  @Benchmark
  public void dTFParseCharBuffer(final Blackhole blackhole) {
    final var instant = DateTimeFormatter.ISO_INSTANT.parse(CharBuffer.wrap(timeJsonChars, 1, len), INSTANT_TEMPORAL_QUERY);
    blackhole.consume(instant);
  }

  @Benchmark
  public void instantFromDTFParseCharBuffer(final Blackhole blackhole) {
    final var instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(CharBuffer.wrap(timeJsonChars, 1, len)));
    blackhole.consume(instant);
  }


  @Benchmark
  public void instantParseString(final Blackhole blackhole) {
    final var instant = Instant.parse(new String(timeJsonChars, 1, len));
    blackhole.consume(instant);
  }

  @Benchmark
  public void dTFParseString(final Blackhole blackhole) {
    final var instant = DateTimeFormatter.ISO_INSTANT.parse(new String(timeJsonChars, 1, len), INSTANT_TEMPORAL_QUERY);
    blackhole.consume(instant);
  }

  @Benchmark
  public void instantFromDTFParseString(final Blackhole blackhole) {
    final var instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(new String(timeJsonChars, 1, len)));
    blackhole.consume(instant);
  }

  @Test
  void testInstantParseCharBuffer() {
    final var instant = Instant.parse(CharBuffer.wrap(timeJsonChars, 1, len));
    assertEquals(Instant.parse(instantString), instant);
  }

  @Test
  void testJIParser() {
    final var instant = INSTANT_PARSER.apply(timeJsonChars, 1, len);
    assertEquals(Instant.parse(instantString), instant);
  }
}
