# masterypath.io-jekyll-deploy

This service is for building educational content into static sites.

## Updating Dockerfile

- Check base_image for jekyll [here](https://github.com/envygeeks/jekyll-docker/blob/master/opts.yml)

# Build

> Ensure you are in the same directory as the `Dockerfile` then run the `build.sh` script.

`./build.sh`

If you provide a version number in the format of `^[0-9]+\.[0-9]+$` the `build.sh` script will update `CURRENT_VERSION` in `VERSION.env` and ensure the argument you provide is greater than the `CURRENT_VERSION` set in the file.

# Deploy on K8s

> WARNING! Ensure you are in the correct K8s context. Normally you want to be in the `docker-desktop` context. This is set using the context menu of the Docker-desktop icon / Kubernetes.

```bash
./deploy_local.sh
```