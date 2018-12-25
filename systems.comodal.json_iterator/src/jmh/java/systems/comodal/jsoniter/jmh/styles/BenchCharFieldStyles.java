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

import static systems.comodal.jsoniter.jmh.styles.BenchStringFieldStyles.JSON_ITERATOR_POOL;
import static systems.comodal.jsoniter.jmh.styles.BenchStringFieldStyles.createJsonIterator;

@State(Scope.Benchmark)
@Warmup(iterations = 4, time = 6)
@Measurement(iterations = 5, time = 7)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BenchCharFieldStyles {

  private static final byte[] BENCH_LARGE_COMPACT_FIELDS_JSON;

  static {
    try {
      BENCH_LARGE_COMPACT_FIELDS_JSON = new String(BenchStringFieldStyles.class.getResourceAsStream("/compactFieldsExchangeInfo.json").readAllBytes())
          .replaceAll("\\s+", "").getBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
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
  public void parse(final Blackhole blackhole) throws IOException {
    final var ji = createJsonIterator(BENCH_LARGE_COMPACT_FIELDS_JSON);
    try {
      blackhole.consume(parser.parse(ji));
    } finally {
      JSON_ITERATOR_POOL.add(ji);
    }
  }

  public JsonIterator getLoadedJsonIterator() {
    return createJsonIterator(BENCH_LARGE_COMPACT_FIELDS_JSON);
  }
}
