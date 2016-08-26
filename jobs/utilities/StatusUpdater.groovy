package utilities

class StatusUpdater {
  static updateStatus(Map args) {
    script = new File("${WORKSPACE}/bash/scripts/update_commit_status.sh").text
    script += """
      update-commit-status \
        ${args.commitStatus} \
        ${args.repoName} \
        ${args.commitSHA} \
        ${args.targetURL} \
        ${args.description}
    """.stripIndent().trim()
  }
}
