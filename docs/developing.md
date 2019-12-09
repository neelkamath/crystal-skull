# Developing

## Server

### Development

```
docker-compose up --build
```

The server will be running at `http://localhost:80`, and has automatic reload enabled (i.e., the command needn't be run again when the code in `src/main` has been updated).

### [Testing](server_testing.md)

### Production

```
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build
```

The server will be running on `http://localhost:80`.

## Specification

`docs/openapi.yaml` is the [OpenAPI specification](https://swagger.io/specification/) for the HTTP API. 

Use the [`components`](https://swagger.io/specification/#componentsObject) object as much as possible (except for simple `example`/`examples` objects) to aid in refactoring and for OpenAPI Generator to give readable names to the generated models. This means that you should use the `$ref` key even inside the `components` object instead of inlining models.

### Testing

```
spectral lint docs/openapi.yaml
```

## Documentation

### Developing

```
redoc-cli serve docs/openapi.yaml -w
```

The documentation will be served on `http://localhost:80`. It will automatically rebuild when `docs/openapi.yaml` is updated. Refresh the page to view the updated version.

### Production

```
redoc-cli bundle docs/openapi.yaml -o public/index.html --title 'Crystal Skull'
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