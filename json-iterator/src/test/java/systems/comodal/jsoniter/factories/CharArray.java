package systems.comodal.jsoniter.factories;

import systems.comodal.jsoniter.JsonIterator;

public final class CharArray implements JsonIteratorFactory {

  public static final JsonIteratorFactory INSTANCE = new CharArray();

  private CharArray() {
  }

  @Override
  public JsonIterator create(final String json) {
    return JsonIterator.parse(json.toCharArray());
  }

  @Override
  public JsonIterator create(final String json, final int charBufferLength) {
    return JsonIterator.parse(json.toCharArray());
  }

  @Override
  public JsonIterator create(final String json, final int bufferLength, final int charBufferLength) {
    return JsonIterator.parse(json, charBufferLength);
  }

  @Override
  public String toString() {
    return "char array";
  }
}
