package systems.comodal.jsoniter;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class BenchStyles {

  private static final byte[] BENCH_LARGE_JSON;

  static {
    try {
      BENCH_LARGE_JSON = BenchStyles.class.getResourceAsStream("/exchangeInfo.json").readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final ConcurrentLinkedQueue<JsonIterator> JSON_ITERATOR_POOL = new ConcurrentLinkedQueue<>();

  private static JsonIterator createJsonIterator(final byte[] json) {
    final var jsonIterator = JSON_ITERATOR_POOL.poll();
    return jsonIterator == null ? JsonIterator.parse(json) : jsonIterator.reset(json);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void for_switch(final Blackhole blackhole) throws IOException {
    final var builder = ExchangeInfo.build();
    final var jsonIterator = createJsonIterator(BENCH_LARGE_JSON);
    try {
      for (var field = jsonIterator.readObjField(); field != null; field = jsonIterator.readObjField()) {
        switch (field) {
          case "timezone":
            builder.timezone(jsonIterator.readString());
            continue;
          case "serverTime":
            builder.serverTime(jsonIterator.readLong());
            continue;
          case "rateLimits":
            while (jsonIterator.readArray()) {
              final var rateLimit = RateLimit.build();
              for (field = jsonIterator.readObjField(); field != null; field = jsonIterator.readObjField()) {
                switch (field) {
                  case "rateLimitType":
                    rateLimit.type(jsonIterator.readString());
                    continue;
                  case "interval":
                    rateLimit.interval(jsonIterator.readLong());
                    continue;
                  case "intervalNum":
                    rateLimit.intervalUnit(jsonIterator.readString());
                    continue;
                  case "limit":
                    rateLimit.limit(jsonIterator.readInt());
                    continue;
                  default:
                    throw new IllegalStateException("Unhandled field " + field);
                }
              }
            }
            continue;
          case "exchangeFilters":
            jsonIterator.skip();
            continue;
          case "symbols":
            jsonIterator.skip();
            continue;
          default:
            throw new IllegalStateException("Unhandled field " + field);
        }
      }
    } finally {
      JSON_ITERATOR_POOL.add(jsonIterator);
    }
    blackhole.consume(builder.create());
  }
}
