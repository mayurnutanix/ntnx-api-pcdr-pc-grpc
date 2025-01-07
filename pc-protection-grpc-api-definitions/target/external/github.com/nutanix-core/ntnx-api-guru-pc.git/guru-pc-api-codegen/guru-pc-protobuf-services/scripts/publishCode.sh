#!/bin/bash
#
# CHANGE THIS FILE TO PUSH YOUR GO CODE TO A GO REPOSITORY
API_SERVER_SOURCE_PATH=../../generated-code
PROTO_SERVICES_PATH=$API_SERVER_SOURCE_PATH/protobuf
 
mkdir -p $PROTO_SERVICES_PATH
cp -r target/generated-sources/swagger/* $PROTO_SERVICES_PATH/


echo "Start"
cd $PROTO_SERVICES_PATH
# Generating go code from proto files
find . -name "*.proto" -exec sh -c 'protoc --go_out=. --go-grpc_out=. "$0"' {} \;
if [ $? -ne 0 ]; then
    exit 1
fi

if find "." -type f -name "*service*pb.go" | grep -q .; then
    echo "Go files generated from protobuf services"
else
    echo "Go files not generated from protobuf services"
    exit 1
fi

# Correcting import statements of go files generated from proto files
export old_path_common="common/"
export new_path_common="github.com/nutanix-core/ntnx-api-guru-pc/generated-code/protobuf/common/"
export old_path_prism="prism/"
export new_path_prism="github.com/nutanix-core/ntnx-api-guru-pc/generated-code/protobuf/prism/"
export old_path_clustermgmt="clustermgmt/"
export new_path_clustermgmt="github.com/nutanix-core/ntnx-api-guru-pc/generated-code/protobuf/clustermgmt/"
export old_path_vmm="vmm/"
export new_path_vmm="github.com/nutanix-core/ntnx-api-guru-pc/generated-code/protobuf/vmm/"
export folder_path=$(pwd)

echo $folder_path
for file in $(find $folder_path -type f)
do
    if [[ -f "$file" && "$file" =~ \.go$ ]]; then
        echo " file name is $file"
        sed -i  "s#$old_path_common#$new_path_common#g" "$file"
        sed -i '' "s#$old_path_common#$new_path_common#g" "$file"
        sed -i  "s#$old_path_prism#$new_path_prism#g" "$file"
        sed -i '' "s#$old_path_prism#$new_path_prism#g" "$file"
        sed -i  "s#$old_path_clustermgmt#$new_path_clustermgmt#g" "$file"
        sed -i '' "s#$old_path_clustermgmt#$new_path_clustermgmt#g" "$file"
        sed -i  "s#$old_path_vmm#$new_path_vmm#g" "$file"
        sed -i '' "s#$old_path_vmm#$new_path_vmm#g" "$file"
    fi
done
echo "Done"
