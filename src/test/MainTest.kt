package com.neelkamath.crystalskull.test

import com.google.gson.Gson
import com.neelkamath.crystalskull.*
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchTest {
    @Test
    fun `Searching for "appl" must include the fruit and tech company as results`() =
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "search?query=appl").response.run {
                val results = Gson().fromJson(content, SearchResponse::class.java)
                assertTrue(
                    results.topics.map { it.topic }.containsAll(listOf("Apple", "Apple Inc.")),
                    """
                        Searching for "appl" must include the fruit and tech company as results
                        The topics were instead: $results
                    """.trimIndent()
                )
            }
        }
}

class QuizTest {
    /** Makes an HTTP POST [request] to the `/quiz` endpoint. */
    private fun post(request: QuizRequest): QuizResponse = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Post, "quiz") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(request))
        }.response.run { Gson().fromJson(content, QuizResponse::class.java) }
    }

    @Test
    fun `Generating a quiz for a particular topic must return a quiz for the same topic`() =
        "Apple".let { topic -> assertEquals(topic, post(QuizRequest(topic)).topic) }

    @Test
    fun `Generating a quiz for a particular topic must return the correct source page's URL`() =
        assertEquals("https://en.wikipedia.org/wiki/John_Mayer_Trio", post(QuizRequest("John Mayer Trio")).url)

    @Test
    fun `A quiz generated without configuration must only contain the default types of questions`() {
        val types = post(QuizRequest("Apple Inc.")).quiz.map { it.type }.toSet()
        assertTrue(listOf("date", "location", "organization", "person").containsAll(types), "Actual types: $types")
    }

    @Test
    fun `A quiz generated with a configuration must only contain certain types of questions`() {
        val requestedTypes = listOf("date", "money")
        val types = post(QuizRequest("Apple Inc.", QuizConfiguration(requestedTypes))).quiz.map { it.type }.toSet()
        assertTrue(requestedTypes.containsAll(types), "Actual types: $types")
    }

    @Test
    fun `A quiz mustn't contain duplicate answers by default`() {
        val answers = post(QuizRequest("Apple Inc.")).quiz.map { it.questionAnswer.answer }
        assertEquals(answers.toSet().size, answers.size, "Questions with duplicate answers")
    }

    @Test
    fun `The quiz mustn't contain more questions than what was asked for`() =
        3.let { max -> assertEquals(max, post(QuizRequest("Apple Inc.", max = max)).quiz.size) }

    @Test
    fun `A quiz's questions on the same topic must be shuffled`() {
        val topic = "Apple Inc."
        val quiz1 = post(QuizRequest(topic))
        val quiz2 = post(QuizRequest(topic))
        val getFirstWord = { response: QuizResponse -> response.quiz[0].questionAnswer.question.split(" ")[0] }
        assertTrue(getFirstWord(quiz1) != getFirstWord(quiz2) || getFirstWord(quiz1) != getFirstWord(quiz2))
    }

    @Test
    fun `The answers present in the options should be shuffled`() = assertEquals(
        4,
        post(QuizRequest("Apple Inc."))
            .quiz
            .map { it.questionAnswer }
            .map { it.options.indexOf(it.answer) }
            .toSet()
            .size,
        "For a large quiz, answers would be present in all the four option positions"
    )
}

class QuestionGeneratorTest {
    private fun test(duplicateAnswers: Boolean) = assertEquals(
        if (duplicateAnswers) 2 else 1,
        generateQuestions(
            listOf(
                ProcessedSentence("Bob is the CEO of KYS.", "person", listOf("Bob")),
                ProcessedSentence("Bob works at KYS.", "person", listOf("Bob"))
            ),
            QuizConfiguration(listOf("person"), duplicateAnswers)
        ).map { it.questionAnswer.answer }.size
    )

    @Test
    fun `Multiple questions with the same answer must be preserved if duplicate answers are allowed`() =
        test(duplicateAnswers = true)

    @Test
    fun `Multiple questions with the same answer must'nt be preserved if duplicate answers are'nt allowed`() =
        test(duplicateAnswers = false)
}