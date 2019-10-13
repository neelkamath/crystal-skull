# Crystal Skull

![Crystal Skull](crystal_skull.jpg)

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name or supplied text. Unlike other quiz generators, our product is unique and easy to use.

The name _Crystal Skull_ comes from the movie _[Indiana Jones and the Kingdom of the Crystal Skull](https://www.imdb.com/title/tt0367882/)_. The movie shows an alien with a crystal skull who seems to have knowledge on everything. This program, too, seems to have knowledge on everything 😉.

## Installation

You can try out the HTTP API using the development server `https://crystal-skull.herokuapp.com`. However, this server may be offline or serving a different API in the future. Hence, it's highly recommended to run your own instance.

### Running Your Own Instance

Install [Docker](https://hub.docker.com/search/?type=edition&offering=community).

To serve at `http://localhost:80`, run `docker run --rm -p 80:80 neelkamath/crytal-skull`. 

You can change the port by setting the `PORT` environment variable (e.g., `docker run --rm -e PORT=8080 -p 8080:8080 neelkamath/crytal-skull`).

To run a particular version, run `docker run --rm -p 80:80 neelkamath/crytal-skull:<TAG>`, where `<TAG>` is from `https://hub.docker.com/r/neelkamath/crystal-skull/tags`.

The container `EXPOSE`s port `80`.

### Generating an SDK

You can generate an API wrapper to use the HTTP API using these steps.

1. Install [node.js](https://nodejs.org/en/download/).
1. `npm i -g @openapitools/openapi-generator-cli`.
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
    Pick one of these (e.g., `javascript`).
1. Run `openapi-generator generate -g <TARGET> -o <DIRECTORY> -i https://raw.githubusercontent.com/neelkamath/crystal-skull/master/docs/openapi.yaml`, where `<TARGET>` is what you picked, and `<DIRECTORY>` is the directory to output the generated SDK to. A documented and ready-to-use wrapper will now be available at `<DIRECTORY>`.

For advanced use cases, please see the [OpenAPI Generator documentation](https://openapi-generator.tech/).

## [Usage](https://neelkamath.gitlab.io/crystal-skull/)

## [Contributing](docs/CONTRIBUTING.md)

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes containing fill-in-the-blank questions using Wikipedia.

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

## License

This project is under the [MIT License](LICENSE).