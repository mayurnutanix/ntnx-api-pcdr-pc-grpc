# ntnx-api-ahv

This repository contains Nutanix Virtual Machine Management API definitions and codegens.

This project was initialized using the Versioned API framework archetype. Here's the command used:
```batch
mvn archetype:generate -DarchetypeGroupId=com.nutanix.nutanix-core.ntnx-api.dev-platform -DarchetypeArtifactId=openapi-api-definition-archetype -DarchetypeVersion=1.2.3-RELEASE -DgroupId=com.nutanix.nutanix-core.ntnx-api.ahv -DartifactId=ahv -DprojectDescription="Nutanix AHV versioned APIs" -DprojectName="vms" -Dversion=9.0-SNAPSHOT -DinteractiveMode=false
```
Note: This is here for documentation purposes only. There is no need to run this command ever again here. Since, then we have changed the namespace from ahv to vmm.


Our chosen namespace is vmm. This stands for Virtual Machine Management.
We expect this to be the landing place for anything related to virtual machines in our stack.
This top level grouping helps organize our APIs and helps customers find them quicker.

### Project organization

The top level folders in this project are vmm-api-definitions and vmm-api-codegen.

#### vmm-api-definitions

As the name suggests this is where all the API definitions live.
We use OpenAPI 3.0 to describe our APIs.

Information on structure underneath:

`defs/metadata/repositories.yaml` - Our dependencies on other team's schemas defined as git references.

`defs/namespaces/vmm` - root of our namespace

Under `defs/namespaces/vmm`:

`etc` contains constants, documentation and error message resources that are
un-versioned and are meant to be used for defining strings.

`versioned/v2` defines our first major API development effort using this new framework.

`versioned/v2/modules` Under a major version you can have multiple modules.

Here is an example of the modules that we are considering:
- ahv/config
- ahv/stats
- esx/config
- esx/stats

Each of the modules will further contain resources, e.g.:

- vms
- vm_recovery_points
- images
- templates

`versioned/v2/modules/ahv/beta` - This folder indicates the current state of the release of the module.
This can be alpha, beta, released.

Each module underneath has models and apis.
Models can be thought of as class definitions or messages if you will from proto.
apis can be thought of as service definitions from proto world.

Putting all this together our endpoints then look like:

- POST /vmm/v2.1.a1/ahv/config/vms
- POST /vmm/v2.1.a1/ahv/config/vm_recovery_points

### vmm-api-codegen

The framework provides both client side generated stubs and dtos in Go, Python and Javascript.
- vmm-javascript-client-sdk (To be used by GUI)
- vmm-python-client-sdk (To be used by Test automation or inter-service communication over HTTP)
- vmm-go-do-definitions (To be used for inter-service communication over HTTP)

The framework also provides automatically generated protos for all the API definitions.
There are organized under:
- vmm-protobuf-messages
- vmm-protobuf-services
- vmm-proto-to-java

We will be leveraging these to generate metropolis and anduril proto interfaces and client and server stubs.

Adonis will leverage vmm-proto-to-java to call metropolis for any HTTP requests it receives.

## Canaveral defaults

### Welcome to your new service
We've created an empty service structure to show you the setup and workflow with Canaveral.

### Directory Structure
At any time you should ensure that your repo has the following top level directory:
  1. `/package`: add your `Dockerfile` under `/package/docker/` if you want to build docker image. You're able to reference files and/or folders directly in your `Dockerfile`. This is because all files and folders under `/services` will be copied into the same folder as the `Dockerfile` during build.
  2. `/services`: put your top-level code directory under `/services`, e.g. `/services/my_service_code/`.
  3. `circle.yml`: this file contains instructions to build the project.
  4. `blueprint.json`: this file contains instructions for Canaveral to deploy the service.

### Build
Canaveral uses CircleCI for building, packaging and alerting its Deployment Engine. A CircleCI repository should have been created for you when you registered your service. Here are some additional steps you should follow to ensure proper builds:

##### Ensure `circle.yml` has the correct variables (docker image only)
  1. Specify your prefered `CANAVERAL_BUILD_SYSTEM` (default is noop)
  2. Specify your prefered `CANAVERAL_PACKAGE_TOOLS` (use "docker" if deploying a docker image, use "noop" if no packaging is needed)
  3. **[OPTIONAL]** Specify the target `DOCKERFILE_NAME` to use  (default is Dockerfile)

You'll be able to monitor the build in https://drt-it-circleci-prod-1.eng.nutanix.com/dashboard .

### Deployment
To use Canaveral for deployment, `blueprint.json` should be placed at the top level of the repo. Spec for the blueprint can be found at
https://drt-it-github-prod-1.eng.nutanix.com/xi-devops/canaveral-utils/blob/master/docs/blueprint-spec-draft1.md

### Monitoring
Canaveral uses [Sentry](https://sentry.io/welcome/) as the default error tracking tool. To track logs for your service, go to [[sentry_project_url]]. You should be able to login with your github account, and navigate to your org: nutanix-core. Then follow the instructions to setup clients to send traces to the service.

__Questions, issues or suggestions? Reach us at https://nutanix.slack.com/messages/xi-canaveral-question/.__

