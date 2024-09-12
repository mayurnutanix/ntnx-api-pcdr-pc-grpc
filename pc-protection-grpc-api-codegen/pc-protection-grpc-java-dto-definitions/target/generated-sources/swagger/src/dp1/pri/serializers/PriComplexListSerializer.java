package dp1.pri.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dp1.pri.deserializers.PriObjectTypeTypedObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static dp1.pri.serializers.PriSerializerUtils.*;

@Slf4j
public class PriComplexListSerializer extends StdSerializer<List<PriObjectTypeTypedObject>> {

  protected PriComplexListSerializer() {
    super(TYPE_FACTORY.constructCollectionType(java.util.List.class, PriObjectTypeTypedObject.class));
  }

  @SneakyThrows
  @Override
  public void serialize(List<PriObjectTypeTypedObject> objectTypeTypedObjectList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
    log.debug("Serializing via PriComplexListSerializer");
    serializeListWithoutHiddenFields(objectTypeTypedObjectList, jsonGenerator, serializerProvider);
  }

  @Override
  public boolean isEmpty(SerializerProvider provider, List<PriObjectTypeTypedObject> value) {
    return super.isEmpty(provider, value) || value.isEmpty();
  }

}




