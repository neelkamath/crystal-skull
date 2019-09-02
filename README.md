# Crystal Skull

![Crystal Skull](crystal_skull.jpg)

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name. Unlike other quiz generators, our product is unique and easy to use.

The name _Crystal Skull_ comes from the movie _[Indiana Jones and the Kingdom of the Crystal Skull](https://www.imdb.com/title/tt0367882/)_. The movie shows an alien with a crystal skull who seems to have knowledge on everything. This program, too, seems to have knowledge on everything 😉.

You can view the HTTP API docs [here](https://neelkamath.gitlab.io/crystal-skull/).

If you're forking the repo to develop the project as your own and not just to send back a PR, follow [these steps](docs/fork.md).

## [Installation](docs/installation.md)
    
## Usage

The project neither contains nor needs any platform or vendor specific code, files, tools, or commands to deploy it since it has been containerized.

Substitute `<GRADLE>` with `gradlew.bat` on Windows and `./gradlew` on others.

### Developing the Server

#### Running

- Windows:
    1. `gradlew.bat -t build`
    1. In another terminal: `gradlew.bat run`
- Other: `./gradlew -t build & ./gradlew run`

The server will be running at `localhost:8080`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code has been updated). You can change the port by setting the `PORT` environment variable.

#### Testing

- Server: `<GRADLE> test`
- Spec: `spectral lint spec.oas3.json`

#### Production

A Dockerfile is used to run the server in production. Follow these steps to test running the server with Docker.
1. `<GRADLE> build`
1. `docker build -t crystal-skull .`
1. Run at `http://localhost:8080` with `docker run -itp 8080:8080 --rm crystal-skull`. You can change the port by setting the `PORT` environment variable.

### Documentation

#### Developing

`redoc-cli serve spec.oas3.json -wp 6969`

Open `http://127.0.0.1:6969` in your browser. The documentation will automatically rebuild whenever you save a change to `spec.oas3.json`. Refresh the page whenever you want to view the updated documentation.

We use port `6969` instead of the default `8080` because the development server usually uses that.

#### Production

`redoc-cli bundle spec.oas3.json -o public/index.html --title 'Crystal Skull'`

Open `public/index.html` in your browser.

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
1. Run `openapi-generator generate -g <TARGET> -o <DIRECTORY> -i spec.oas3.json`, where `<TARGET>` is what you chose in the previous step, and `<DIRECTORY>` is the directory to output the generated SDK to. A documented and ready-to-use wrapper will now be available at `<DIRECTORY>`.

For advanced use cases, please see the [OpenAPI Generator documentation](https://openapi-generator.tech/).

### Mocking a Server

`prism mock spec.oas3.json`

The mock server will be running at the URL displayed on STDOUT.

## Contributing

The specification of the HTTP API, `spec.oas3.json`, is an OpenAPI v3 document.

CI/CD is setup to automatically update the hosted documentation and development server for all commits to the `master` branch which have passed the tests.

When updating the version of Kotlin used, update the plugin version in the [build file](build.gradle.kts), the Docker images used in the [CI/CD file](.gitlab-ci.yml), and the version stated to install in the [installation document](docs/installation.md).

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes containing fill-in-the-blank questions using Wikipedia.

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

## License

This project is under the [MIT License](LICENSE).