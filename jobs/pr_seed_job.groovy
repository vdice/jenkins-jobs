evaluate(new File("${WORKSPACE}/common.groovy"))

name = 'pr-seed-job'

job(name) {
  description """
    A seed job for testing functionality introduced/affected in a Jenkins Job PR.
    *DO NOT RUN* during work hours if changes may affect ci and/or otherwise introduce breaking changes to existing jobs.
  """.stripIndent().trim()

  scm {
    git {
      remote {
        github('${FORK_ORG}/jenkins-jobs')
        credentials('597819a0-b0b9-4974-a79b-3a5c2322606d')
      }
      branch('${PR_BRANCH}')
    }
  }

  logRotator {
    daysToKeep defaults.daysToKeep
  }

  wrappers {
    timestamps()
    colorizeOutput 'xterm'
    parameters {
      stringParam('PR_BRANCH', '', 'PR branch to process jobs from')
      stringParam('FORK_ORG', '', 'GitHub fork organization to resolve git repo ($FORK_ORG/jenkins-jobs)')
    }
  }

  steps {
    shell '''
      #!/usr/bin/env bash

      set -eo pipefail

      make docker-build docker-test
    '''.stripIndent().trim()
  }
}
