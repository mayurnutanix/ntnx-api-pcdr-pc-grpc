package com.nutanix.dp1.pri.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class PriCaseInsensitiveEnumDeserializer<T extends Enum<T>> extends StdDeserializer<T> {

  private final Class<T> tClass;

  protected PriCaseInsensitiveEnumDeserializer(Class<T> tClass) {
    super(tClass);
    this.tClass = tClass;
  }

  @Override
  public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    ObjectCodec codec = jsonParser.getCodec();
    JsonNode node = codec.readTree(jsonParser);
    String value = node.textValue();
    return Enum.valueOf(tClass, value.toUpperCase());
  }
}