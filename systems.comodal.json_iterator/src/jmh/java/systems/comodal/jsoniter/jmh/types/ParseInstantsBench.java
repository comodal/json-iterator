package systems.comodal.jsoniter.jmh.types;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.CharBuffer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static systems.comodal.jsoniter.InstantParser.INSTANT_PARSER;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ParseInstantsBench {

  static final TemporalQuery<Instant> INSTANT_TEMPORAL_QUERY = Instant::from;
  private static final char[][] instantJsonChars = new char[2_048][];

  static {
    var now = Instant.now();
    final var rand = ThreadLocalRandom.current();
    final long bound = SECONDS.toNanos(10);
    for (int i = 0; i < instantJsonChars.length; i++) {
      now = now.plusNanos(rand.nextLong(bound));
      instantJsonChars[i] = ('"' + now.toString() + '"').toCharArray();
    }
  }

  @Benchmark
  public void jiParser(final Blackhole blackhole) {
    final char[] json = instantJsonChars[ThreadLocalRandom.current().nextInt(instantJsonChars.length)];
    final var instant = INSTANT_PARSER.apply(json, 1, json.length - 2);
    blackhole.consume(instant);
  }

  @Benchmark
  public void dTFParseCharBuffer(final Blackhole blackhole) {
    final char[] json = instantJsonChars[ThreadLocalRandom.current().nextInt(instantJsonChars.length)];
    final var instant = DateTimeFormatter.ISO_INSTANT.parse(CharBuffer.wrap(json, 1, json.length - 2), INSTANT_TEMPORAL_QUERY);
    blackhole.consume(instant);
  }

  @Benchmark
  public void dTFParseString(final Blackhole blackhole) {
    final char[] json = instantJsonChars[ThreadLocalRandom.current().nextInt(instantJsonChars.length)];
    final var instant = DateTimeFormatter.ISO_INSTANT.parse(new String(json, 1, json.length - 2), INSTANT_TEMPORAL_QUERY);
    blackhole.consume(instant);
  }
}
