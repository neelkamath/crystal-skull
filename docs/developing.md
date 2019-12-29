# Developing

## Server

### Developing

```
docker-compose -f docker/docker-compose.yml -f docker/docker-compose.override.yml --project-directory . run --rm test
```

The server will be running at `http://localhost:80` and has automatic reload enabled. A bind mount connects the project directory to the container so that you can run commands like `gradle test`. You can run `gradle dependencyUpdates` to view dependency updates. Test reports save to `build/reports/tests/test/`. `src/test` contains unit tests. `src/intTest` contains integration tests.

### Production

```
docker-compose --project-directory . -f docker/docker-compose.yml -f docker/docker-compose.prod.yml up --build
```

The server will be running on `http://localhost:80`.

### Profiling

1. Run:
    ```
    docker-compose --project-directory . -f docker/docker-compose.yml -f docker/docker-compose.prod.yml \
        run --rm -p 80:80 -p 9010:9010 quiz sh docker/profile.sh
    ```
1. Open VisualVM.
1. Click **File**.
1. Click **Add JMX Connection...**.
1. Enter `localhost:9010` in **Connection:**.
1. Click **OK**.
1. Double click **localhost:9010 (pid PID)** (where `PID` is an integer) under **Local** in the **Applications** tab.

## Specification

`docs/openapi.yaml` is the [OpenAPI specification](https://swagger.io/specification/) for the HTTP API. 

Use the [`components`](https://swagger.io/specification/#componentsObject) object as much as possible (except for simple `example`/`examples` objects) to aid in refactoring and for OpenAPI Generator to give readable names to the generated models. This means that you should use the `$ref` key even inside the `components` object instead of inlining models.

### Testing

```
npx @stoplight/spectral lint docs/openapi.yaml
```

## Documentation

### Developing

```
npx redoc-cli serve docs/openapi.yaml -w
```

The documentation will be served on `http://localhost:80`. It will automatically rebuild when `docs/openapi.yaml` is updated. Refresh the page to view the updated version.

### Production

```
npx redoc-cli bundle docs/openapi.yaml -o public/index.html --title 'Crystal Skull'
```

The documentation will be saved to `public/index.html`.

## Mocking a Server

`prism mock docs/openapi.yaml`

The mock server will be running at `http://localhost:4010`.

## Releases

1. If required, bump the HTTP API version in `docs/openapi.yaml` and `build.gradle.kts`.
1. If required, update the [Docker Hub repository](https://hub.docker.com/r/neelkamath/crystal-skull)'s **Overview**.
1. Commit to the `master` branch. For every commit in which the tests have passed, the following will automatically be done.
    - If there is a new HTTP API version, a git tag and GitHub release will be created.
    - The new images will be uploaded to Docker Hub.
    - The new documentation will be hosted.