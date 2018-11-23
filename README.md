# JSON Iterator [![Build Status](https://travis-ci.org/comodal/json-iterator.svg?branch=master)](https://travis-ci.org/comodal/json-iterator) [ ![Download](https://api.bintray.com/packages/comodal/libraries/json-iterator/images/download.svg) ](https://bintray.com/comodal/libraries/json-iterator/_latestVersion)

JSON Iterator is a minimal Java 12 adaption of the original [jsoniter JsonIterator](https://github.com/json-iterator/java), maintaining only the [stream parsing features](http://jsoniter.com/java-features.html#iterator-to-rescue).  See [JsonIterator.java](systems.comodal.json_iterator/src/main/java/systems/comodal/jsoniter/JsonIterator.java) for the public interface.

0.4.3 was the last release targeting Java 11.

### Usage
```java
var jsonIterator = JsonIterator.parse(`{"hello": "world"}`);
var fieldName = jsonIterator.readObject();
var fieldValue = jsonIterator.readString();
System.out.println(fieldName + ' ' + fieldValue);
```
