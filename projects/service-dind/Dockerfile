FROM docker:20.10.5-dind

ARG VERSION

RUN [[ $VERSION > 0 ]] || exit 1

RUN apk update

RUN apk add python3 bash

RUN apk add py3-pip

RUN pip3 install pika

WORKDIR /root

RUN wget https://github.com/digitalocean/doctl/releases/download/v1.59.0/doctl-1.59.0-linux-amd64.tar.gz
RUN tar xf ~/doctl-1.59.0-linux-amd64.tar.gz
RUN mv ~/doctl /usr/local/bin

RUN mkdir /input/

ADD ./src/ /app/
RUN chmod 0755 /app/*.sh
RUN chmod 0755 /app/*.py

EXPOSE 8080