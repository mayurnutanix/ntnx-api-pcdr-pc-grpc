#!/bin/bash
sudo apt-get install -y protobuf-compiler
sudo apt-get install -y libprotobuf-dev


# Install golang
wget https://golang.org/dl/go1.21.0.linux-amd64.tar.gz
sudo tar -C /usr/local -xzf go1.21.0.linux-amd64.tar.gz
export PATH=$PATH:/usr/local/go/bin

# Check version
go version
go env

# Install protoc-gen-go and protoc-gen-go-grpc
go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.33.0
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.3.0
go get -u google.golang.org/protobuf@v1.33.0
export PATH=$PATH:$(go env GOPATH)/bin
echo "export PATH=${PATH}" >> $BASH_ENV
protoc-gen-go --version

# Install protoc3 for google/protobuf/*.proto files
curl -OL https://github.com/google/protobuf/releases/download/v3.2.0/protoc-3.2.0-linux-x86_64.zip
unzip protoc-3.2.0-linux-x86_64.zip -d protoc3
sudo mv protoc3/bin/* /usr/local/bin/
sudo mv protoc3/include/* /usr/local/include/
