package dp1.pri.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static dp1.pri.deserializers.PriDeserializerUtils.*;

@Slf4j
public class PriObjectTypeTypedObjectDeserializer extends StdDeserializer<PriObjectTypeTypedObject> {

  private final String packagePrefix = "dp1.pri";

  public PriObjectTypeTypedObjectDeserializer() {
    super(PriObjectTypeTypedObject.class);
  }

  @SneakyThrows
  @Override
  public PriObjectTypeTypedObject deserialize(JsonParser p, DeserializationContext ctxt) {
    ObjectCodec codec = p.getCodec();
    JsonNode targetNode = codec.readTree(p);
    Class<?> clazz = getObjectTypedType(targetNode, this.packagePrefix);
    log.debug("Deserializing json node into instance of {}", clazz);
    return (PriObjectTypeTypedObject) readValue(codec, targetNode, clazz);
  }

}
