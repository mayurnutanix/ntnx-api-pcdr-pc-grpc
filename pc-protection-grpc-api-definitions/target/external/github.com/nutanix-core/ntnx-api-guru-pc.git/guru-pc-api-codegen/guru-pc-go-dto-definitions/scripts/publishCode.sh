#!/bin/bash
#
# CHANGE THIS FILE TO PUSH YOUR GO CODE TO A GO REPOSITORY
API_SERVER_SOURCE_PATH=../../generated-code/target
DTO_PATH=$API_SERVER_SOURCE_PATH/dto
 
mkdir -p $DTO_PATH
cp -r target/generated-sources/swagger/src/* $DTO_PATH/

export old_path="models/"
export new_path="ntnx-api-guru-pc/generated-code/target/dto/models/"

export folder_path=$DTO_PATH/models
echo "Start go dto"
for file in $(find $folder_path -type f)
do
    echo " file name is $file"
    sed -i  "s#$old_path#$new_path#g" "$file"
    sed -i '' "s#$old_path#$new_path#g" "$file"
done
echo "Done"