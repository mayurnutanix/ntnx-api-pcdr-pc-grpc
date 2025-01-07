/*
 * Generated file ..
 *
 * Product version: 17.0.0-SNAPSHOT
 *
 * Part of the PC Protection PC Client SDK
 *
 * (c) 2025 Nutanix Inc.  All rights reserved
 *
 */

package dp1.pri.common.v1.config;


import javax.validation.constraints.*;


import static dp1.pri.deserializers.PriDeserializerUtils.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The message severity.
 * 
 */

public enum MessageSeverity {

  /**
    * Unknown value.

    */
  $UNKNOWN,
    /**
    * Redacted value.

    */
  $REDACTED,
    /**
    * Information about successful completion.

    */
  INFO,
    /**
    * Warning indicating future error.

    */
  WARNING,
    /**
    * Error indicating failed completion.

    */
  ERROR  ;

  private static final Map<String, MessageSeverity> lookup = new LinkedHashMap<String, MessageSeverity>();

  static {
    lookup.put("$UNKNOWN", $UNKNOWN);
    lookup.put("$REDACTED", $REDACTED);
    lookup.put("INFO", INFO);
    lookup.put("WARNING", WARNING);
    lookup.put("ERROR", ERROR);
  }

  @JsonCreator
  public static MessageSeverity fromString(String enumTypeVar) {
    return lookup.getOrDefault(enumTypeVar, MessageSeverity.$UNKNOWN);
  }

  @JsonValue
  public String fromEnum() {
    for (Map.Entry<String, MessageSeverity> entry : lookup.entrySet()) {
      if (entry.getValue() == this) {
        return entry.getKey();
      }
    }
    return "$UNKNOWN";
  }


}
