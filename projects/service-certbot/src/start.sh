#!/usr/bin/env bash

AWS_CRED_FILE=~/.aws/config
echo "[default]" > $AWS_CRED_FILE
echo "aws_access_key_id=${ACCESS_KEY_ID}" >> $AWS_CRED_FILE
echo "aws_secret_access_key=${ACCESS_KEY}" >> $AWS_CRED_FILE

echo "starting server ..."

/app/server.py