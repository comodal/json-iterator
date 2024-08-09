package systems.comodal.jsoniter.factory;

import systems.comodal.jsoniter.CharBufferFunction;
import systems.comodal.jsoniter.FieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;
import systems.comodal.jsoniter.ValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface ElementFactory<T> extends FieldBufferPredicate {

  static <T> List<T> parseList(final JsonIterator ji,
                               final Supplier<? extends ElementFactory<T>> objectParserSupplier) {
    if (ji.readArray()) {
      final var list = new ArrayList<T>();
      do {
        final var parser = objectParserSupplier.get();
        ji.testObject(parser);
        list.add(parser.create());
      } while (ji.readArray());
      return list;
    } else {
      return List.of();
    }
  }

  static <T> List<T> parseList(final JsonIterator ji,
                               final Supplier<? extends ElementFactory<T>> objectParserSupplier,
                               final CharBufferFunction<T> stringParser) {
    if (ji.readArray()) {
      final var list = new ArrayList<T>();
      do {
        if (ji.whatIsNext() == ValueType.OBJECT) {
          final var parser = objectParserSupplier.get();
          ji.testObject(parser);
          list.add(parser.create());
        } else {
          list.add(ji.applyChars(stringParser));
        }
      } while (ji.readArray());
      return list;
    } else {
      return List.of();
    }
  }

  T create();
}
