# Contributing

CI/CD is setup to automatically update the hosted documentation and development server for all commits to the `master` branch which have passed the tests.

Create a GitHub release for every new HTTP API version.

When required, update the [Docker Hub repository](https://hub.docker.com/r/neelkamath/crystal-skull)'s **Overview**.

If you're forking the repo to develop the project as your own and not just to send back a PR, follow [these steps](fork.md).

## Installation

1. If you are developing the server, install a [Java JDK or JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 8 or higher. 
1. If you are testing the Dockerfile or running the app in production, install [Docker v19](https://hub.docker.com/search/?type=edition&offering=community).
1. If you are generating an SDK, generating documentation, testing the spec, or mocking the server, install [node.js](https://nodejs.org/en/download/).
1. If you are generating an SDK, run `npm i -g @openapitools/openapi-generator-cli`.
1. If you are generating documentation, run `npm i -g redoc-cli`.
1. If you are testing the spec, run `npm i -g @stoplight/spectral`.
1. If you are mocking a server, run `npm i -g @stoplight/prism-cli`.
1. Clone the repository using one of the following methods.
    - SSH: `git clone git@github.com:neelkamath/crystal-skull.git`
    - HTTPS: `git clone https://github.com/neelkamath/crystal-skull.git`

## [Developing](developing.md)