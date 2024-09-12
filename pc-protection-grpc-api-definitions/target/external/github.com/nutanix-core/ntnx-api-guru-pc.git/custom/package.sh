#!/bin/bash

ARCHIVE_NAME="ntnx-api-guru-pc.tar.gz"
BINARY_NAME="go_guru_server"

# build go binary
go mod tidy
go build -o $BINARY_NAME ./guru-pc-api-service/server/main/main.go
if [ $? -ne 0 ]; then
    exit 1
fi

tar -cvf $ARCHIVE_NAME $BINARY_NAME 

# copy binary to uploads
mkdir -p ./package/uploads
mv -fv $ARCHIVE_NAME ./package/uploads/

python -m canaveral_build_tools.core.store_build_artifacts
rm -rf $BINARY_NAME 
