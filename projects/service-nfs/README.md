# service-nfs

NFS server for shared write-many storage in K8s cluster

# Development Env Notes

> Background; [Workaround for limitation on local system](https://github.com/tonymichaelson/k8s-nfs-operation-not-permittied-workaround)

This means that the development server runs differently than the cloud version. In the cloud, the state will be stored to a provisioned block storage volume that is managed by the kubernetes cluster.

# K8s Note

In the `nfs.yml` file edit the `hostdata / hostPath / path` directory to a location on your system that you prefer to store files that will be availabe inside the `service-nfs` runtime.

# Docker-Desktop Configuration Requirements

Ensure under `preferences / resources / file sharing` that you add the folder or any root of the path used in `nfs.yml`

# Build Image

> Ensure you are in the same directory as the `Dockerfile` then run the `build.sh` script.

`./build.sh`

If you provide a version number in the format of `^[0-9]+\.[0-9]+$` the `build.sh` script will update `CURRENT_VERSION` in `VERSION.env` and ensure the argument you provide is greater than the `CURRENT_VERSION` set in the file.

# Deploy on K8s

```bash
./deploy_local.sh
```

# NFS Mount Test

> Optionally, you can ensure the NFS server is working with an ubuntu host.

### Edit ubuntu.yml

> You must get the nfs cluster IP from `k get service` and use that IP in the `ubuntu.yml` file.

```bash
k apply -f deployment/dev/ubuntu.yml

mac:service-nfs tony$ k get pods
NAME           READY   STATUS    RESTARTS   AGE
nfs-client     1/1     Running   0          2m21s
nfs-server-0   1/1     Running   0          17h

k exec --stdin --tty nfs-client -- bash

root@nfs-client:/# ls -l /data
total 12
drwxr-xr-x 4 root root 4096 Aug 10 22:49 conf
drwxr-xr-x 2 root root 4096 Aug 10 22:49 docker-images
drwxr-xr-x 5 root root 4096 Aug 10 22:49 sites
root@nfs-client:/# echo "test content" > /data/test.txt
root@nfs-client:/# cat /data/test.txt
test content
root@nfs-client:/# rm /data/test.txt
root@nfs-client:/# ls -l /data/
total 12
drwxr-xr-x 4 root root 4096 Aug 10 22:49 conf
drwxr-xr-x 2 root root 4096 Aug 10 22:49 docker-images
drwxr-xr-x 5 root root 4096 Aug 10 22:49 sites
root@nfs-client:/# exit

k delete -f deployment/dev/ubuntu.yml
```

> There will be a delay in deleting the pod while it unmounts the NFS share. `ctrl-c` is safe as pod termination will continue in the background.