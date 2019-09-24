# Crystal Skull

![Crystal Skull](crystal_skull.jpg)

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name. Unlike other quiz generators, our product is unique and easy to use.

The name _Crystal Skull_ comes from the movie _[Indiana Jones and the Kingdom of the Crystal Skull](https://www.imdb.com/title/tt0367882/)_. The movie shows an alien with a crystal skull who seems to have knowledge on everything. This program, too, seems to have knowledge on everything 😉.

## [Usage](https://neelkamath.gitlab.io/crystal-skull/)

## Contributing

`openapi.yaml` is the specification for the HTTP API.

CI/CD is setup to automatically update the hosted documentation and development server for all commits to the `master` branch which have passed the tests.

Create a GitHub release for every new HTTP API version.

When required, update the [Docker Hub repository](https://hub.docker.com/r/neelkamath/crystal-skull)'s **Overview**.

If you're forking the repo to develop the project as your own and not just to send back a PR, follow [these steps](docs/fork.md).

### Installation

1. If you are developing the server, install [Java SE Development Kit 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html). 
1. If you are testing the Dockerfile or running the app in production, install [Docker v19](https://hub.docker.com/search/?type=edition&offering=community).
1. If you are generating an SDK, generating documentation, testing the spec, or mocking the server, install [node.js](https://nodejs.org/en/download/).
1. If you are generating an SDK, run `npm i -g @openapitools/openapi-generator-cli`.
1. If you are generating documentation, run `npm i -g redoc-cli`.
1. If you are testing the spec, run `npm i -g @stoplight/spectral`.
1. If you are mocking a server, run `npm i -g @stoplight/prism-cli`.
1. Clone the repository using one of the following methods.
    - SSH: `git clone git@github.com:neelkamath/crystal-skull.git`
    - HTTPS: `git clone https://github.com/neelkamath/crystal-skull.git`

### [Developing](docs/developing.md)

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes containing fill-in-the-blank questions using Wikipedia.

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

## License

This project is under the [MIT License](LICENSE).