#!/usr/bin/env bash

source VERSION.env
DEPLOYMENT_TEMPLATE=./deployment/dev/$SERVICE_NAME.yml.template
DEPLOYMENT_FILE=./deployment/dev/$SERVICE_NAME.yml
PV_TEMPLATE=./deployment/dev/$SERVICE_NAME-pv.yml.template
PV_FILE=./deployment/dev/$SERVICE_NAME-pv.yml
PVC_FILE=./deployment/dev/$SERVICE_NAME-pvc.yml
SERVICE_FILE=./deployment/dev/$SERVICE_NAME-service.yml
NFS_DATA_FOLDER=$1
IMAGE_PREFIX=""

if [[ $(kubectl config current-context) =~ microk8s ]]; then
  IMAGE_PREFIX="localhost:32000/"
fi

deploy() {
  if [[ $(kubectl config current-context) =~ docker-desktop|microk8s ]];
  then
    cat $DEPLOYMENT_TEMPLATE | 
      sed "s~{{IMAGE_PREFIX}}~${IMAGE_PREFIX}~g" |
      sed "s/{{VERSION}}/${CURRENT_VERSION}/g" |
      sed "s~{{NFS_DATA_FOLDER}}~${NFS_DATA_FOLDER}~g" > $DEPLOYMENT_FILE
    echo "Deploying nfs server"
    kubectl apply -f $DEPLOYMENT_FILE
    sleep 5
    kubectl get pods | grep 'nfs'
    echo "Deploying nfs service"
    kubectl apply -f $SERVICE_FILE
    sleep 5
    kubectl get service | grep 'nfs'
    NFS_SERVICE_IP=$(kubectl get service | grep 'nfs' | awk -F " +" '{print $3}')
    echo "Collected nfs service IP of: ${NFS_SERVICE_IP}"
    cat $PV_TEMPLATE | sed "s/{{NFS_SERVICE_IP}}/${NFS_SERVICE_IP}/g" > $PV_FILE
    echo "Applying persistent volumes"
    kubectl apply -f $PV_FILE
    sleep 2
    kubectl get pv
    echo "Applying persistent volume claims"
    kubectl apply -f $PVC_FILE
    sleep 2
    kubectl get pvc
  else
    echo "Context not set to docker-desktop|microk8s for kubernetes" >&2
    exit 1
  fi
}

if [[ -d "$NFS_DATA_FOLDER" ]];
then
  deploy
else
  echo "Please supply a valid full path to the directory used for storing nfs data" >&2
  echo "Initial state should be available for this project" >&2
  exit 1
fi