#!/bin/bash
#
# CHANGE THIS FILE TO PUSH YOUR GO CODE TO A GO REPOSITORY
API_SERVER_SOURCE_PATH=../../generated-code
EDM_PATH=$API_SERVER_SOURCE_PATH/edm
 
mkdir -p $EDM_PATH
cp -r target/generated-sources/swagger/models/edm/* $EDM_PATH/
echo "Done"