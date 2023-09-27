FROM nginx:latest

ARG VERSION

SHELL ["/bin/bash", "-c"]

RUN [[ $VERSION > 0 ]] || exit 1

RUN apt-get update

RUN apt-get -qq install python3-pip procps wget

RUN pip3 install pika

ADD ./src/ /app/

RUN chmod 0755 /app/*.sh
RUN chmod 0755 /app/*.py

ENTRYPOINT [ "/app/start.sh" ]