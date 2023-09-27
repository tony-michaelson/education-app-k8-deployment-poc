FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1

MAINTAINER Masterypath.io <customersupport@masterypath.io>

ADD sbt_project /app/
WORKDIR /app

RUN apt-get update && \
	apt-get install -y dos2unix zip unzip

RUN curl -s "https://get.sdkman.io" | bash

RUN /bin/bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install scala 2.13.3"

RUN sbt assembly

RUN cp target/scala-2.13/*ScalaTester*.jar Resources.jar

COPY start.sh /app/start.sh
RUN ["chmod", "+x", "\/app\/start.sh"]
RUN ["dos2unix", "\/app\/start.sh"]
CMD ["\/app\/start.sh"]
