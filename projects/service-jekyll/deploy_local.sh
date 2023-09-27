#!/usr/bin/env bash

source VERSION.env
DEPLOYMENT_TEMPLATE=./deployment/dev/$SERVICE_NAME-deployment.yml.template
DEPLOYMENT_FILE=./deployment/dev/$SERVICE_NAME-deployment.yml
SERVICE_FILE=./deployment/dev/$SERVICE_NAME-service.yml
IMAGE_PREFIX=""

if [[ $(kubectl config current-context) =~ microk8s ]]; then
  IMAGE_PREFIX="localhost:32000/"
fi

if [[ $(kubectl config current-context) =~ docker-desktop|microk8s ]];
then
  cat $DEPLOYMENT_TEMPLATE |
    sed "s~{{IMAGE_PREFIX}}~${IMAGE_PREFIX}~g" |
    sed "s/{{VERSION}}/${CURRENT_VERSION}/g" > $DEPLOYMENT_FILE
  kubectl apply -f $DEPLOYMENT_FILE
  kubectl apply -f $SERVICE_FILE
  sleep 3
  echo ""
  kubectl get pods | grep $SERVICE_NAME
  kubectl get service | grep $SERVICE_NAME
else
  echo "Context not set to docker-desktop|microk8s for kubernetes" >&2
  exit 1
fi