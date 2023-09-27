FROM node:14.11.0-stretch-slim

MAINTAINER Masterypath.io <customersupport@masterypath.io>

ADD project /app/
WORKDIR /app

RUN apt-get update && \
	apt-get install -y dos2unix

RUN yarn add --dev mocha

COPY start.sh /app/start.sh
RUN ["chmod", "+x", "\/app\/start.sh"]
RUN ["dos2unix", "\/app\/start.sh"]
CMD ["\/app\/start.sh"]
