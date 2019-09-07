# Forking the Repository

1. [Install](installation.md) the app.
1. Create a [GitLab](https://gitlab.com/users/sign_in#register-pane) account for CI/CD.
1. [Connect](https://docs.gitlab.com/ee/ci/ci_cd_for_external_repos/github_integration.html) the GitHub repo to a GitLab repo.
1. Create a [Heroku](https://signup.heroku.com) account to deploy the server.
1. Go to your [Heroku dashboard](https://dashboard.heroku.com/apps).
1. Click `New`.
1. Click `Create new app`.
1. Enter an `App name`.
1. Click `Create app`.
1. In the GitLab repo, create the following environment variables [via the UI](https://docs.gitlab.com/ee/ci/variables/#via-the-ui).
    - `HEROKU_APP`: Your Heroku app's name
    - `HEROKU_AUTH_TOKEN`: Your Heroku API key (obtained from the `API Key` section in [your profile](https://dashboard.heroku.com/account))