# Crystal Skull

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name. Unlike other quiz generators, our product is unique and easy to use.

There is a development server at `https://crystall-skull.herokuapp.com/` which may be offline or serving a different API in the future. You should deploy your own server for production.

## Installation

1. If you are running or testing the server, install a version of Kotlin not less than 1.3, and less than 2 from [here](https://kotlinlang.org/docs/tutorials/command-line.html).
1. If you are generating an SDK or mocking a server, install [node.js](https://nodejs.org/en/download/).
1. If you are generating an SDK, run `npm i -g @openapitools/openapi-generator-cli`.
1. If you are mocking a server, run `npm i -g @stoplight/prism-cli`.
1. Clone the repository using one of the following methods:
    - SSH: `git clone git@github.com:neelkamath/crystal-skull.git`
    - HTTPS: `git clone https://github.com/neelkamath/crystal-skull.git`
    
## Usage

### [Documentation](https://neelkamath.gitlab.io/crystal-skull/)

### Development

`<GRADLE> -t build & ./gradlew run`, where `<GRADLE>` is `gradlew.bat` on Windows and `./gradlew` on others

The server will be running at `localhost:8080`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code has been updated).

### Production

Run the server on `localhost:8080`: `<GRADLE> run`, where `<GRADLE>` is `gradlew.bat` on Windows and `./gradlew` on others

### Generating an SDK

1. Run `openapi-generator list`.

    This will output something like:
    ```
    CLIENT generators:
        - ada
        - android
        ...
        - javascript
        ...
    SERVER generators:
        - ada-server
        - aspnetcore
        ...
    ```
   Pick one of these.
1. Run `openapi-generator generate -g <TARGET> -o <DIRECTORY> -i spec.oas3.json`, where `<TARGET>` is what you chose in the previous step, and `<DIRECTORY>` is the directory to output the generated SDK to.

For advanced use cases, please see [OpenAPI Generator](https://openapi-generator.tech/)'s documentation.

### Mocking a Server

`prism mock spec.oas3.json`

The mock server will be running at the URL displayed on STDOUT.

## Contributing

Run the tests with `<GRADLE> test`, where `<GRADLE>` is `gradlew.bat` on Windows and `./gradlew` on others.

The specification of the HTTP API is an OpenAPI v3 document located [here](spec.oas3.json).

Committing to the `master` branch will automatically update the hosted documentation with the newly generated one.

If you're forking the repo to develop the project as your own and not just to send back a PR, you'll have to setup the CI/CD pipeline. Create a [GitLab](https://gitlab.com/users/sign_in#register-pane) account, and [connect](https://docs.gitlab.com/ee/ci/ci_cd_for_external_repos/github_integration.html) the GitHub repo to a GitLab repo.

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

[Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes using Wikipedia.

## License

This project is under the [MIT License](LICENSE).