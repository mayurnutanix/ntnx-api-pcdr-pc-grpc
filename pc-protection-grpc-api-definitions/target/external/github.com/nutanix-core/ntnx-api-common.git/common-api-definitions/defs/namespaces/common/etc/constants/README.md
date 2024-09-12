This folder contains one or more YAML files that hold references to constants that can be 
used with the ConstantRef macro.  This promotes reusability of commonly used constant constructs
without needing to repeat them.  Constants defined here are available to be referenced as remote references as well

The constants are defined as follows:

```
- name: "const1"
  type: "integer"
  format: "int64"
  value: "123456789000"
  description: "A large 64 bit integer value"
- name: "const2"
  type: "integer"
  value: "12345"
  description: "A 32 bit integer value (no format)"
- name: "const3"
  type: "string"
  value: "stringWithExplicitType"
  description: "A string value with type specified explicitly"
- name: "const4"
  value: "stringWithMissingType"
  description: "A string value - no type"
- name: "const5"
  type: "number"
  value: "12345"
  description: "This will be a BigDecimal value (Default)"
- name: "const5"
  type: "number"
  format: "float"
  value: "12345"
  description: "This will be used for Float values"
- name: "const6"
  type: "number"
  format: "double"
  value: "12345"
  description: "This will be used for Float values"

```