#!/usr/bin/env bash

source VERSION.env

PROPER_VERSION_FORMAT='^[0-9]+\.[0-9]+$'
NEW_VERSION=$1

# https://stackoverflow.com/questions/12765340/difference-between-parentheses-and-brackets-in-bash-conditionals

# Function to ensure version is formatted as n.n i.e. 1.4
function isProperVersionFormat() {
  if [[ $1 =~ $PROPER_VERSION_FORMAT ]] ; then
    exit 0
  else
    exit 1
  fi
}

# Function to check if CURRENT_VERSION exists and is properly formatted
function checkCurrentVersion() {
  if [ -z "$CURRENT_VERSION" ]; then
    echo "CURRENT_VERSION not set in VERSION.env!" >&2
    exit 1
  else
    echo "Current Version is: ${CURRENT_VERSION}"
  fi
}

# Function to check if CURRENT_VERSION exists and is properly formatted
function checkNewVersionFormat() {
  if [[ ! $1 =~ $PROPER_VERSION_FORMAT ]] ; then
    echo "New version specified as argument does not match pattern '${PROPER_VERSION_FORMAT}'!" >&2
    exit 1
  fi
}

# Function to ensure version is higher than CURRENT_VERSION
function checkVersionIsHigher() {
   if ! (( $(echo "$1 > $CURRENT_VERSION" | bc -l) )); then
      echo "Version must be higer than: "$CURRENT_VERSION >&2
      exit 1
   fi
}

# Function to update CURRENT_VERSION in VERSION.env
function updateCurrentVersion() {
  checkNewVersionFormat $1
  checkVersionIsHigher $NEW_VERSION
  echo 'export CURRENT_VERSION='$1 > VERSION.env
  echo 'export SERVICE_NAME='$SERVICE_NAME >> VERSION.env
  source VERSION.env
}

# Function to check new version passed as arg to this script and set to CURRENT_VERSION if it's higher
function checkNewVersion() {
  if [[ ! -z "$NEW_VERSION" ]]; then
    echo "Using new version: ${NEW_VERSION}"
    updateCurrentVersion $NEW_VERSION
  fi
}

# Function to ensure Dockerfile is present
function checkDockerFileExists() {
  if ! [ -f "./Dockerfile" ]; then
    echo "Run this script from the same directory as the Dockerfile!" >&2
    exit 1
  fi
}

# MAIN
checkDockerFileExists
checkCurrentVersion
checkNewVersion

if [[ $(kubectl config current-context) =~ microk8s|docker-desktop ]]; then
  docker build --build-arg=VERSION=$CURRENT_VERSION \
    -t masterypath.io/service-$SERVICE_NAME:$CURRENT_VERSION \
    -t masterypath.io/service-$SERVICE_NAME:latest \
    .
fi

if [[ $(kubectl config current-context) =~ microk8s ]]; then
  docker tag masterypath.io/service-$SERVICE_NAME:$CURRENT_VERSION \
    localhost:32000/masterypath.io/service-$SERVICE_NAME:$CURRENT_VERSION
  docker push localhost:32000/masterypath.io/service-$SERVICE_NAME:$CURRENT_VERSION
fi