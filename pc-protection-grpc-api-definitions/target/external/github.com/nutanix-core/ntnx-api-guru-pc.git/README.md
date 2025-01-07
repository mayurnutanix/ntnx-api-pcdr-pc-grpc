# Guru
Guru hosts Domain Manager a.k.a. Prism Central management APIs. It is a gRPC service written in Go with its controller onboarded on Adonis. Currently, it only runs on a PCVM as a Genesis managed service.

### Build and Deployment
1. After checking out the repo, from the base repo directory, run `mvn clean install -s settings.xml` to build the schema models. This requires Java, Maven, ProtocGenGo (v3.12.1) to be installed on the system.
2. To build the Go binary, from the base repo directory, run `GOOS=linux GOARCH=amd64 go build ./guru-pc-api-service/server/main/main.go`. This builds the Go binary for the PCVM architecture environment.
3. To deploy the binary on PC, scp the file to PC at location `~/bin/` and replace the binary `go_guru_server`. This might initially require stopping the already running Guru service. This can be done using `genesis stop guru`. To restart the service after replacing the binary, use `cluster start`.
4. Logs for the service can be found in `~/data/logs/guru.INFO`.

__For more info, reach out on slack channel #ask-prism or #prism_central__
