#!/usr/bin/env bash
set -eo pipefail

update-commit-status() {
  commit_status="${1}"
  repo_name="${2}"
  git_commit="${3}"
  target_url="${4}"
  description="${5}"

  curl \
    --silent \
    --user deis-admin:"${GITHUB_ACCESS_TOKEN}" \
    --data '{ \
    "state":"'"${commit_status}"'", \
    "target_url":"'"${target_url}"'", \
    "description":"'"${description}"'", \
    "context":"ci/jenkins/pr"}' \
    "https://api.github.com/repos/deis/${repo_name}/statuses/${git_commit}"
}
