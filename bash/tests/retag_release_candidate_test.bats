#!/usr/bin/env bats

setup() {
  . "${BATS_TEST_DIRNAME}/../scripts/retag_release_candidate.sh"
  load stub
  stub docker
}

teardown() {
  rm_stubs
}

@test "main : COMPONENT_NAME not monitor" {
  export COMPONENT_NAME="my-component"
  export COMPONENT_SHA="abc1234def5678"
  export RELEASE_TAG="v9.9.9"

  run main

  [ "${status}" -eq 0 ]
  [ "${lines[0]}" = "Retagging deis/my-component:git-abc1234 to deis/my-component:v9.9.9" ]
  [ "${lines[1]}" = "Retagging quay.io/deis/my-component:git-abc1234 to quay.io/deis/my-component:v9.9.9" ]
}

@test "main : COMPONENT_NAME is monitor" {
  export COMPONENT_NAME="monitor"
  export COMPONENT_SHA="abc1234def5678"
  export RELEASE_TAG="v9.9.9"

  run main

  [ "${status}" -eq 0 ]
  [ "${lines[0]}" = "Retagging deis/grafana:git-abc1234 to deis/grafana:v9.9.9" ]
  [ "${lines[1]}" = "Retagging quay.io/deis/grafana:git-abc1234 to quay.io/deis/grafana:v9.9.9" ]
  [ "${lines[2]}" = "Retagging deis/influxdb:git-abc1234 to deis/influxdb:v9.9.9" ]
  [ "${lines[3]}" = "Retagging quay.io/deis/influxdb:git-abc1234 to quay.io/deis/influxdb:v9.9.9" ]
  [ "${lines[4]}" = "Retagging deis/telegraf:git-abc1234 to deis/telegraf:v9.9.9" ]
  [ "${lines[5]}" = "Retagging quay.io/deis/telegraf:git-abc1234 to quay.io/deis/telegraf:v9.9.9" ]
}
