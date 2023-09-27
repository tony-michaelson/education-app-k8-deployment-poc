#!/usr/bin/env bash

doctl auth init --access-token ${DOCTL_TOKEN}
doctl registry login

sleep 10

docker-entrypoint.sh dockerd &

echo "starting server ..."

touch /app/server.log
/app/server.py