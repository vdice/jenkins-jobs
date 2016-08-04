#!/usr/bin/env bash

set -eo pipefail

# Gets actual commit for reporting status and potentially tagging Docker image
main() {
  envPropsFilepath="${ENV_PROPS_FILEPATH:-/tmp/${JOB_NAME}/${BUILD_NUMBER}/env.properties}"

  export ACTUAL_COMMIT="${sha1}"
  # if triggered by pull request plugin, use ghprbActualCommit
  if [ "${ghprbActualCommit}" != "" ]; then
    export ACTUAL_COMMIT="${ghprbActualCommit}"
    export VERSION="git-${ACTUAL_COMMIT:0:7}"
    echo "PR build, so using VERSION=${VERSION} for Docker image tag rather than the merge commit"
  fi
  echo ACTUAL_COMMIT="${ACTUAL_COMMIT}" > "${envPropsFilepath}"

  if [ "${VERSION}" != "" ]; then
    echo VERSION="${VERSION}" >> "${envPropsFilepath}"
  fi
}

main
