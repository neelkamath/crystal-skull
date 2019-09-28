package com.neelkamath.crystalskull.test

import com.google.gson.Gson
import com.neelkamath.crystalskull.*
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication

class SearchTest : StringSpec({
    """Searching for "appl" must include the fruit and tech company as results""" {
        withTestApplication(Application::main) {
            val response = handleRequest(HttpMethod.Get, "search?query=appl").response
            val search = Gson().fromJson(response.content, SearchResponse::class.java).topics.map { it.topic }
            withClue(search.toString()) { search shouldContainAll listOf("Apple", "Apple Inc.") }
        }
    }
})

class MetadataTest : StringSpec({
    "Generating a quiz for a particular topic must return a quiz for the same topic" {
        "Apple".let { post(QuizRequest(it)).metadata!!.topic shouldBe it }
    }

    "Generating a quiz for a particular topic must return the correct source page's URL" {
        post(QuizRequest("John Mayer Trio")).metadata!!.url shouldBe "https://en.wikipedia.org/wiki/John_Mayer_Trio"
    }
})

class ConfigurationTest : StringSpec() {
    /** Filters options from questions in the [response] having type [NamedEntity.date]. */
    private fun getDateOptions(response: QuizResponse): List<String> =
        response.quiz.filter { it.type == NamedEntity.date }.flatMap { it.questionAnswer.options }

    init {
        "A quiz generated without configuration must only contain the default types of questions" {
            val types = listOf(NamedEntity.date, NamedEntity.location, NamedEntity.organization, NamedEntity.person)
            post(QuizRequest("Apple Inc.")).quiz.map { it.type }.toSet() shouldContainExactlyInAnyOrder types
        }

        "Questions on dates mustn't have options sans years by default" {
            for (option in getDateOptions(post(QuizRequest("Apple Inc.")))) {
                withClue(option) { containsYear(option).shouldBeTrue() }
            }
        }

        "Questions on dates may have options sans years if configured as such" {
            val options = getDateOptions(post(QuizRequest("Apple Inc.", QuizConfiguration(allowSansYears = true))))
            withClue("A large quiz will have at least one option sans a year: $options") {
                options.map { containsYear(it) } shouldContain false
            }
        }

        "A quiz generated with a configuration must only contain the requested types of questions" {
            val types = listOf(NamedEntity.date, NamedEntity.money)
            val response = post(QuizRequest("Apple Inc.", QuizConfiguration(types)))
            response.quiz.map { it.type }.toSet() shouldContainExactlyInAnyOrder types
        }

        "A quiz mustn't contain duplicate answers by default" {
            post(QuizRequest("Apple Inc.")).quiz.map { it.questionAnswer.answer }.run {
                withClue("Questions with duplicate answers") { size shouldBe toSet().size }
            }
        }

        "The generator mustn't crash while generating questions for every type" {
            post(QuizRequest("Apple Inc.", QuizConfiguration(types = NamedEntity.values().toList())))
        }
    }
}

class RelatedQuizTest : StringSpec({
    "Topics related to to the one the quiz was generated on should allow for quizzes to be generated for them" {
        post(QuizRequest("Apple Inc.")).related!!
            .associateWith { post(QuizRequest(it)).metadata!!.topic }
            .map { it.value shouldBe it.key }
    }
})

class SizeTest : StringSpec({
    "The quiz mustn't contain more questions than what was asked for" {
        3.let { post(QuizRequest("Apple Inc.", max = it)).quiz.size shouldBe it }
    }
})

class ShuffledOptionsTest : StringSpec({
    "The answers present in the options should be shuffled" {
        val questions = post(QuizRequest("Apple Inc.")).quiz.map { it.questionAnswer }
        withClue("For a large quiz, answers would be present in all the four option positions") {
            questions.map { it.options.indexOf(it.answer) }.toSet().size shouldBe 4
        }
    }
})

class TextSupplierTest : StringSpec({
    "Quizzes must generate using the supplied text" {
        val text = listOf("Bob attended Harvard Business School.", "Bob married Nancy Drew in August 1976.")
        val quiz = post(QuizRequest(text = text)).quiz
        withClue("Request: $text\nResponse: $quiz") { quiz shouldHaveSize 2 }
    }
})

/** Makes an HTTP POST [request] to the `/quiz` endpoint. */
private fun post(request: QuizRequest): QuizResponse = withTestApplication(Application::main) {
    handleRequest(HttpMethod.Post, "quiz") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Gson().toJson(request))
    }.response.run { Gson().fromJson(content, QuizResponse::class.java) }
}

class QuestionGeneratorTest : StringSpec() {
    init {
        val test = { duplicateAnswers: Boolean ->
            val answers = generateQuestions(
                listOf(
                    ProcessedSentence(ProcessedContext("Bob is the CEO of KYS."), NamedEntity.person, listOf("Bob")),
                    ProcessedSentence(ProcessedContext("Bob works at KYS."), NamedEntity.person, listOf("Bob"))
                ),
                QuizConfiguration(listOf(NamedEntity.person), duplicates = Duplicates(duplicateAnswers))
            ).map { it.questionAnswer.answer }
            withClue(answers.toString()) { answers shouldHaveSize if (duplicateAnswers) 2 else 1 }
        }

        "Multiple questions with the same answer must be preserved if duplicate answers are allowed" { test(true) }

        "Multiple questions with the same answer must'nt be preserved if duplicate answers are'nt allowed" {
            test(false)
        }
    }
}