FROM itsthenetwork/nfs-server-alpine:latest

ARG VERSION

RUN [[ $VERSION > 0 ]] || exit 1

RUN mkdir /data

RUN echo "Hello There!" > /data/hello.txt

VOLUME /data