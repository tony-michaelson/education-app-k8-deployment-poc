#!/usr/bin/env bash
rm -rf /sites/staging.masterypath.io
mv /app/client-build-dir/ /sites/staging.masterypath.io
flyway migrate
/app/bin/masterypath-io -Dplay.http.secret.key=${PLAY_HTTP_SECRET_KEY}
