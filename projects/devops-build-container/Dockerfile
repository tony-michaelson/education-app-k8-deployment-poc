FROM node:10-slim

ARG VERSION
ARG KUBECTL_VERSION
ARG DOCTL_VERSION

SHELL ["/bin/bash", "-c"]

RUN [[ $VERSION > 0 ]] || exit 1
RUN [[ $KUBECTL_VERSION > 0 ]] || exit 1
RUN [[ $DOCTL_VERSION > 0 ]] || exit 1

RUN apt-get update
RUN apt-get -qq install \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common \
    openssh-client \
    git \
    python-dev \
    build-essential \
    gnupg2 \
    libpq-dev \
    python-pip \
    wget \
    nfs-common

RUN cd ~; wget https://github.com/digitalocean/doctl/releases/download/v${DOCTL_VERSION}/doctl-${DOCTL_VERSION}-linux-amd64.tar.gz
RUN tar -C /usr/local/bin -xf ~/doctl-${DOCTL_VERSION}-linux-amd64.tar.gz
RUN rm ~/doctl-${DOCTL_VERSION}-linux-amd64.tar.gz

RUN curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg | apt-key add - \
    && apt-key fingerprint 0EBFCD88 \
    && add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
    $(lsb_release -cs) \
    stable" \
    && apt-get update \
    && apt-get -qq install docker-ce

RUN apt-get -qq install python3-pip

RUN pip3 install "docker-compose<1.28"

WORKDIR /root

RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl
RUN chmod +x ./kubectl
RUN mv ./kubectl /usr/local/bin/kubectl
RUN kubectl

RUN pip3 install boto3