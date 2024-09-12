#!/bin/bash -e

# Computes the overall test coverage of the project.
COVERAGE_THRESHOLD=85

cd "${PROJECT_ROOT_FOLDER}"
go mod tidy

# get inside services folder
echo $PROJECT_ROOT_FOLDER
cd "${PROJECT_ROOT_FOLDER}"/guru-pc-api-service

# Build the coverage profile
# shellcheck disable=SC2046
go test $(go list ./... | grep -Ev "/mocks|/consts|/errors|/models|/test-consts|/server|/interfaces|/adapter|/middleware|/grpc") -coverprofile=coverage.out -timeout=6m -covermode=count
go tool cover -func=coverage.out 

# Output the percent
COVERAGE_PERCENT=$(go tool cover -func=coverage.out | \
  tail -n 1 | \
  awk '{ print $3 }' | \
  sed -e 's/^\([0-9]*\).*$/\1/g')

echo "Coverage of the project is $COVERAGE_PERCENT"
echo "Coverage Threshold of the project is $COVERAGE_THRESHOLD"

# Fail the build if the coverage is less than the threshold
if [ "$COVERAGE_PERCENT" -lt $COVERAGE_THRESHOLD ] ; then
 echo "Coverage check failed. Please add more test cases"
 exit 1
else
 echo "Coverage check passed"
 exit 0
fi

# back to project root folder
cd "${PROJECT_ROOT_FOLDER}"
