package dp1.pri.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static dp1.pri.deserializers.PriDeserializerUtils.*;

@Slf4j
public abstract class PriOneOfDeserializer<W> extends StdDeserializer<W> {

  public PriOneOfDeserializer(JavaType wrapperClass) {
    super(wrapperClass);
  }

  @Override
  public W deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    ObjectCodec codec = jp.getCodec();
    JsonNode node = codec.readTree(jp);
    try {
      W oneOfObject = getObjectInstance();
      Object value = getObject(codec, node);
      setDataObject(oneOfObject, value);
      return oneOfObject;
    }
    catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      log.error("Class not found in classpath or could not be instantiated", e);
      throw new IOException("Class not found in classpath or could not be instantiated", e);
    }
  }

  protected abstract String getPackagePrefix();

  @SuppressWarnings("unchecked")
  protected W getObjectInstance() throws InstantiationException, IllegalAccessException {
    return (W) handledType().newInstance();
  }

  protected Object getObject(ObjectCodec codec, JsonNode node) throws IOException, ClassNotFoundException {
    JsonNodeType nodeType = node.getNodeType();
    if (nodeType == JsonNodeType.ARRAY || nodeType == JsonNodeType.OBJECT || nodeType == JsonNodeType.NUMBER ||
        nodeType == JsonNodeType.STRING || nodeType == JsonNodeType.BOOLEAN) {
      return readValue(codec, node, getPackagePrefix(),null);
    }
    else {
      log.error("Unrecognized json node type: {}", nodeType);
      throw new IllegalArgumentException("Could not recognize json node type");
    }
  }

  protected abstract void setDataObject(W oneOfObject, Object nestedObject);
}
