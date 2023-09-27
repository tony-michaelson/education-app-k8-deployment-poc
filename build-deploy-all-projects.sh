#!/usr/bin/env bash

NFS_DATA_DIR="$(pwd)/nfs-data/"
DOCKER_IMAGES_DIR="$(pwd)/docker-images/"

./secrets.sh

cd projects || exit 1
PROJECT_DIR=$(pwd)

kubectl apply -f ./service-api/server/deployment/env/dev/config.yml

function mainDir() {
  cd $PROJECT_DIR || exit 1
}

function deployed() {
  echo ""
  echo "###### Deployed $1 ######"
  echo ""
  sleep 1
}

function waitForRunningPod() {
  if kubectl get pods | grep $1 | grep -i 'Running'; then
    echo ""
    echo "###### $1 Pod is Running ######"
    echo ""
    sleep 1
  else
    waitForRunningPod $1
  fi
}

cd service-nfs || exit 1
./build.sh
./deploy_local.sh $NFS_DATA_DIR
deployed "NFS"
waitForRunningPod "nfs"

mainDir
cd service-mq || exit 1
./build.sh
./deploy_local.sh
deployed "Message Queue Service"
waitForRunningPod "rabbit"

mainDir
cd service-nginx || exit 1
./build.sh
./deploy_local.sh
deployed "Nginx"
waitForRunningPod "nginx"

mainDir
cd service-jekyll || exit 1
./build.sh
./deploy_local.sh
deployed "Jekyll"
waitForRunningPod "jekyll"

mainDir
cd service-dind || exit 1
./build.sh
./deploy_local.sh $DOCKER_IMAGES_DIR
deployed "Docker-in-Docker"
waitForRunningPod "dind"

mainDir
cd service-certbot || exit 1
./build.sh
./deploy_local.sh
deployed "Certbot"
waitForRunningPod "certbot"

mainDir
cd service-api/server || exit 1
./build_docker.sh
./deploy_local.sh $(pwd)
deployed "API Service"
waitForRunningPod "api"

echo "Running REST API compilation. This will take a while ... "
echo "https://youtu.be/VB4CCHHYOqY"
echo ""

sleep 5

API_POD_NAME=$(kubectl get pods | grep 'api' | awk -F" +" '{print $1}')
kubectl exec $API_POD_NAME -- /app/scripts/sbt_run.sh compile

sleep 5

kubectl exec --stdin --tty $API_POD_NAME -- /app/scripts/sbt_run.sh