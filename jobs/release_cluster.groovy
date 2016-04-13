evaluate(new File("${WORKSPACE}/common.groovy"))

import utilities.StatusUpdater

name = "${defaults.k8sClaimer['repoName']}-lease"

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

  logRotator {
    numToKeep defaults.numBuildsToKeep
  }

  wrappers {
    timestamps()
    colorizeOutput 'xterm'
    credentialsBinding {
      string("GITHUB_ACCESS_TOKEN", "8e11254f-44f3-4ddd-bf98-2cabcb7434cd")
    }
  }

  publishers {
    // TODO: need to even update commit status on cluster release?
    def statuses = [['SUCCESS', 'success'],['FAILURE', 'failure'],['ABORTED', 'error']]
    postBuildScripts {
      onlyIfBuildSucceeds(false)
      steps {
        statuses.each { buildStatus, commitStatus ->
          conditionalSteps {
            condition {
              status(buildStatus, buildStatus)
              steps {
                shell StatusUpdater.updateStatus(
                  commitStatus: commitStatus, repoName: '${COMPONENT_REPO}', commitSHA: '${ACTUAL_COMMIT}', description: "\${KUBE_CLUSTER_NAME} k8s cluster released")
              }
            }
          }
        }
      }
    }
  }

  steps {
    // TODO: need to even update commit status on cluster release?
    shell StatusUpdater.updateStatus(
      commitStatus: 'pending', repoName: '${COMPONENT_REPO}', commitSHA: '${ACTUAL_COMMIT}', description: 'Releasing k8s cluster ${KUBE_CLUSTER_NAME}...')

    shell '''
      #!/usr/bin/env bash

      set -eo pipefail

      #release cluster

    '''.stripIndent().trim()
  }
}
