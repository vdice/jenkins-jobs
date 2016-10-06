def workspace = new File(".").getAbsolutePath()
if (!new File("${workspace}/common/var.groovy").canRead()) { workspace = "${WORKSPACE}"}
evaluate(new File("${workspace}/common/var.groovy"))

name = 'workflow-test-upgrade'

job(name) {
  description "Job to test Workflow chart upgrades -- using helm-classic"

  multiscm {
   git {
     remote {
         github('deis/e2e-runner')
         credentials('597819a0-b0b9-4974-a79b-3a5c2322606d')
     }
     extensions {
         relativeTargetDirectory('e2e-runner')
     }
     branch('master')
   }
   git {
     remote {
         github('vdice/deis-workflow-gke')
         credentials('597819a0-b0b9-4974-a79b-3a5c2322606d')
     }
     extensions {
         relativeTargetDirectory('deis-workflow-gke')
     }
     branch('master')
   }
 }

  logRotator {
    daysToKeep defaults.daysToKeep
  }

  publishers {
    slackNotify(channel: defaults.slack.channel, statuses: ['SUCCESS', 'FAILURE'])
  }

  parameters {
    stringParam('PREVIOUS_WORKFLOW_RELEASE', '', 'Previous Workflow release value (default: auto-detected from installed chart)')
    stringParam('DESIRED_WORKFLOW_RELEASE', '', 'Desired Workflow release value (default: auto-detected from charts directory)')
  }

  wrappers {
    timestamps()
    colorizeOutput 'xterm'
    credentialsBinding {
      string("AUTH_TOKEN", "a62d7fe9-5b74-47e3-9aa5-2458ba32da52")
    }
  }

  steps {
    shell """
      #!/usr/bin/env bash

      set -eo pipefail

      # TODO: do we really want to lease a cluster or just be given existing cluster name/creds?
      source e2e-runner/scripts/lease.sh
      export CLAIMER_URL="\${CLAIMER_URL:-k8s-claimer.champagne.deis.com}"
      export CLUSTER_DURATION="\${CLUSTER_DURATION:-1800}"
      export LEASE_RETRIES="\${LEASE_RETRIES:-5}"

      lease

      # TODO: set max timeout to wait for workflow to come up -- 10 mins?
      # TODO: sed/perl the wfm-api version urls to `-staging` variants (or enable this in deis-workflow-gke script)

      # Install PREVIOUS_WORKFLOW_RELEASE chart
      DESIRED_WORKFLOW_RELEASE="\${PREVIOUS_WORKFLOW_RELEASE}" \
        ./deis-workflow-gke/install_workflow_2_gke.sh install

      # Installed Workflow should be up and running
      # WORKFLOW_CHART exported as installed chart name

      # TODO: download PREVIOUS_WORKFLOW_RELEASE cli binary
      # TODO: create state on Workflow cluster (deis create app, deis pull deis/example-go, etc.)

      # Upgrade to DESIRED_WORKFLOW_RELEASE
      ./deis-workflow-gke/install_workflow_2_gke.sh upgrade

      # TODO: download DESIRED_WORKFLOW_RELEASE cli binary
      # TODO: verify state created pre-upgrade

      delete-lease
    """.stripIndent().trim()
  }
}
