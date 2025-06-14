package systems.comodal.jsoniter.factories;

import systems.comodal.jsoniter.JsonIterator;

public interface JsonIteratorFactory {

  JsonIterator create(final String json);

  JsonIterator create(final String json, final int charBufferLength);

  JsonIterator create(final String json, final int bufferLength, final int charBufferLength);
}
