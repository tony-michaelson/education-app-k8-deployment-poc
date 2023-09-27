#!/usr/bin/env bash
source /root/.sdkman/bin/sdkman-init.sh

cp /input/code code.scala
cp /input/test tests.scala

scalac -cp Resources.jar code.scala tests.scala 2>&1

if [ $? != 0 ]; then
  exit 0
fi

scala -cp Resources.jar org.scalatest.tools.Runner -fWU test.out -R ./ -s Tests

output=$(cat test.out)

if [[ $output == *"All tests passed."* ]]; then
  echo "pass"
fi

cat test.out

exit 0
