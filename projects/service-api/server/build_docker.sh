#!/usr/bin/env bash

docker build -t masterypath.io/service-api:latest deployment/env/dev/api-dev-dockerfile/

if [[ $(kubectl config current-context) =~ microk8s ]]; then
  docker tag masterypath.io/service-api:latest \
    localhost:32000/masterypath.io/service-api:latest
  docker push localhost:32000/masterypath.io/service-api:latest
fi