# Forking the Repository

1. Set up VCS releases.
    1. Go to your [GitHub settings](https://github.com/settings/tokens).
    1. Click **Generate new token**.
    1. Enter a **Note**.
    1. Select the **repo** scope.
    1. Click **Generate token**.
    1. Note down your personal access token for later.
1. Set up CI/CD.
    1. Create a [GitLab](https://gitlab.com/users/sign_in#register-pane) account.
    1. [Connect](https://docs.gitlab.com/ee/ci/ci_cd_for_external_repos/github_integration.html) the GitHub repo to a GitLab repo.
    1. In the GitLab repo, create the following environment variables [via the UI](https://docs.gitlab.com/ee/ci/variables/#via-the-ui).
    
        |Key|Value|Masked|
        |---|---|---|
        |`GITHUB_TOKEN`|The GitHub personal access token you noted down|Yes|