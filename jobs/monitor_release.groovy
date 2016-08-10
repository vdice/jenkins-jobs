evaluate(new File("${WORKSPACE}/common.groovy"))

repoName = 'monitor'
name = "${repoName}-release"

job(name) {
  description """
    <ol>
      <li>Watches the ${repoName} repo for a git tag push. (It can also be triggered manually, supplying a value for TAG.)</li>
      <li>Unless TAG is set (manual trigger), this job only runs off the latest tag.</li>
      <li>The commit at HEAD of tag is then used to locate the release candidate images for all the ${repoName} sub-components.</li>
      <li>Retags release candidates with official semver tag in the 'deis' registry orgs.</li>
    </ol>
  """.stripIndent().trim()

  scm {
    git {
      remote {
        github("deis/${repoName}")
        credentials('597819a0-b0b9-4974-a79b-3a5c2322606d')
        refspec('+refs/tags/*:refs/remotes/origin/tags/*')
      }
      extensions {
        relativeTargetDirectory(repoName)
    }
      branch('*/tags/*')
    }
  }

  logRotator {
    daysToKeep defaults.daysToKeep
  }

  publishers {
    slackNotifications {
      notifyAborted()
      notifyFailure()
      notifySuccess()
      notifyRepeatedFailure()
     }
   }

  parameters {
    stringParam('TAG', '', 'Specific tag to release')
  }

  triggers {
    githubPush()
  }

  wrappers {
    buildName('${GIT_BRANCH} ${TAG} #${BUILD_NUMBER}')
    timestamps()
    colorizeOutput 'xterm'
  }

  steps {
    shell """
      #!/usr/bin/env bash

      set -eo pipefail

      cd ${repoName}

      (${new File("${WORKSPACE}/bash/scripts/locate_release_candidate.sh").text})
    """.stripIndent().trim()

    conditionalSteps {
      condition {
        not {
          shell 'cat "${WORKSPACE}/env.properties" | grep -q SKIP_RELEASE'
        }
      }
      steps {
        downstreamParameterized {
          trigger('release-candidate-promote') {
            block {
              buildStepFailure('FAILURE')
              failure('FAILURE')
              unstable('UNSTABLE')
            }
            parameters {
              propertiesFile('${WORKSPACE}/env.properties')
            }
          }
        }
      }
    }
  }
}
