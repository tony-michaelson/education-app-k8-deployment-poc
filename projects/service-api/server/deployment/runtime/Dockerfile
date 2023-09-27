FROM java:8-jre

MAINTAINER masterypath.io <issues-dev@masterypath.io>

ARG MPIO_VERSION
ENV FLYWAY_VERSION=7.3.1

COPY masterypath-io-$MPIO_VERSION.tgz /app/
RUN tar -zxpf /app/masterypath-io-$MPIO_VERSION.tgz -C /app
RUN rm /app/masterypath-io-$MPIO_VERSION.tgz

COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

RUN wget https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/${FLYWAY_VERSION}/flyway-commandline-${FLYWAY_VERSION}.tar.gz \
  && tar -xzf flyway-commandline-${FLYWAY_VERSION}.tar.gz -C / \
  && mv /flyway-${FLYWAY_VERSION} /flyway \
  && rm flyway-commandline-${FLYWAY_VERSION}.tar.gz

ENV PATH="/flyway:${PATH}"

COPY flyway.conf /flyway/conf/flyway.conf
COPY sql/*.sql /flyway/sql/

WORKDIR /home/

RUN rm /etc/apt/sources.list.d/jessie-backports.list
RUN apt-get -qq update
RUN apt-get -qq install python

ADD build /app/client-build-dir/

EXPOSE 9000
CMD ["/app/start.sh"]
