#!/usr/bin/env bash

USER=$1
if [[ ! -d projects ]]; then mkdir projects; fi
cd projects || exit 1

git clone git@code.solern.com:masterypath/service-nfs.git
git clone git@code.solern.com:masterypath/service-mq.git
git clone git@code.solern.com:masterypath/service-nginx.git
git clone git@code.solern.com:masterypath/service-jekyll.git
git clone git@code.solern.com:masterypath/service-dind.git
git clone git@code.solern.com:masterypath/service-certbot.git
git clone git@code.solern.com:masterypath/service-api.git