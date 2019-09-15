# Usage

## Server

### Running

`docker-compose up`

The server will be running at `http://localhost:8080`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code in `src/main/` has been updated).

### Testing

- Spec: `spectral lint spec.oas3.json`
- Validate Docker Compose file: `docker-compose config`
- Server: 
    1. `docker run --rm -it --mount type=bind,src=$PWD,dst=/home/gradle openjdk:11 bash`
    1. Run `cd home` in the container's shell.
    
    Since a Gradle daemon is slow to start in a container, we allow it to be reused whenever tests need to be rerun by running the container interactively. Run `./gradlew test` in the container's shell to run tests. You can open `build/reports/tests/test/index.html` in your browser to view the reports.

### Production

1. `docker build -f Dockerfile-prod -t prod .`
1. The Dockerfile `EXPOSE`s port `8080`. So to serve at `http://localhost:8080`, run `docker run --rm -p 8080:8080 prod`

## Documentation

### Developing

`redoc-cli serve spec.oas3.json -wp 6969`

We use port `6969` since `8080` is usually used by the application server.

Open `http://localhost:8080` in your browser. 

The documentation will automatically rebuild whenever you save a change to `spec.oas3.json`. Refresh the page whenever you want to view the updated documentation.

### Production

`redoc-cli bundle spec.oas3.json -o public/index.html --title 'Crystal Skull'`

Open `public/index.html` in your browser.

## Generating an SDK

Run `openapi-generator list`.

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
Pick one of these (e.g., `javascript`).

Run `openapi-generator generate -g <TARGET> -o <DIRECTORY> -i spec.oas3.json`, where `<TARGET>` is what you picked, and `<DIRECTORY>` is the directory to output the generated SDK to. A documented and ready-to-use wrapper will now be available at `<DIRECTORY>`.

For advanced use cases, please see the [OpenAPI Generator documentation](https://openapi-generator.tech/).

## Mocking a Server

`prism mock spec.oas3.json`

The mock server will be running at `http://localhost:4010`.