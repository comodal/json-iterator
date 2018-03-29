package systems.comodal.jsoniter;

import java.util.Arrays;

public enum ValueType {

  INVALID,
  STRING,
  NUMBER,
  NULL,
  BOOLEAN,
  ARRAY,
  OBJECT;

  static final ValueType[] VALUE_TYPES = new ValueType[256];

  static {
    Arrays.fill(VALUE_TYPES, INVALID);
    VALUE_TYPES['"'] = STRING;
    VALUE_TYPES['-'] = NUMBER;
    VALUE_TYPES['0'] = NUMBER;
    VALUE_TYPES['1'] = NUMBER;
    VALUE_TYPES['2'] = NUMBER;
    VALUE_TYPES['3'] = NUMBER;
    VALUE_TYPES['4'] = NUMBER;
    VALUE_TYPES['5'] = NUMBER;
    VALUE_TYPES['6'] = NUMBER;
    VALUE_TYPES['7'] = NUMBER;
    VALUE_TYPES['8'] = NUMBER;
    VALUE_TYPES['9'] = NUMBER;
    VALUE_TYPES['t'] = BOOLEAN;
    VALUE_TYPES['f'] = BOOLEAN;
    VALUE_TYPES['n'] = NULL;
    VALUE_TYPES['['] = ARRAY;
    VALUE_TYPES['{'] = OBJECT;
  }
}
