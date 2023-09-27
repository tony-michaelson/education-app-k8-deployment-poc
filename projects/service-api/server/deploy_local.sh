#!/usr/bin/env bash

CODE_DIR=$1

if [[ ! -d "$CODE_DIR" ]]; then
  echo "Please specify the directory that contains the server code. The folder with a top level build.sbt file." >&2
  exit 1
fi

API_DEPLOYMENT=./deployment/env/dev/api-deployment.yml
API_DEPLOYMENT_TEMPLATE=./deployment/env/dev/api-deployment.yml.template
API_SERVICE=./deployment/env/dev/api-service.yml

PGWEB_POD=./deployment/env/dev/pgweb-pod.yml
PGWEB_SERVICE=./deployment/env/dev/pgweb-service.yml

POSTGRES_POD=./deployment/env/dev/postgres-pod.yml
POSTGRES_SERVICE=./deployment/env/dev/postgres-service.yml

IMAGE_PREFIX=""

if [[ $(kubectl config current-context) =~ microk8s ]]; then
  IMAGE_PREFIX="localhost:32000/"
fi

if [[ $(kubectl config current-context) =~ docker-desktop|microk8s ]];
then
  cat $API_DEPLOYMENT_TEMPLATE | 
        sed "s~{{CODE_DIR}}~${CODE_DIR}~g" |
        sed "s~{{IMAGE_PREFIX}}~${IMAGE_PREFIX}~g" |
        sed "s~{{NFS_DATA_FOLDER}}~${NFS_DATA_FOLDER}~g" > $API_DEPLOYMENT
  kubectl apply -f $POSTGRES_POD
  kubectl apply -f $POSTGRES_SERVICE
  echo "Deployed PostgreSQL DB Server ..."
  sleep 15
  kubectl apply -f $PGWEB_POD
  kubectl apply -f $PGWEB_SERVICE
  echo "Deployed PG Web Server ..."
  sleep 15
  kubectl apply -f $API_DEPLOYMENT
  kubectl apply -f $API_SERVICE
  echo "Deployed REST API Pod ..."
  kubectl get pods
else
  echo "Context not set to docker-desktop|microk8s for kubernetes" >&2
  exit 1
fi