#!/bin/sh
#
# Script that helps start the Backup Service

# Set JAVA_HOME
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/jre-1.8.0}"

# Set Log Directory
export BACKUP_SERVICE_LOG_DIR="${BACKUP_SERVICE_LOG_DIR:-/home/nutanix/data/logs}"

# Set Core Dump Path
export CORE_DUMP_PATH="${CORE_DUMP_PATH:-/home/nutanix/data/cores}"

# Set Adonis Service Location
export BACKUP_SERVICE_SERVICE_HOME="${BACKUP_SERVICE_SERVICE_HOME:-/home/nutanix/backup-service}"

# Setting initial and minimum heap size -Xms
HEAP_MIN_SIZE="${HEAP_MIN_SIZE:-32m}"
# Setting maximum heap size -Xmx
HEAP_MAX_SIZE="${HEAP_MAX_SIZE:-128m}"
# Setting Thread stack size -Xss
STACK_SIZE="${STACK_SIZE:-256k}"
# Setting Max MetaSpace Size
MAX_METASPACE_SIZE="${MAX_METASPACE_SIZE:-128m}"

JVM_OPTIONS="-Xms${HEAP_MIN_SIZE} \
-Xmx${HEAP_MAX_SIZE} \
-Xss${STACK_SIZE} \
-XX:MaxMetaspaceSize=${MAX_METASPACE_SIZE} \
-XX:+UseSerialGC \
-XX:MaxDirectMemorySize=5m \
-Xshare:off \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-Xloggc:${BACKUP_SERVICE_LOG_DIR}/BACKUP_SERVICE_gc.log \
-XX:+UseGCLogFileRotation \
-XX:ReservedCodeCacheSize=16m \
-XX:NumberOfGCLogFiles=5 \
-XX:GCLogFileSize=10M \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:+CrashOnOutOfMemoryError \
-XX:ErrorFile=${CORE_DUMP_PATH}/BACKUP_SERVICE_hs_err_%p.log \
-XX:HeapDumpPath=${CORE_DUMP_PATH}/BACKUP_SERVICE_heap_dump_$$_$(date +%s).hprof"

# Set the external config location from where config will be read

export SPRING_CONFIG_LOCATION="${SPRING_CONFIG_LOCATION:-file://${BACKUP_SERVICE_SERVICE_HOME}/config/application.yaml}"

BACKUP_SERVICE_SERVICE_JAR="${BACKUP_SERVICE_SERVICE_JAR:-${BACKUP_SERVICE_SERVICE_HOME}/lib/pcbr-service.jar}"

"${JAVA_HOME}"/bin/java ${JVM_OPTIONS} -jar ${BACKUP_SERVICE_SERVICE_JAR}