#!/usr/bin/env bash

echo "starting server ..."

if [ ! -d "/sites/.build_files" ]; then
  mkdir /sites/.build_files
fi

touch /app/server.log
/app/server.py