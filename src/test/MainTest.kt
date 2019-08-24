package com.neelkamath.crystalskull.test

import com.google.gson.Gson
import com.neelkamath.crystalskull.*
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
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

class QuizTest {
    /** Makes an HTTP POST [request] to the `/quiz` endpoint. */
    private fun post(request: QuizRequest): QuizResponse = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Post, "quiz") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(request))
        }.response.run { Gson().fromJson(content, QuizResponse::class.java) }
    }

    inner class TopicNameTest : StringSpec({
        "Generating a quiz for a particular topic must return a quiz for the same topic" {
            "Apple".let { post(QuizRequest(it)).topic shouldBe it }
        }
    })

    inner class TopicUrlTest : StringSpec({
        "Generating a quiz for a particular topic must return the correct source page's URL" {
            post(QuizRequest("John Mayer Trio")).url shouldBe "https://en.wikipedia.org/wiki/John_Mayer_Trio"
        }
    })

    inner class DefaultEntitiesTest : StringSpec({
        "A quiz generated without configuration must only contain the default types of questions" {
            post(QuizRequest("Apple Inc.")).quiz.map { it.type }.toSet() shouldContainExactlyInAnyOrder listOf(
                NamedEntity.date,
                NamedEntity.location,
                NamedEntity.organization,
                NamedEntity.person
            )
        }
    })

    inner class ConfiguredEntitiesTest : StringSpec({
        "A quiz generated with a configuration must only contain the requested types of questions" {
            val types = listOf(NamedEntity.date, NamedEntity.money)
            val response = post(QuizRequest("Apple Inc.", QuizConfiguration(types)))
            response.quiz.map { it.type }.toSet() shouldContainExactlyInAnyOrder types
        }
    })

    inner class ConfiguredAnswersTest : StringSpec({
        "A quiz mustn't contain duplicate answers by default" {
            post(QuizRequest("Apple Inc.")).quiz.map { it.questionAnswer.answer }.run {
                withClue("Questions with duplicate answers") { size shouldBe toSet().size }
            }
        }
    })

    inner class QuizSizeTest : StringSpec({
        "The quiz mustn't contain more questions than what was asked for" {
            3.let { post(QuizRequest("Apple Inc.", max = it)).quiz.size shouldBe it }
        }
    })

    inner class ShuffledOptionsTest : StringSpec({
        "The answers present in the options should be shuffled" {
            val questions = post(QuizRequest("Apple Inc.")).quiz.map { it.questionAnswer }
            withClue("For a large quiz, answers would be present in all the four option positions") {
                questions.map { it.options.indexOf(it.answer) }.toSet().size shouldBe 4
            }
        }
    })
}

class DuplicateAnswersTester {
    private fun test(duplicateAnswers: Boolean) {
        val questions = generateQuestions(
            listOf(
                ProcessedSentence(ProcessedContext("Bob is the CEO of KYS."), NamedEntity.person, listOf("Bob")),
                ProcessedSentence(ProcessedContext("Bob works at KYS."), NamedEntity.person, listOf("Bob"))
            ),
            QuizConfiguration(listOf(NamedEntity.person), duplicateAnswers)
        )
        questions.map { it.questionAnswer.answer }.size shouldBe if (duplicateAnswers) 2 else 1
    }

    inner class DuplicateAnswersTest : StringSpec({
        "Multiple questions with the same answer must be preserved if duplicate answers are allowed" {
            test(duplicateAnswers = true)
        }
    })

    inner class UniqueAnswersTests : StringSpec({
        "Multiple questions with the same answer must'nt be preserved if duplicate answers are'nt allowed" {
            test(duplicateAnswers = false)
        }
    })
}