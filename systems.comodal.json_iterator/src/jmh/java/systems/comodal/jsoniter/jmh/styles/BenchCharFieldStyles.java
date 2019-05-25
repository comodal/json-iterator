package systems.comodal.jsoniter.jmh.styles;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.factory.JsonIterParserFactory;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 4, time = 6)
@Measurement(iterations = 5, time = 7)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BenchCharFieldStyles {

  private static final byte[] BENCH_LARGE_COMPACT_FIELDS_JSON = INIT_JSON.initJson("/compactFieldsExchangeInfo.json");
  private static final char[] BENCH_LARGE_COMPACT_FIELDS_JSON_CHARS = new String(BENCH_LARGE_COMPACT_FIELDS_JSON).toCharArray();

  static final class INIT_JSON {

    private INIT_JSON() {
    }

    static byte[] initJson(final String filename) {
      try {
        return new String(BenchStringFieldStyles.class.getResourceAsStream(filename).readAllBytes())
            .replaceAll("\\s+", "").getBytes();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  @Param({
      "StaticFieldOrdering",
      "IocLoopCharSwitch",
      "IocLoopCharIf"
  })
  private String style;
  private JsonIterParser<ExchangeInfo> parser;

  @Setup
  public void setup() {
    parser = JsonIterParserFactory.loadParser(ExchangeInfo.class, style);
  }

  @Benchmark
  public void parseBytes(final Blackhole blackhole) throws IOException {
    final var ji = JsonIterator.parse(BENCH_LARGE_COMPACT_FIELDS_JSON);
    blackhole.consume(parser.parse(ji));
  }

  @Benchmark
  public void parseChars(final Blackhole blackhole) throws IOException {
    final var ji = JsonIterator.parse(BENCH_LARGE_COMPACT_FIELDS_JSON_CHARS);
    blackhole.consume(parser.parse(ji));
  }

  public JsonIterator getLoadedBytesJsonIterator() {
    return JsonIterator.parse(BENCH_LARGE_COMPACT_FIELDS_JSON);
  }

  public JsonIterator getLoadedCharsJsonIterator() {
    return JsonIterator.parse(BENCH_LARGE_COMPACT_FIELDS_JSON_CHARS);
  }
}
