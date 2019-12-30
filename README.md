# Crystal Skull

![Crystal Skull](crystal_skull.jpg)

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name or supplied text. Unlike other quiz generators, our product is unique and easy to use.

The name _Crystal Skull_ comes from the movie _[Indiana Jones and the Kingdom of the Crystal Skull](https://www.imdb.com/title/tt0367882/)_. The movie shows an alien with a crystal skull who seems to have knowledge on everything. This program, too, seems to have knowledge on everything 😉.

## Installation

1. Install [Docker](https://hub.docker.com/search/?type=edition&offering=community).
1. Clone the repository using one of the following methods.
    - SSH: `git clone git@github.com:neelkamath/crystal-skull.git`
    - HTTPS: `git clone https://github.com/neelkamath/crystal-skull.git`
1. Optionally, generate a wrapper for the HTTP API using [OpenAPI Generator](https://openapi-generator.tech/) on the file [`https://raw.githubusercontent.com/neelkamath/crystal-skull/master/docs/openapi.yaml`](https://raw.githubusercontent.com/neelkamath/crystal-skull/master/docs/openapi.yaml).

## Usage

```
docker-compose --project-directory . -f docker/docker-compose.yml -f docker/docker-compose.prod.yml up --build
```
   
The server will be running on `http://localhost:80`.

## [Contributing](docs/CONTRIBUTING.md)

## Credits

[![Built with spaCy](https://img.shields.io/badge/built%20with-spaCy-09a3d5.svg)](https://spacy.io)

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating fill-in-the-blank questions using Wikipedia.

The [`wait-for-it.sh`](docker/wait-for-it.sh) script was taken from [vishnubob](https://github.com/vishnubob/wait-for-it).

## License

This project is under the [MIT License](LICENSE).