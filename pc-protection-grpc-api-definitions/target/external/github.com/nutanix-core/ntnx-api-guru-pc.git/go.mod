module github.com/nutanix-core/ntnx-api-guru-pc

go 1.21.0

require (
	github.com/golang/protobuf v1.5.4
	google.golang.org/grpc v1.65.0
	google.golang.org/protobuf v1.34.2
)

require google.golang.org/genproto/googleapis/rpc v0.0.0-20240528184218-531527333157 // indirect

require (
	golang.org/x/net v0.26.0 // indirect
	golang.org/x/sys v0.22.0 // indirect
	golang.org/x/text v0.16.0 // indirect
)

exclude golang.org/x/crypto v0.10.0

exclude golang.org/x/crypto v0.9.0

exclude golang.org/x/crypto v0.8.0

exclude github.com/golang/glog v1.1.1

exclude github.com/golang/glog v1.1.0

exclude github.com/stretchr/testify v1.8.3

exclude github.com/stretchr/testify v1.8.2

replace golang.org/x/crypto => golang.org/x/crypto v0.6.0

replace golang.org/x/crypto2 => golang.org/x/crypto v0.7.0

replace github.com/Shopify/sarama => github.com/Shopify/sarama v1.17.0
