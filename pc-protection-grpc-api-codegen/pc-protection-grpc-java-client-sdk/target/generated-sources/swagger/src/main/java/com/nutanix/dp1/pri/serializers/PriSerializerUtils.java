package com.nutanix.dp1.pri.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nutanix.dp1.pri.deserializers.PriObjectTypeTypedObject;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import static com.nutanix.dp1.pri.deserializers.PriDeserializerUtils.*;

@Slf4j
public class PriSerializerUtils {

  private PriSerializerUtils() {}

  public static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();

  public static void serializeListWithoutHiddenFields(List<PriObjectTypeTypedObject> objectTypeTypedObjectList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (objectTypeTypedObjectList.isEmpty()) {
      jsonGenerator.writeStartArray();
      jsonGenerator.writeEndArray();
      return;
    }
    Class<?> clazz = objectTypeTypedObjectList.get(0).getClass();
    log.debug("Custom serializing complex list of type " + clazz + " to omit platform fields from items except first");
    jsonGenerator.writeStartArray();
    serializerProvider.defaultSerializeValue(objectTypeTypedObjectList.get(0), jsonGenerator);
    log.debug("Serialized first item of complex list of type " + clazz + " normally");
    for (int i = 1; i < objectTypeTypedObjectList.size(); i++) {
      serializeListItemsWithoutHiddenFields(objectTypeTypedObjectList.get(i), clazz, jsonGenerator, serializerProvider);
    }
    jsonGenerator.writeEndArray();
    log.debug("Custom serialization for complex list of type " + clazz + " is complete");
  }

  public static void serializeListItemsWithoutHiddenFields(PriObjectTypeTypedObject object, Class clazz, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (object != null) {
      try {
        String objectTypeValue = object.get$objectType();
        Map<String, Object> reservedValue = object.get$reserved();
        Field objectType = FieldUtils.getField(clazz, OBJECT_TYPE, true);
        objectType.setAccessible(true);
        objectType.set(object, null);
        Field unknownFields = FieldUtils.getField(clazz, UNKNOWN_FIELDS, true);
        unknownFields.setAccessible(true);
        Map<String, Object> unknownFieldsValue = (Map<String, Object>) unknownFields.get(object);
        unknownFields.set(object, null);
        object.get$reserved().clear();
        serializerProvider.defaultSerializeValue(object, jsonGenerator);
        objectType.set(object, objectTypeValue);
        object.get$reserved().putAll(reservedValue);
        unknownFields.set(object, unknownFieldsValue);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Unable to remove platform generated fields ($objectType, $reserved, $unknownFields) failed with error :" + e);
      }
    }
  }

  public static ArrayList<Field> getAllFields(Class<?> clazz) {
    ArrayList<Field> allFields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
    if (clazz.getSuperclass() != PriObjectTypeTypedObject.class && clazz.getSuperclass() != null) {
      allFields.addAll(getAllFields(clazz.getSuperclass()));
    }
    return allFields;
  }
}
