# service-dind

Docker runner service container

# K8s Notes

In the `dind-deployment.yml` file edit the `docker-images / hostPath / path` directory to a location on your system that you prefer to store docker images that will be availabe inside the `service-dind` runtime.

# Docker-Desktop Configuration Requirements

Ensure under `preferences / resources / file sharing` that you add the folder or any root of the path used in `dind-deployment.yml`

# Build Image

> Ensure you are in the same directory as the `Dockerfile` then run the `build.sh` script.

`./build.sh`

If you provide a version number in the format of `^[0-9]+\.[0-9]+$` the `build.sh` script will update `CURRENT_VERSION` in `VERSION.env` and ensure the argument you provide is greater than the `CURRENT_VERSION` set in the file.

# Deploy on K8s

```bash
./deploy_local.sh /path/to/docker-images
```