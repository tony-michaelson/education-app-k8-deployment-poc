Welcome
=========

# Introduction

This is the entrypoint repository for the MasteryPath platform.

# Setting Up Dev Env

> Before starting, ensure that you install:

- [docker engine](https://docs.docker.com/engine/install/)
- [docker-desktop](https://www.docker.com/products/docker-desktop) or [microk8s](https://microk8s.io/docs)
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm/)
- [yarn](https://www.npmjs.com/package/yarn-install)

## Docker-Desktop Users

- Add the directory containing this README to `Docker-desktop / Preferences / Resources / File Sharing`
- Enable kubernetes in the docker-desktop settings

## Microk8s Users

- `sudo usermod -a -G microk8s $USER`
- `sudo chown -f -R $USER ~/.kube`
- `sudo usermod -a -G docker $USER`
- restart session and verify group memberships with `groups`
- `sudo cp kubectl /usr/local/bin/`
- `kubectl config current-context` (no sudo)
- `docker run hello-world` (no sudo)
- `microk8s enable registry`
- `microk8s enable dns`
- `sudo apt-get install nfs-common`

### Clone Repos

From this directory, run this script:

`./clone-all-projects.sh`

### Build & Deploy Projects

From this directory, run this script:

`./build-deploy-all-projects.sh`

### Run Rest API

Once completed, ensure the REST API server is running.

Run this script inside the kubernetes pod for the api: `/app/scripts/sbt_run.sh`

### Run UI Server

From this directory, run this script:

`./ui-setup.sh`

From inside the `projects/service-api/client/` directory, run `yarn start`

> Note! First REST API request will trigger a reload and be slow

### PG Web Server

[http://localhost:8081/](http://localhost:8081/)