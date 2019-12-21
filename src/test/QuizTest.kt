package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.*
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class QuizmasterTest : StringSpec({
    fun testPersonQuiz(personQuiz: List<QuizQuestion>) {
        personQuiz.map { it.type }.forEach { it shouldBe Label.PERSON }
        personQuiz.map { it.options }.forEach { it.shouldContainAll("Bill Gates", "Steve Jobs") }
        personQuiz[0].answer shouldBe "Bill Gates"
        personQuiz[0].answerOffset shouldBe AnswerOffset(0, 10)
        personQuiz[1].answer shouldBe "Steve Jobs"
        personQuiz[1].answerOffset shouldBe AnswerOffset(15, 25)
    }

    fun testDateQuestion(dateQuestion: QuizQuestion) {
        dateQuestion.options shouldContain "1900s"
        dateQuestion.answerOffset shouldBe AnswerOffset(43, 48)
        dateQuestion.type shouldBe Label.DATE
    }

    "Questions should be created from processed sections" {
        val sentence = "Bill Gates and Steve Jobs were born in the 1900s."
        val context = ProcessedContext(sentence)
        val sentence1 = ProcessedSentence(context, Label.PERSON, names = listOf("Bill Gates", "Steve Jobs"))
        val sentence2 = ProcessedSentence(context, Label.DATE, names = listOf("1900s"))
        val quiz = Quizmaster().quiz(listOf(listOf(sentence1), listOf(sentence2)))
        quiz.flatMap { entry ->
            entry.value.map { it.question }
        }.forEach { it shouldBe sentence }
        quiz.map { it.key.context.previous }.forEach { it.shouldBeNull() }
        testPersonQuiz(quiz.getValue(sentence1))
        testDateQuestion(quiz.getValue(sentence2)[0])
    }
})

class DuplicatesRemoverTest : StringSpec({
    val set = setOf("Steve Paul Jobs", "Steve", "Steve Wozniak", "Steve Gary Wozniak", "Gil Steve", "Gil Amelio")

    fun test(set: Set<String>, sought: String, duplicates: Set<String>) =
        withClue("Sought: $sought") { removeDuplicates(set, sought) shouldBe set - duplicates }

    "Smaller duplicates should be removed when a bigger string is sought" {
        test(set, sought = "Steve Wozniak", duplicates = setOf("Steve", "Steve Gary Wozniak"))
    }

    "Bigger duplicates should be removed when a smaller string is sought" {
        test(
            set,
            sought = "Steve",
            duplicates = setOf("Steve Paul Jobs", "Steve Wozniak", "Steve Gary Wozniak", "Gil Steve")
        )
    }

    "String containing unique terms shouldn't be removed when there are no duplicates" {
        test(setOf("Neel K.", "Pasquale S.", "Lord G."), sought = "Neel K.", duplicates = setOf())
    }

    "Strings containing the same terms shouldn't be removed when there are no duplicates" {
        test(set, sought = "Gil Amelio", duplicates = setOf())
    }
})