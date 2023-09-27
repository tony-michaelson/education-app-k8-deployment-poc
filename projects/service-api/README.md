# Project Overview

This project is deployed on Kubernetes and requires at least the following services to use the API service.

- [service-nfs](https://code.solern.com/masterypath/service-nfs)
- [service-mq](https://code.solern.com/masterypath/service-mq)
- [service-nginx](https://code.solern.com/masterypath/service-nginx)
- [service-jekyll](https://code.solern.com/masterypath/service-jekyll)
- [service-dind](https://code.solern.com/masterypath/service-dind)
- [service-certbot](https://code.solern.com/masterypath/service-certbot)
- [service-api](https://code.solern.com/masterypath/service-api)

# Gitflow

[Using standard gitflow style repo management](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)

> Caveat; `master` branch is treated as a `develop` branch as seen in the above URL. This is to avoid the common mistake of commiting directly to `master` by mistake and pushing unintentional updates to `PROD`.
> We're branching off `master` for each feature in the format of `issue#/username`.

# Setting up Development Environment

> Install docker-desktop and enable kubernetes

> `k` is an alias of `kubectl` and is commonly used as such in the community

# Secrets

> Ask for the secrets you'll need to load into your local K8s

> `k describe secret` should have at least:

```
Name:         api
Data
====
PLAY_HTTP_SECRET_KEY:  63 bytes


Name:         auth0
Data
====
AUTH0_CLIENT_ID:      32 bytes
AUTH0_CLIENT_SECRET:  29 bytes


Name:         aws-certbot-ssl
Data
====
ACCESS_KEY:     40 bytes
ACCESS_KEY_ID:  20 bytes

Name:         digitalocean
Data
====
DO_SECRET_KEY:  43 bytes

Name:         docker
Data
====
DOCTL_TOKEN:  64 bytes


Name:         mq
Data
====
MQ_PASSWORD:  10 bytes
MQ_USERNAME:  10 bytes


Name:         postgresql
Data
====
password:  16 bytes
username:  7 bytes


Name:         sendgrid
Data
====
SENDGRID_APIKEY:  69 bytes
```

# Config

> Configuration keys are needed for the REST API. Create them in K8s like so:

`k apply -f ./server/deployment/env/dev/config.yml`

# Services

> Now setup each service in the following order using the project READMEs:

- [service-nfs](https://code.solern.com/masterypath/service-nfs)
- [service-mq](https://code.solern.com/masterypath/service-mq)
- [service-nginx](https://code.solern.com/masterypath/service-nginx)
- [service-jekyll](https://code.solern.com/masterypath/service-jekyll)
- [service-dind](https://code.solern.com/masterypath/service-dind)
- [service-certbot](https://code.solern.com/masterypath/service-certbot)

# REST API

See [README.md](https://code.solern.com/masterypath/service-api/-/blob/master/server/README.md) for the REST API server for setup details.

# Client UI

See [README.md](https://code.solern.com/masterypath/service-api/-/tree/master/client) for the client code for setup details.