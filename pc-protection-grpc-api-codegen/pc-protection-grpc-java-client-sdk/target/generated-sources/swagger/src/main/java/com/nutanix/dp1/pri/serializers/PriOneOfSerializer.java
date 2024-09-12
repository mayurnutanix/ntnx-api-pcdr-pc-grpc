package com.nutanix.dp1.pri.serializers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.nutanix.devplatform.models.PrettyModeViews;
import com.nutanix.dp1.pri.deserializers.PriObjectTypeTypedObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.nutanix.dp1.pri.deserializers.PriDeserializerUtils.OBJECT_TYPE;
import static com.nutanix.dp1.pri.serializers.PriSerializerUtils.*;

@Slf4j
public class PriOneOfSerializer extends StdSerializer<Object> {
  protected static final List<String> IGNORE_IN_ONEOF_PRETTY_MODE = Collections
      .unmodifiableList(Arrays.asList("$unknownFields", "$reserved"));

  protected PriOneOfSerializer() {
    super(Object.class);
  }
  @SneakyThrows
  @Override
  public void serialize(Object object, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if(PrettyModeViews.PrettyView.class.equals(serializerProvider.getActiveView())) {
      log.debug("Custom serializing oneOf object of type " + object.getClass() + " in Pretty Mode");
      if (object instanceof List) {
        handleListType((List) object, jsonGenerator, serializerProvider);
      } else {
        serializeOneOfInPrettyMode(object, object.getClass(), jsonGenerator, serializerProvider);
      }
    } else {
      serializerProvider.defaultSerializeValue(object, jsonGenerator);
    }
  }

  protected void handleListType(List list, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, IllegalAccessException {
    if (list.isEmpty()) {
      jsonGenerator.writeStartArray();
      jsonGenerator.writeEndArray();
      return;
    }
    Class<?> clazz = list.get(0).getClass();
    jsonGenerator.writeStartArray();
    for (Object item : list) {
      serializeOneOfInPrettyMode(item, clazz, jsonGenerator, serializerProvider);
    }
    jsonGenerator.writeEndArray();
  }

  protected void serializeOneOfInPrettyMode(Object object, Class clazz, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IllegalAccessException, IOException {
    if (PriObjectTypeTypedObject.class.isAssignableFrom(clazz) && object != null) {
      jsonGenerator.writeStartObject();
      ArrayList<Field> fields = getAllFields(clazz);
      for (Field field : fields) {
        field.setAccessible(true);
        try {
          if (!(IGNORE_IN_ONEOF_PRETTY_MODE.contains(field.getName()) ||
                isEmpty(serializerProvider, field.get(object)) ||
                Modifier.isStatic(field.getModifiers()) ||
                Modifier.isTransient(field.getModifiers()) ||
              (field.getAnnotation(JsonProperty.class) != null &&
              field.getAnnotation(JsonProperty.class).access().equals(JsonProperty.Access.WRITE_ONLY)))) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if(annotation != null) {
              jsonGenerator.writeFieldName(annotation.value());
            }
            else {
              jsonGenerator.writeFieldName(field.getName());
            }
            BeanWrapperImpl wrapper = new BeanWrapperImpl(object);
            Object property = wrapper.getPropertyValue(field.getName());
            serializerProvider.defaultSerializeValue(property, jsonGenerator);
          }
        }
        catch (IllegalAccessException e) {
          log.error("Inaccessible field {} in oneof data", field);
          throw new IllegalAccessException("Can not access field " + field.getName() + ", failed with error :" + e);
        }
      }
      jsonGenerator.writeEndObject();
    } else {
        serializerProvider.defaultSerializeValue(object, jsonGenerator);
    }
  }

  @Override
  public boolean isEmpty(SerializerProvider serializerProvider, Object o) {
    return (super.isEmpty(serializerProvider, o)) ||
           (o instanceof Collection && CollectionUtils.isEmpty((Collection<?>) o)) ||
           (o instanceof String && StringUtils.isEmpty((String) o));
  }

}
