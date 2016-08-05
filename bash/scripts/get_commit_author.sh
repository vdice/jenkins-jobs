#!/usr/bin/env bash

set -eo pipefail

main() {
  echo COMMIT_AUTHOR_EMAIL="$(git rev-parse HEAD | git --no-pager show -s --format='%ae')" \
    >> "${ENV_PROPS_FILEPATH:-/tmp/${JOB_NAME}/${BUILD_NUMBER}/env.properties}"
}

if [ -n "${JENKINS_HOME}" ]; then
  main
fi
