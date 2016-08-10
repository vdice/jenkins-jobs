#!/usr/bin/env bash

set -eo pipefail

main() {
  envPropsFilepath="${BATS_TEST_DIRNAME}/tmp/env.properties"
  if [ -n "${JENKINS_HOME}" ]; then
    envPropsFilepath="${WORKSPACE}/env.properties"
  fi

  tag=''
  if [ -n "${TAG}" ]; then
    echo "TAG set to '${TAG}', attempting release of this tag..."
    tag="${TAG}"
  elif [ -n "${GIT_BRANCH}" ]; then
    echo "GIT_BRANCH set to '${GIT_BRANCH}', attempting release of this tag..."
    tag="${GIT_BRANCH#origin/tags/}"

    # make sure tag matches latest; else, exit
    latest_tag="${LATEST_COMPONENT_TAG:-$(git tag -l | tail -n1)}"
    if [ "${latest_tag}" != "${tag}" ]; then
      echo "Latest tag of '${latest_tag}' does not match '${tag}'; not proceeding with release."
      echo SKIP_RELEASE=true > "${envPropsFilepath}"
      exit 0
    fi
  else
    echo "GIT_BRANCH or TAG not set, cannot determine tag to release; exiting."
    exit 1
  fi

  # get component name (same as git repo name) and env var name for downstream jobs
  component_name="${COMPONENT_NAME:-$(basename "$(git rev-parse --show-toplevel)")}"
  component_env_var="$(echo "${component_name}" | perl -ne 'print uc' | sed 's/-/_/g')"_SHA
  git_commit="${COMPONENT_SHA:-$(git rev-parse HEAD)}"

  if [ "${component_name}" == 'monitor' ]; then
    # all monitor sub-component candidates share the same monitor sha
    # just pick one to locate -- if one exists, they all should.
    candidate_image=quay.io/deis/grafana:git-"${git_commit:0:7}"
  else
    candidate_image=quay.io/deis/"${component_name}":git-"${git_commit:0:7}"
  fi
  
  echo "Locating candidate release image ${candidate_image}..."
  docker pull "${candidate_image}"

  { echo COMPONENT_NAME="${component_name}"; \
    echo COMPONENT_SHA="${git_commit}"; \
    echo RELEASE_TAG="${tag}"; \
    echo "${component_env_var}"="${git_commit}"; } > "${envPropsFilepath}"
}

if [ -n "${JENKINS_HOME}" ]; then
  main
fi
