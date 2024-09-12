module ntnx-api-guru-pc

go 1.21

require (
	github.com/golang/glog v1.2.1
	github.com/golang/mock v1.6.0
	github.com/golang/protobuf v1.5.4
	github.com/gorilla/handlers v1.5.1
	github.com/gorilla/mux v1.8.1
	github.com/natefinch/lumberjack v2.0.0+incompatible
	github.com/nutanix-core/go-cache v0.0.0-20240616123053-5c075257d96c
	github.com/nutanix-core/ntnx-api-guru v1.0.193
	github.com/sirupsen/logrus v1.9.3
	github.com/stretchr/testify v1.9.0
	google.golang.org/grpc v1.64.0
	google.golang.org/protobuf v1.34.1
)

require (
	github.com/nutanix-core/ntnx-api-utils-go v1.0.27
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240318140521-94a12d6c2237
)

require (
	github.com/golang-jwt/jwt v3.2.2+incompatible // indirect
	github.com/google/go-cmp v0.6.0 // indirect
	github.com/google/uuid v1.6.0 // indirect
	github.com/robfig/cron/v3 v3.0.1 // indirect
)

require (
	github.com/davecgh/go-spew v1.1.2-0.20180830191138-d8f796af33cc // indirect
	github.com/dgrijalva/jwt-go v3.2.1-0.20210628220118-008eba19055c+incompatible // indirect
	github.com/felixge/httpsnoop v1.0.1 // indirect
	github.com/go-co-op/gocron v1.37.0
	github.com/go-ole/go-ole v1.2.6 // indirect
	github.com/go-openapi/swag v0.23.0 // indirect
	github.com/golang/snappy v0.0.4 // indirect
	github.com/hkra/go-jwks v0.0.0-20200402141848-a47e42bb51fa // indirect
	github.com/josharian/intern v1.0.1-0.20191216181125-a140101e2404 // indirect
	github.com/mailru/easyjson v0.7.7 // indirect
	github.com/opentracing-contrib/go-grpc v0.0.0-20210225150812-73cb765af46e // indirect
	github.com/opentracing/opentracing-go v1.2.0 // indirect
	github.com/pkg/errors v0.9.1 // indirect
	github.com/pmezard/go-difflib v1.0.1-0.20181226105442-5d4384ee4fb2 // indirect
	github.com/shirou/gopsutil v3.21.11+incompatible // indirect
	github.com/stretchr/objx v0.5.2 // indirect
	github.com/tklauser/go-sysconf v0.3.14 // indirect
	github.com/tklauser/numcpus v0.8.0 // indirect
	github.com/uber/jaeger-client-go v2.30.0+incompatible // indirect
	github.com/uber/jaeger-lib v2.4.1+incompatible // indirect
	github.com/yusufpapurcu/wmi v1.2.4 // indirect
	go.uber.org/atomic v1.11.0 // indirect
	golang.org/x/crypto v0.22.0 // indirect
	golang.org/x/net v0.24.0 // indirect
	golang.org/x/sys v0.20.0 // indirect
	golang.org/x/text v0.15.0 // indirect
	gopkg.in/natefinch/lumberjack.v2 v2.2.1
	gopkg.in/yaml.v2 v2.4.0 // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
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
