package systems.comodal.jsoniter.jmh.styles;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.factory.JsonIterParser;
import systems.comodal.jsoniter.factory.JsonIterParserFactory;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 4)
@Measurement(iterations = 2, time = 4)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BenchExchangeInfoIfMask {

  private static final byte[] BENCH_LARGE_JSON = BenchCharFieldStyles.INIT_JSON.initJson("/exchangeInfo.json");
  private static final char[] BENCH_LARGE_JSON_CHARS = new String(BENCH_LARGE_JSON).toCharArray();

  @Param({
      "IocLoopCompareStringFieldToCharsIf",
      "IocLoopCompareStringFieldToCharsIfMask"
  })
  private String style;
  private JsonIterParser<ExchangeInfo> parser;

  @Setup
  public void setup() {
    parser = JsonIterParserFactory.loadParser(ExchangeInfo.class, style);
  }

  @Benchmark
  public void parseBytes(final Blackhole blackhole) throws IOException {
    final var ji = JsonIterator.parse(BENCH_LARGE_JSON);
    blackhole.consume(parser.parse(ji));
  }

  @Benchmark
  public void parseChars(final Blackhole blackhole) throws IOException {
    final var ji = JsonIterator.parse(BENCH_LARGE_JSON_CHARS);
    blackhole.consume(parser.parse(ji));
  }

  public JsonIterator getLoadedBytesJsonIterator() {
    return JsonIterator.parse(BENCH_LARGE_JSON);
  }

  public JsonIterator getLoadedBytesInputStreamJsonIterator(final int bufferSize) {
    return JsonIterator.parse(new ByteArrayInputStream(BENCH_LARGE_JSON), bufferSize);
  }

  public JsonIterator getLoadedCharsJsonIterator() {
    return JsonIterator.parse(BENCH_LARGE_JSON_CHARS);
  }
}
