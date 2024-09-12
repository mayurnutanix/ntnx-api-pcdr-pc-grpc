#!/bin/bash
#
# CHANGE THIS FILE TO PUSH YOUR GO CODE TO A GO REPOSITORY
API_SERVER_SOURCE_PATH=../../generated-code/target
INTERFACES_PATH=$API_SERVER_SOURCE_PATH/interfaces
 
mkdir -p $INTERFACES_PATH
cp -r target/generated-sources/swagger/src/* $INTERFACES_PATH/