# Crystal Skull

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name. Unlike other quiz generators, our product is unique and easy to use.

If you're forking the repo to develop the project as your own and not just to send back a PR, follow [these steps](docs/fork.md).

## [Installation](docs/installation.md)
    
## Usage

Substitute `<GRADLE>` with `gradlew.bat` on Windows and `./gradlew` on others.

### [Documentation](https://neelkamath.gitlab.io/crystal-skull/)

### Developing the Server

#### Running

`<GRADLE> -t build & <GRADLE> run`

The server will be running at `localhost:8080`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code has been updated). You can change the port by setting the `PORT` environment variable.

#### Testing

- Server: `<GRADLE> test`
- Spec: `spectral lint spec.oas3.json`

### Running the Server

1. `<GRADLE> build`
1. `docker build -t crystal-skull .`
1. Run at `http://localhost:8080` with `docker run -itp 8080:8080 --rm crystal-skull`. You can change the port by setting the `PORT` environment variable.

### Deploying

The project neither contains nor needs any platform or vendor specific code, files, tools, or commands since it has been containerized.

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

For advanced use cases, please see [OpenAPI Generator](https://openapi-generator.tech/)'s documentation.

### Mocking a Server

`prism mock spec.oas3.json`

The mock server will be running at the URL displayed on STDOUT.

## Contributing

The specification of the HTTP API is an OpenAPI v3 document located [here](spec.oas3.json).

CI/CD is setup to automatically update the hosted documentation and development server for all commits to the `master` branch which have passed the tests.

When updating the version of Kotlin used, update the plugin version in the [build file](build.gradle.kts) as well as the Docker images used in the [CI/CD file](.gitlab-ci.yml).

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

[Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes using Wikipedia.

## License

This project is under the [MIT License](LICENSE).