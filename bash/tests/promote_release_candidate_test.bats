#!/usr/bin/env bats

setup() {
  . "${BATS_TEST_DIRNAME}/../scripts/promote_release_candidate.sh"
  load stub
  stub docker

  STAGING_ORG="deisci"
  PROD_ORG="deis"
}

teardown() {
  rm_stubs
}

@test "main : COMPONENT_NAME empty" {
  run main

  [ "${status}" -eq 0 ]
  [ "${output}" = "COMPONENT_NAME is empty; no component to promote; exiting..." ]
}

@test "main : COMPONENT_NAME not monitor" {
  export COMPONENT_NAME="my-component"
  export COMPONENT_SHA="abc1234def5678"

  run main

  [ "${status}" -eq 0 ]
  [ "${lines[0]}" = "Promoting ${STAGING_ORG}/my-component:git-abc1234 to ${PROD_ORG}/my-component:git-abc1234" ]
  [ "${lines[1]}" = "Promoting quay.io/${STAGING_ORG}/my-component:git-abc1234 to quay.io/${PROD_ORG}/my-component:git-abc1234" ]
}

@test "main : COMPONENT_NAME is monitor" {
  export COMPONENT_NAME="monitor"
  export COMPONENT_SHA="abc1234def5678"
  export RELEASE_TAG="v9.9.9"

  run main

  [ "${status}" -eq 0 ]
  [ "${lines[0]}" = "Promoting ${STAGING_ORG}/grafana:git-abc1234 to ${PROD_ORG}/grafana:git-abc1234" ]
  [ "${lines[1]}" = "Promoting quay.io/${STAGING_ORG}/grafana:git-abc1234 to quay.io/${PROD_ORG}/grafana:git-abc1234" ]
  [ "${lines[2]}" = "Promoting ${STAGING_ORG}/influxdb:git-abc1234 to ${PROD_ORG}/influxdb:git-abc1234" ]
  [ "${lines[3]}" = "Promoting quay.io/${STAGING_ORG}/influxdb:git-abc1234 to quay.io/${PROD_ORG}/influxdb:git-abc1234" ]
  [ "${lines[4]}" = "Promoting ${STAGING_ORG}/telegraf:git-abc1234 to ${PROD_ORG}/telegraf:git-abc1234" ]
  [ "${lines[5]}" = "Promoting quay.io/${STAGING_ORG}/telegraf:git-abc1234 to quay.io/${PROD_ORG}/telegraf:git-abc1234" ]
}
