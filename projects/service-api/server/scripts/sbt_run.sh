#!/usr/bin/env bash

function setupDB {
  echo "${POSTGRES_HOST}:${POSTGRES_PORT}:${POSTGRES_DB}:${POSTGRES_USER}:${POSTGRES_PASSWORD}" > ~/.pgpass
  chmod 0600 ~/.pgpass
  pg_dump -U ${POSTGRES_USER} -h ${POSTGRES_HOST} ${POSTGRES_DB} > /tmp/${POSTGRES_DB}-backup.sql
  psql -U ${POSTGRES_USER} -h ${POSTGRES_HOST} -f ./modules/flyway/migration/DOWNS.sql
}

function restoreDB {
  psql -U ${POSTGRES_USER} -h ${POSTGRES_HOST} -f ./modules/flyway/migration/DOWNS.sql
  psql -U ${POSTGRES_USER} -h ${POSTGRES_HOST} ${POSTGRES_DB} < /tmp/${POSTGRES_DB}-backup.sql
}

if [[ $HOSTNAME == *"mpio-api"* ]]; then
  if [[ ! $(pwd) == "/app" ]]; then cd /app; fi
fi

if [ "$1" ] && [ "$1" == "test" ]; then
  setupDB
  sbt flyway/flywayMigrate slick/slickCodegen slick/generateRepos test
  ./scripts/test-psql-constraints.py
  restoreDB
elif [ "$1" ] && [ "$1" == "compile" ]; then
  sbt flyway/flywayMigrate slick/slickCodegen slick/generateRepos compile
elif [ "$1" ] && [ "$1" == "compileRun" ]; then
  sbt flyway/flywayMigrate slick/slickCodegen slick/generateRepos compile run
else
  sbt run
fi