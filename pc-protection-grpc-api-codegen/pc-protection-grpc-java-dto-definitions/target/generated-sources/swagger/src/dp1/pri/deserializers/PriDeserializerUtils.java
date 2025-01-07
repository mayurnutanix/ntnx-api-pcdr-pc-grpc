package dp1.pri.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PriDeserializerUtils {

  public static final String OBJECT_TYPE = "$objectType";
  public static final String OBJECT_VERSION = "$fv";
  public static final String LEGACY_FQ_OBJECT_TYPE = "$fqObjectType";
  public static final String RESERVED = "$reserved";
  public static final String UNKNOWN_FIELDS = "$unknownFields";
  public static final String API_MINOR_VERSION = "minorVersion";
  private static final String DOT = ".";

  private static final String VERSION_REGEX = String.format("(.*\\.)?(v\\d+\\.r\\d+\\.?(?<%s>[a|b]\\d+)?)(\\.(.*))?", API_MINOR_VERSION);
  private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);

  public static Class<?> getObjectTypedType(JsonNode node, String packagePrefix) throws ClassNotFoundException {
    JsonNodeType nodeType = node.getNodeType();
    if (!JsonNodeType.OBJECT.equals(node.getNodeType())) {
      log.error("Cannot get object-typed type from node of type {}", nodeType);
      throw new IllegalArgumentException(String.format("Cannot get object-typed type from node of type %s", nodeType));
    }
    JsonNode objectTypeNode = node.get(OBJECT_TYPE);
    if (objectTypeNode == null) {
      log.error("No $objectType present in object for deserialization");
      throw new IllegalArgumentException("No $objectType present in object for deserialization");
    }
    String objectTypeFromNode = objectTypeNode.textValue();
    String objectType = StringUtils.isEmpty(packagePrefix) ? objectTypeFromNode
                                                           : packagePrefix.concat(DOT).concat(objectTypeFromNode);
    return Class.forName(objectType);
  }

  private static Class<?> getType(JsonNode node, String packagePrefix) throws ClassNotFoundException {
    log.debug("Inferring java type of json node {}", node);
    JsonNodeType nodeType = node.getNodeType();
    if (JsonNodeType.STRING.equals(nodeType)) {
      return String.class;
    }
    if (JsonNodeType.NUMBER.equals(nodeType)) {
      return Number.class;
    }
    if (JsonNodeType.BOOLEAN.equals(nodeType)) {
      return Boolean.class;
    }
    if (JsonNodeType.ARRAY.equals(nodeType)) {
      return List.class;
    }
    if (!JsonNodeType.OBJECT.equals(nodeType)) {
      log.error("Cannot deserialize node of type {}", nodeType);
      throw new IllegalArgumentException(String.format("Cannot deserialize node of type %s", nodeType));
    }
    if (node.get(OBJECT_TYPE) == null) {
      // OBJECT node without $objectType can only be Map
      return Map.class;
    }
    return getObjectTypedType(node, packagePrefix);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static List readArrayNode(ObjectCodec codec, JsonNode node,
                                   String packagePrefix, Class<?> parameterizedClass) throws IOException, ClassNotFoundException {
    List value = new ArrayList();
    int count = node.size();
    log.debug("Processing {} children of array type node", count);
    Class<?> type = null;
    for (int i = 0; i < count; i++) {
      if (i == 0) {
        type = parameterizedClass != null ? parameterizedClass : getType(node.get(0), packagePrefix);
      }
      value.add(readValue(codec, node.get(i), packagePrefix, type));
    }
    return value;
  }


  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Map readMapNode(ObjectCodec codec, JsonNode node,
                                String packagePrefix) throws IOException, ClassNotFoundException {
    Map value = new LinkedHashMap<>();
    Iterator<Map.Entry<String, JsonNode>> mapIterator = node.fields();
    while (mapIterator.hasNext()) {
      java.util.Map.Entry<String, JsonNode> mapEntry = mapIterator.next();
      log.debug("Processing key {} of map type node", mapEntry.getKey());
      value.put(mapEntry.getKey(), readValue(codec, mapEntry.getValue(), packagePrefix,null));
    }
    return value;
  }

  public static Object readValue(ObjectCodec codec, JsonNode node,
                                 String packagePrefix, Class<?> parameterizedClass) throws IOException, ClassNotFoundException {
    Class<?> type = null;
    if (parameterizedClass != null) {
      type = parameterizedClass;
    }
    else {
      type = getType(node, packagePrefix);
    }
    if ((List.class).equals(type)) {
      //we will handle parameterized types at a sub-level later
      return readArrayNode(codec, node, packagePrefix, null);
    }
    if ((Map.class).equals(type)) {
      return readMapNode(codec, node, packagePrefix);
    }
    return readValue(codec, node, type);
  }

  public static Object readValue(ObjectCodec codec, JsonNode node, Class<?> type) throws IOException {
    JsonParser parser;
    final String VALUE = "value";
    if (Enum.class.isAssignableFrom(type) && node.has(VALUE)) {
      parser = node.get(VALUE).traverse();
    } else {
      parser = node.traverse();
    }

    parser.setCodec(codec);
    return parser.readValueAs(type);
  }

  public static Object handleEtag(Object value, Map<String, Object> reservedMap) {
    if (reservedMap.containsKey("ETag")) {
      Object etagRef = reservedMap.get("ETag");
      if (value instanceof List && ((List)value).size()>0) {
        for (Object item : ((List)value)) {
          if (item instanceof PriObjectTypeTypedObject) {
            ((PriObjectTypeTypedObject) item).get$reserved().put("ETag",etagRef);
          }
        }
      }
      else if (value instanceof PriObjectTypeTypedObject) {
        ((PriObjectTypeTypedObject) value).get$reserved().put("ETag",etagRef);
      }
    }
    return value;
  }

  public static String getMinorVersion(String name) {
    Matcher matcher = VERSION_PATTERN.matcher(name);
    if (!matcher.matches()) {
      return null;
    }
    String minorVersion = matcher.group(API_MINOR_VERSION);
    if (StringUtils.isBlank(minorVersion)) {
      return null;
    }
    return minorVersion;
  }

  public static boolean classNameEquals(String name, String targetClassName) {
    return name.substring(name.lastIndexOf(".")+1).equals(targetClassName);
  }

}
