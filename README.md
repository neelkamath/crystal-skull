# Crystal Skull

![Crystal Skull](crystal_skull.jpg)

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name or supplied text. Unlike other quiz generators, our product is unique and easy to use.

The name _Crystal Skull_ comes from the movie _[Indiana Jones and the Kingdom of the Crystal Skull](https://www.imdb.com/title/tt0367882/)_. The movie shows an alien with a crystal skull who seems to have knowledge on everything. This program, too, seems to have knowledge on everything 😉.

## Installation

Install [Docker](https://hub.docker.com/search/?type=edition&offering=community).

You may optionally generate a wrapper for the HTTP API using [OpenAPI Generator](https://openapi-generator.tech/) on the file [`https://raw.githubusercontent.com/neelkamath/crystal-skull/master/docs/openapi.yaml`](https://raw.githubusercontent.com/neelkamath/crystal-skull/master/docs/openapi.yaml).

## Usage

Since the API is currently unstable, you'll have to read the [developer docs](docs/CONTRIBUTING.md) in order to set it up so that you can try it out. Once the API is stable, it'll be available on Docker Hub. You can view the HTTP API docs [here](https://neelkamath.gitlab.io/crystal-skull/).

## [Contributing](docs/CONTRIBUTING.md)

## Credits

[![Built with spaCy](https://img.shields.io/badge/built%20with-spaCy-09a3d5.svg)](https://spacy.io)

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating fill-in-the-blank questions using Wikipedia.

The [`wait-for-it.sh`](docker/wait-for-it.sh) script was taken from [vishnubob](https://github.com/vishnubob/wait-for-it).

## License

This project is under the [MIT License](LICENSE).