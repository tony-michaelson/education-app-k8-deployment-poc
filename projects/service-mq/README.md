# service-mq

RabbitMQ service to facilitate messaging between microservices

# Build

> Ensure you are in the same directory as the `Dockerfile` then run the `build.sh` script.

`./build.sh`

If you provide a version number in the format of `^[0-9]+\.[0-9]+$` the `build.sh` script will update `CURRENT_VERSION` in `VERSION.env` and ensure the argument you provide is greater than the `CURRENT_VERSION` set in the file.

# Deploy on K8s

```bash
kubectl config current-context
k apply -f deployment/dev/mq-deployment.yml
k get deployments
k get pods

k apply -f deployment/dev/mq-service.yml
k get service
```