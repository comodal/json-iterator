package systems.comodal.jsoniter.factories;

import systems.comodal.jsoniter.JsonIterator;

public final class ByteArray implements JsonIteratorFactory {

  public static final JsonIteratorFactory INSTANCE = new ByteArray();

  private ByteArray() {
  }

  @Override
  public JsonIterator create(final String json) {
    return JsonIterator.parse(json);
  }

  @Override
  public JsonIterator create(final String json, final int charBufferLength) {
    return JsonIterator.parse(json, charBufferLength);
  }

  @Override
  public JsonIterator create(final String json, final int bufferLength, final int charBufferLength) {
    return JsonIterator.parse(json, charBufferLength);
  }

  @Override
  public String toString() {
    return "byte array";
  }
}
