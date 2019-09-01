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
 * Questions on dates may contain options lacking years (e.g., `June 7` instead of `June 7, 2000`). Since such questions
 * may be unnecessarily difficult and/or irrelevant to a person's study, you can choose whether to [allowSansYears].
 */
internal data class QuizConfiguration(
    val types: List<NamedEntity> =
        listOf(NamedEntity.date, NamedEntity.location, NamedEntity.organization, NamedEntity.person),
    val allowSansYears: Boolean = false,
    val duplicates: Duplicates = Duplicates()
)

/**
 * Duplicates to be allowed.
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
internal data class Duplicates(val duplicateAnswers: Boolean = false, val duplicateSentences: Boolean = false)

/** The type of entity for NLP named entity recognition. */
internal enum class NamedEntity { date, location, money, organization, percentage, person, time }

/** A [quiz] on [topic] generated from a [url]. */
internal data class QuizResponse(val topic: String, val quiz: List<QuizQuestion>, val url: String)

/** A [questionAnswer] with a specific [type] of [QuestionAnswer.options]. */
internal data class QuizQuestion(val questionAnswer: QuestionAnswer, val type: NamedEntity)

/** Multiple choice question containing four [options] of which one is the one correct [answer]. */
internal data class QuestionAnswer(val questionContext: QuestionContext, val options: Set<String>, val answer: String) {
    init {
        if (options.size != 4) throw Error("<options> must have a size of four: $this")
        if (answer !in options) throw Error("<answer> must be in <options>: $this")
    }
}

/**
 * A fill-in-the-blank [question] with a [context].
 *
 * [question] should contain five underscores (i.e., `"_____"`) for where the answer is to be filled in.
 *
 * Occasionally, the [question] is insufficient for the user to understand what is being asked. For example, the
 * question `"This slump caused the company to collapse in _____."` is impossible to understand. For this reason, the
 * sentence present prior to the [question] will be included as a [context]. If the [question] is the first sentence in
 * its passage, then this field will be `null` (a [context] isn't required in such cases anyway).
 */
internal data class QuestionContext(val question: String, val context: String?) {
    init {
        if ("_____" !in question) throw Error("<question> must include a blank (i.e., _____): $this")
    }
}