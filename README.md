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

You can generate a wrapper for the HTTP API using [OpenAPI Generator](https://openapi-generator.tech/) on the file `https://raw.githubusercontent.com/neelkamath/crystal-skull/master/docs/openapi.yaml`.

## [Usage](https://neelkamath.gitlab.io/crystal-skull/)

## [Contributing](docs/CONTRIBUTING.md)

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes containing fill-in-the-blank questions using Wikipedia.

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

## License

This project is under the [MIT License](LICENSE).