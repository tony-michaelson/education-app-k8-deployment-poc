FROM certbot/dns-route53:v1.13.0

ARG VERSION

RUN [[ $VERSION > 0 ]] || exit 1

RUN apk update

RUN apk add python3 bash nginx

RUN mkdir /run/nginx

RUN pip3 install pika boto3

ADD ./src/ /app/

RUN chmod 0755 /app/*.sh
RUN chmod 0755 /app/*.py

RUN mkdir ~/.aws

ENTRYPOINT [ "/app/start.sh" ]