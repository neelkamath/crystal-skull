# Usage

## Server

### Running

`docker-compose up`

The server will be running at `http://localhost:80`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code in `src/main/` has been updated).

### Testing

- Spec: `spectral lint openapi.yaml`
- Server: 
    1. `docker run --rm -it --mount type=bind,src=$PWD,dst=/home openjdk:11 bash`
    1. Run `cd home` in the container's shell.

    Since a Gradle daemon is slow to start in a container, we allow it to be reused whenever tests need to be rerun by running the container interactively. Run `./gradlew test` in the container's shell to run tests. You can open `build/reports/tests/test/index.html` in your browser to view the reports.

### Production

`docker build -f prod.Dockerfile -t prod .`

The Dockerfile `EXPOSE`s port `80`. So to serve at `http://localhost:80`, run `docker run --rm -p 80:80 prod`. You can change the port by setting the `PORT` environment variable (e.g., `docker run --rm -e PORT=8080 -p 8080:8080 prod`).

## Documentation

### Developing

`redoc-cli serve openapi.yaml -w`

Open `http://localhost:8080` in your browser. 

The documentation will automatically rebuild whenever you save a change to `openapi.yaml`. Refresh the page whenever you want to view the updated documentation.

### Production

`redoc-cli bundle openapi.yaml -o public/index.html --title 'Crystal Skull'`

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

Run `openapi-generator generate -g <TARGET> -o <DIRECTORY> -i openapi.yaml`, where `<TARGET>` is what you picked, and `<DIRECTORY>` is the directory to output the generated SDK to. A documented and ready-to-use wrapper will now be available at `<DIRECTORY>`.

For advanced use cases, please see the [OpenAPI Generator documentation](https://openapi-generator.tech/).

## Mocking a Server

`prism mock openapi.yaml`

The mock server will be running at `http://localhost:4010`.