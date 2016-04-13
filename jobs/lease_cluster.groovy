evaluate(new File("${WORKSPACE}/common.groovy"))

import utilities.StatusUpdater

name = "${defaults.k8sClaimer['repoName']}-release"
downstreamJobName = defaults.testJob[config.type]

job(name) {
  description """
  TODO
  """.stripIndent().trim()

  scm {
    git {
      remote {
        github("deis/${defaults.k8sClaimer['repoName']}")
        // credentials('597819a0-b0b9-4974-a79b-3a5c2322606d')
      }
      branch('master')
    }
  }

  // concurrentBuild()
  // throttleConcurrentBuilds {
  //   maxPerNode(defaults.maxBuildsPerNode)
  //   maxTotal(defaults.maxTotalConcurrentBuilds)
  // }

  logRotator {
    numToKeep defaults.numBuildsToKeep
  }

  wrappers {
    timestamps()
    colorizeOutput 'xterm'
    // credentialsBinding {
    //   string("DOCKER_PASSWORD", "0d1f268f-407d-4cd9-a3c2-0f9671df0104")
    //   string("QUAY_PASSWORD", "c67dc0a1-c8c4-4568-a73d-53ad8530ceeb")
    //   string("GITHUB_ACCESS_TOKEN", "8e11254f-44f3-4ddd-bf98-2cabcb7434cd")
    // }
  }

  publishers {
    def statuses = [['SUCCESS', 'success', "\${KUBE_CLUSTER_NAME} cluster claimed!"],
                    ['FAILURE', 'failure', "Unable to claim a cluster."],
                    ['ABORTED', 'error', "Job aborted; unable to claim a cluster."]]
    postBuildScripts {
      onlyIfBuildSucceeds(false)
      steps {
        statuses.each { buildStatus, commitStatus, description ->
          conditionalSteps {
            condition {
              status(buildStatus, buildStatus)
              steps {
                shell StatusUpdater.updateStatus(
                  commitStatus: commitStatus, repoName: '${COMPONENT_REPO}', commitSHA: '${ACTUAL_COMMIT}', description: description)
              }
            }
          }
        }
      }
    }
  }

  steps {
    shell StatusUpdater.updateStatus(
      commitStatus: 'pending', repoName: '${COMPONENT_REPO}', commitSHA: '${ACTUAL_COMMIT}', description: 'Claiming k8s cluster...')

    shell '''
      #!/usr/bin/env bash

      set -eo pipefail

      # claim cluster
      # outputs LEASE_TOKEN KUBECONFIG KUBE_ENDPOINT KUBE_CLUSTER_NAME
      k8s-claimer-cli lease acquire k8s-claimer-e2e.deis.com -env

    '''.stripIndent().trim()

    // TODO: better way to just have downstream job 'inherit' this job's env?
    // Perhaps 'currentBuild()'
    // https://jenkinsci.github.io/job-dsl-plugin/#path/javaposse.jobdsl.dsl.helpers.step.StepContext.downstreamParameterized-trigger-parameters-currentBuild
    downstreamParameterized {
      trigger(downstreamJobName) {
        parameters {
          predefinedProps([
            "${repo.commitEnvVar}": '${GIT_COMMIT}',
            'UPSTREAM_BUILD_URL': '${BUILD_URL}',
            'UPSTREAM_SLACK_CHANNEL': "${repo.slackChannel}",
            'COMPONENT_REPO': "${repo.name}",
            'ACTUAL_COMMIT': '${ghprbActualCommit}',
            //TODO: cluster-specific env vars here:
            // 'LEASE_TOKEN': '${LEASE_TOKEN}'
            // ...
          ])
        }
      }
    }
  }
}
