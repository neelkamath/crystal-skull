# Testing the Server

Only the first run will be slow since it has to download dependencies.

Running tests causes the directory `build/reports/tests/test` to become populated with reports reporting on passing and failing tests. This directory will need to be created before running tests (e.g., by running `mkdir -p build/reports/tests/test` on OSes other than Windows). It is okay if the report states that it was generated at a different date and time than it actually was.

## One-off

Great for CI environments.

```
docker-compose  -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.test.yml up --build --abort-on-container-exit --exit-code-from test
```

## Interactive

Great for testing while developing. Any changes to the project, including restarting the development server when its code changes, is done automatically.

```
docker-compose  -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.test.yml run --service-ports test bash
```

Run `gradle test` any number of times you want.