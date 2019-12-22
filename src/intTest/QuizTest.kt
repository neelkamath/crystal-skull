package com.neelkamath.crystalskull.intTest

import com.neelkamath.crystalskull.Label
import com.neelkamath.crystalskull.QuizRequest
import com.neelkamath.crystalskull.QuizResponse
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveAtLeastSize
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

// Tests for the `/quiz` endpoint.
class QuizTest : StringSpec({
    // The topic "Apple Inc." is used often since it has an abundance of questions and question types to test on.
    val quizTopic = "Apple Inc."
    val quizRequest = QuizRequest(quizTopic)
    val quizResponse = Server.requestQuiz(quizRequest)
    val generatedQuiz = Server.quiz(quizRequest)

    "A quiz response should have a status code of 200" { quizResponse.code() shouldBe 200 }

    "Requesting a quiz for a certain topic should beckon a response for the same topic" {
        generatedQuiz.metadata!!.topic shouldBe quizTopic
    }

    "A quiz on a particular topic should contain the URL of the content's source" {
        generatedQuiz.metadata!!.url shouldBe "https://en.wikipedia.org/wiki/Apple_Inc."
    }

    "A quiz on supplied text shouldn't have topic metadata" {
        Server.quiz(QuizRequest(text = listOf("Bill Gates founded Microsoft."))).metadata.shouldBeNull()
    }

    "Requesting seven quiz questions should beckon at most seven questions in the response" {
        Server.quiz(QuizRequest("Apple Inc.", max = 7)).quiz.size shouldBeLessThanOrEqual 7
    }

    fun QuizResponse.getAnswersSansYears(): List<String> =
        quiz.filter { it.type == Label.DATE }.map { it.answer }.filter { !it.contains(Regex("""\d\d\d\d""")) }

    "Questions sans years should be absent by default" { generatedQuiz.getAnswersSansYears().shouldBeEmpty() }

    "Questions sans years should be present if requested" {
        Server.quiz(QuizRequest("Apple Inc.", allowSansYears = true)).getAnswersSansYears() shouldHaveAtLeastSize 1
    }

    "Duplicate answers should be absent by default" {
        generatedQuiz.quiz.map { it.answer }.let { it.toSet() shouldHaveSize it.size }
    }

    "Duplicate answers should be present if requested" {
        Server.quiz(QuizRequest("Apple Inc.", duplicateAnswers = true)).quiz.map { it.answer }.let {
            it.size shouldBeGreaterThan it.toSet().size
        }
    }

    "Duplicate questions should be absent by default" {
        val sentence = "Bill Gates is rich"
        Server.quiz(QuizRequest(text = listOf(sentence, sentence))).quiz.map { it.question }.let {
            it.toSet() shouldHaveSize it.size
        }
    }

    "Duplicate questions should be present if requested" {
        Server.quiz(QuizRequest("Apple Inc.", duplicateSentences = true)).quiz.map { it.question }.let {
            it.size shouldBeGreaterThan it.toSet().size
        }
    }

    fun testContext(hasContext: Boolean) {
        val sentence1 = "Bill Gates founded Microsoft."
        val sentence2 = "He was born on October 28, 1955."
        val request = QuizRequest(text = listOf("$sentence1 $sentence2"))
        val context =
            Server.quiz(request).quiz.filter { it.question == if (hasContext) sentence2 else sentence1 }[0].context
        if (hasContext) context.shouldNotBeNull() else context.shouldBeNull()
    }

    "A question on the first sentence in a passage shouldn't have a context" { testContext(hasContext = false) }

    "A question on the second sentence in a passage should have a context" { testContext(hasContext = true) }

    "Each question should have four options" {
        generatedQuiz.quiz.map { it.options }.filter { it.size != 4 }.shouldBeEmpty()
    }

    "Each question's answer should match the substring indicated by the answer offset" {
        generatedQuiz.quiz
            .map { it.answer }
            .zip(generatedQuiz.quiz.map { it.question.substring(it.answerOffset.start until it.answerOffset.end) })
            .filter { it.first != it.second }
            .shouldBeEmpty()
    }

    "Related topic names should allow for quizzes to be generated off of them" {
        generatedQuiz.related!!
            .associateWith { Server.quiz(QuizRequest(it)) }
            .mapValues { it.value.metadata!!.topic }
            .filter { it.key != it.value }
            .let {
                withClue("Quizzes generated for unrelated topics: $it") { it.size shouldBe 0 }
            }
    }

    "Related topics should be sorted in order of relevance" {
        withClue("The greater the number of occurrences of a topic, the greater its relevance") {
            val request = QuizRequest(text = listOf("Bill Gates founded Microsoft.", "Bill Gates was born in 1955."))
            Server.quiz(request).related shouldBe listOf("Bill Gates", "Microsoft")
        }
    }

    fun testOptions(response: QuizResponse, label: Label, pattern: Regex) = response
        .quiz
        .filter { it.type == label }
        .flatMap { it.options }
        .filterNot { it.matches(pattern) }
        .shouldBeEmpty()

    "When there are a shortage of dates in the supplied text, realistic options should be generated" {
        val request = QuizRequest(text = listOf("He was born on October 28, 1955."))
        val month = Regex("(January|February|March|April|May|June|July|August|September|October|November|December)")
        testOptions(Server.quiz(request), Label.DATE, Regex("""$month ([1-9]|[12][0-9]|3[01]), \d\d\d\d"""))
    }

    "When there are a shortage of times in the supplied text, realistic options should be generated" {
        testOptions(
            Server.quiz(QuizRequest(text = listOf("It's 02:39 PM now."))),
            Label.TIME,
            Regex("""\d\d:\d\d [AP]M""")
        )
    }

    "When there are a shortage of percentages in the supplied text, realistic options should be generated" {
        val request = QuizRequest(text = listOf("I got 90% on my test."))
        testOptions(Server.quiz(request), Label.PERCENT, Regex("""(100|\d{1,2}(\.\d{1,2})?)%"""))
    }

    "Options should only be taken from the section the question is from unless more are required" {
        val name1 = "Tesla"
        val name2 = "Einstein"
        val name3 = "Edison"
        val sentence1 = "$name1 is first."
        val sentence2 = "$name2 is second."
        val sentence3 = "$name3 is third."
        val section = "$sentence1 $sentence2 $sentence3"
        val options = Server.quiz(
            QuizRequest(
                text = listOf(
                    "Bill Gates was born on October 28, 1955.",
                    section,
                    "Galileo Galilei is fourth. Narendra Modi is fifth. Steve Jobs is sixth."
                )
            )
        ).quiz.filter { it.question == sentence1 }[0].options
        withClue("Options: $options") { options shouldContainAll listOf(name1, name2, name3) }
    }
})
