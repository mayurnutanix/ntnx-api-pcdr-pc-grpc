package dp1.pri.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "SameParameterValue"})
@Slf4j
@Data
public abstract class PriAbstractBoundedMap<T> {

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  protected Map<String, T> boundedMap;

  @JsonIgnore
  protected final Class<T> valueClass;

  @JsonIgnore
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  protected Map<String, T> unknownKeysMap = new LinkedHashMap<>();

  protected PriAbstractBoundedMap(Class<T> valueClass) {
    this.valueClass = valueClass;
  }

  public T get(String key) {

    if(key == null || boundedMap == null || !boundedMap.containsKey(key)) {
      log.debug("Null or invalid key {} ", key);
      throw new IllegalArgumentException("key:" + key + " not recognized");
    }
    return boundedMap.get(key);
  }

  public void put(String key, T value) {
    if(boundedMap == null) {
      throw new IllegalStateException("BoundedMap is not initialized");
    }
    if(key == null) {
      throw new IllegalArgumentException("Null key is not supported");
    }

    if(!boundedMap.containsKey(key)) {
      log.debug("Putting unrecognized key {} into unknown map", key);
      unknownKeysMap.put(key, value);
      return;
    }
    boundedMap.replace(key, value);
  }

  @JsonIgnore
  public Set<String> getKeys() {
    return boundedMap.keySet();
  }

  protected final void init(List<String> allowedKeys, T defaultValue) {
    if(boundedMap == null) {
      boundedMap = new LinkedHashMap<>();
      allowedKeys.forEach(k -> boundedMap.put(k, defaultValue));
    }
  }
}
