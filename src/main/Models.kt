package com.neelkamath.crystalskull

import com.neelkamath.kwikipedia.search
import kotlinx.coroutines.runBlocking

/** Search results for a topic. */
internal data class SearchResponse(val topics: List<Topic>)

/** A [topic] (e.g., `"Apple Inc."`) and its description. */
internal data class Topic(val topic: String, val description: String)

/**
 * Request to generate a quiz for a Wikipedia [topic] (e.g., `"Apple Inc."`).
 *
 * The quiz mustn't contain more than [max] questions. If `null`, the quiz should contain all the questions generated
 * while respecting the [configuration].
 */
internal data class QuizRequest(
    val topic: String = runBlocking { search()[0].title },
    val configuration: QuizConfiguration = QuizConfiguration(),
    val max: Int? = null
)

/**
 * Configuration for the quiz generator.
 *
 * The quiz must contain certain [types] of questions only.
 *
 * [duplicateAnswers] states whether questions with the same answer should be included. For example, one question may be
 * `Apple Computer Company was founded by _____.`, and another would be `Apple purchased NeXT for its NeXTSTEP operating
 * system and to bring _____ back.`. Although both the questions are different (and would have different options), both
 * their answers are `Steve Jobs`.
 *
 * [duplicateSentences] states whether to include multiple questions from the same sentence. Take the sentence `Apple
 * was founded by Steve Jobs in April 1976.` for example. Two questions may be generated from this, one being `Apple was
 * founded by _____ in April 1976.` (where the answer is `Steve Jobs`), and another might be `Apple was founded by Steve
 * Jobs in _____.` (where the answer is `April 1976`).
 */
internal data class QuizConfiguration(
    val types: List<NamedEntity> = listOf("date", "location", "organization", "person"),
    val duplicateAnswers: Boolean = false,
    val duplicateSentences: Boolean = false
)

/**
 * The type of entity for NLP named entity recognition.
 *
 * It must be `"date"`, `"location"`, `"money"`, `"organization"`, `"percentage"`, `"person"`, or `"time"`.
 */
internal typealias NamedEntity = String

/** A [quiz] on [topic] generated from [url]. */
internal data class QuizResponse(val topic: String, val quiz: List<QuizQuestion>, val url: String)

/** A [questionAnswer] with a specific [type] of [QuestionAnswer.options]. */
internal data class QuizQuestion(val questionAnswer: QuestionAnswer, val type: NamedEntity)

/**
 * Multiple choice [question] containing four [options] of which one is the one correct [answer].
 *
 * This is a fill-in-the-blank question, where the blank is the [answer] replaced by five underscores (`"_____"`).
 */
internal data class QuestionAnswer(val question: String, val options: Set<String>, val answer: String) {
    init {
        if ("_____" !in question) throw Error("<question> must include a blank (i.e., _____): $this")
        if (options.size != 4) throw Error("<options> must have a size of four: $this")
        if (answer !in options) throw Error("<answer> must be in <options>: $this")
    }
}