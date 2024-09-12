#!/bin/bash
#
# CHANGE THIS FILE TO PUSH YOUR GO CODE TO A GO REPOSITORY
API_SERVER_SOURCE_PATH=../../generated-code/target
SERVER_PATH=$API_SERVER_SOURCE_PATH/server
 
mkdir -p $SERVER_PATH
cp -r target/generated-sources/swagger/src/* $SERVER_PATH/

export old_path="apis/prism/v4"
export new_path="ntnx-api-guru-pc/generated-code/target/interfaces/apis/prism/v4"
export folder_path=$SERVER_PATH/routes
echo "Start"
for file in $(find $folder_path -type f)
do
    echo "$file"
    sed -i  "s#$old_path#$new_path#g" "$file"
    sed -i '' "s#$old_path#$new_path#g" "$file"
done
echo "Done"
