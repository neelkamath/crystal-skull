# Developing

## Server

Replace `<GRADLE>` with `gradlew.bat` on Windows, and `./gradlew` on others.

### Development

`<GRADLE> -t assemble & <GRADLE> run`

The server will be running at `http://localhost:80`, and has automatic reload enabled (i.e., the server needn't be recompiled when the code in `src/main` has been updated). You can change the port by setting the `PORT` environment variable.

### Testing

`<GRADLE> test`

You can open `build/reports/tests/test/index.html` in your browser to view the reports.

### Production

`docker build -t crystal-skull .`

To serve at `http://localhost:80`, run `docker run --rm -p 80:80 crystal-skull`. You can change the port by setting the `PORT` environment variable (e.g., `docker run --rm -e PORT=8080 -p 8080:8080 crystal-skull`). The container `EXPOSE`s port `80`.

## Specification

`docs/openapi.yaml` is the [OpenAPI specification](https://swagger.io/specification/) for the HTTP API.

### Testing

`spectral lint docs/openapi.yaml`

## Documentation

### Developing

`redoc-cli serve docs/openapi.yaml -w`

Open `http://localhost:8080` in your browser. 

The documentation will automatically rebuild whenever you save a change to `docs/openapi.yaml`. Refresh the page whenever you want to view the updated documentation.

### Production

`redoc-cli bundle docs/openapi.yaml -o public/index.html --title 'Crystal Skull'`

Open `public/index.html` in your browser.

## Mocking a Server

`prism mock docs/openapi.yaml`

The mock server will be running at `http://localhost:4010`.

## Releases

- If required, bump the HTTP API version in `docs/openapi.yaml` and `build.gradle.kts`.
- If required, update the [Docker Hub repository](https://hub.docker.com/r/neelkamath/crystal-skull)'s **Overview**.
- For every commit to the `master` branch in which the tests have passed, the following will automatically be done.
    - If there is a new HTTP API version, a git tag and GitHub release will be created.
    - The new images will be uploaded to Docker Hub.
    - The Heroku deployment will be updated.
    - The new documentation will be hosted.