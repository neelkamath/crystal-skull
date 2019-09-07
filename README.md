# Crystal Skull

![Crystal Skull](crystal_skull.jpg)

For developers who want to build an innovative quiz app, Crystal Skull is a server that generates complete quizzes using just a topic name. Unlike other quiz generators, our product is unique and easy to use.

The name _Crystal Skull_ comes from the movie _[Indiana Jones and the Kingdom of the Crystal Skull](https://www.imdb.com/title/tt0367882/)_. The movie shows an alien with a crystal skull who seems to have knowledge on everything. This program, too, seems to have knowledge on everything 😉.

You can view the HTTP API docs [here](https://neelkamath.gitlab.io/crystal-skull/).

If you're forking the repo to develop the project as your own and not just to send back a PR, follow [these steps](docs/fork.md).

## [Installation](docs/installation.md)
    
## [Usage](docs/usage.md)

## Contributing

The specification of the HTTP API, `spec.oas3.json`, is an OpenAPI v3 document.

CI/CD is setup to automatically update the hosted documentation and development server for all commits to the `master` branch which have passed the tests.

When updating the version of Kotlin used, update the plugin version in the [build file](build.gradle.kts), the Docker images used in the [CI/CD file](.gitlab-ci.yml), and the version stated to install in the [installation document](docs/installation.md).

## Credits

The template for the README's description came from [Joel on Software](https://www.joelonsoftware.com/2002/05/09/product-vision/).

Although he didn't convey how or what to do in any way, [Sundararaman](https://github.com/vsundar17697) showed me the idea of generating quizzes containing fill-in-the-blank questions using Wikipedia.

The quiz generator uses text from [Wikipedia](https://en.wikipedia.org/) which is licensed under [CC-BY-SA](http://creativecommons.org/licenses/by-sa/3.0/).

## License

This project is under the [MIT License](LICENSE).