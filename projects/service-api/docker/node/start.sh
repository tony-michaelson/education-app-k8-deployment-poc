#!/usr/bin/env bash

userCode=$(cat /input/code)
testCode=$(cat /input/test)

cat << EOF > /app/test/test.js
const assert = require('assert').strict;
$userCode
$testCode
EOF

yarn test > test.out 2>&1

if [ $? == 0 ]; then
  echo "pass"
fi

cat test.out
exit 0
