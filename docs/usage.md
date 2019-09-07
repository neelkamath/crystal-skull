# Usage

Substitute `<GRADLE>` with `gradlew.bat` on Windows and `./gradlew` on others.

## Developing the Server

### Running

- Windows:
    1. `gradlew.bat -t build`
    1. In another terminal: `gradlew.bat run`
- Other: `./gradlew -t build & ./gradlew run`

The server will be running at `localhost:8080`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code has been updated). You can change the port by setting the `PORT` environment variable.

### Testing

- Server: `<GRADLE> test`
- Spec: `spectral lint spec.oas3.json`

### Production

A Dockerfile is used to run the server in production. Follow these steps to test running the server with Docker.
1. `<GRADLE> build`
1. `docker build -t crystal-skull .`
1. Run at `http://localhost:8080` with `docker run -itp 8080:8080 --rm crystal-skull`. You can change the port by setting the `PORT` environment variable.

## Documentation

### Developing

`redoc-cli serve spec.oas3.json -wp 6969`

Open `http://127.0.0.1:6969` in your browser. The documentation will automatically rebuild whenever you save a change to `spec.oas3.json`. Refresh the page whenever you want to view the updated documentation.

We use port `6969` instead of the default `8080` because the development server usually uses that.

### Production

`redoc-cli bundle spec.oas3.json -o public/index.html --title 'Crystal Skull'`

Open `public/index.html` in your browser.

## Generating an SDK

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

## Mocking a Server

`prism mock spec.oas3.json`

The mock server will be running at the URL displayed on STDOUT.