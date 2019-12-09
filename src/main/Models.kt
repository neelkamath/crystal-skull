package com.neelkamath.crystalskull

/** Search results for a topic. */
data class SearchResponse(val topics: List<Topic>)

/** A [topic] (e.g., `"Apple Inc."`) and its description. */
data class Topic(val topic: String, val description: String)

/**
 * Request to generate a quiz either on a Wikipedia [topic] or the supplied [text].
 *
 * If supplying [text] on a topic such as Bill Gates, each item in the [List] should be on a subtopic such as Early
 * Life or Career.
 *
 * The quiz mustn't contain more than [max] questions. If `null`, the quiz should contain all the possible questions.
 *
 * Questions on dates may contain options lacking years (e.g., `June 7` instead of `June 7, 2000`). Since such questions
 * may be unnecessarily difficult and/or irrelevant to a person's study, you can choose whether to [allowSansYears].
 *
 * [duplicateAnswers] states whether questions with the same answer should be included. For example, one question may be
 * "Apple Computer Company was founded by Steve Jobs.", and another would be "Apple purchased NeXT for its NeXTSTEP
 * operating system and to bring Steve Jobs back.". Although both the questions are different (and would have different
 * options), both their answers are "Steve Jobs".
 *
 * [duplicateSentences] states whether to include multiple questions from the same sentence. Take the sentence "Apple
 * was founded by Steve Jobs in April 1976." for example. Two questions may be generated from this, one being "Apple was
 * founded by Steve Jobs in April 1976." (where the answer is "Steve Jobs"), and another might be "Apple was founded by
 * Steve Jobs in April 1976." (where the answer is "April 1976").
 */
data class QuizRequest(
    val topic: String? = null,
    val text: List<String>? = null,
    val max: Int? = null,
    val allowSansYears: Boolean = false,
    val duplicateAnswers: Boolean = false,
    val duplicateSentences: Boolean = false
) {
    init {
        if (topic != null && text != null) throw Error("Both <topic> and <text> cannot be non-null")
        if (max != null && max < 0) throw Error("<max> cannot be negative")
    }
}

/** The type of entity for NLP named entity recognition. */
enum class Label {
    /** People, including fictional. */
    PERSON,
    /** Nationalities or religious or political groups. */
    NORP,
    /** Buildings, airports, highways, bridges, etc. */
    FAC,
    /** Companies, agencies, institutions, etc. */
    ORG,
    /** Countries, cities, states. */
    GPE,
    /** Non-GPE locations, mountain ranges, bodies of water. */
    LOC,
    /** Objects, vehicles, foods, etc. (Not services.) */
    PRODUCT,
    /** Named hurricanes, battles, wars, sports events, etc. */
    EVENT,
    /** Titles of books, songs, etc. */
    WORK_OF_ART,
    /** Named documents made into laws. */
    LAW,
    /** Any named language. */
    LANGUAGE,
    /** Absolute or relative dates or periods. */
    DATE,
    /** Times smaller than a day. */
    TIME,
    PERCENT,
    /** Monetary values, including unit. */
    MONEY,
    /** Measurements, as of weight or distance. */
    QUANTITY,
    /** “first”, “second”, etc. */
    ORDINAL,
    /** Numerals that do not fall under another type. */
    CARDINAL
}

/**
 * The topics [related] to the [quiz] can also have quizzes generated for them.
 *
 * [metadata] will not be `null` only if the quiz was generated using a topic name.
 */
data class QuizResponse(
    val quiz: List<QuizQuestion>,
    val metadata: QuizMetadata? = null,
    val related: List<String>? = null
)

/** The [url] of the [topic] a quiz was generated on. */
data class QuizMetadata(val topic: String, val url: String)

/**
 * Multiple choice fill-in-the-blank [question] of a particular [type] having four [options].
 *
 * Occasionally, the [question] is insufficient for the user to understand what is being asked. For example, the
 * [question] `"This slump caused the company to collapse in 1976."` is impossible to understand. For this reason, the
 * sentence present prior to the [question] will be included as a [context]. If the [question] is the first sentence in
 * its passage, then this field will be `null` (a [context] isn't required in such cases anyway).
 *
 * You can also get the [answer] from the [answerOffset] in the [question]. For example, if the [question] is `"Bob is
 * good."` and the [answer] is `"Bob"`, then the  [AnswerOffset.start] and [AnswerOffset.end] will be `0` and `3`
 * respectively.
 */
data class QuizQuestion(
    val question: String,
    val options: Set<String>,
    val answerOffset: AnswerOffset,
    val type: Label,
    val context: String? = null
) {
    val answer = question.slice(answerOffset.start until answerOffset.end)

    init {
        if (options.size != 4) throw Error("<options> must have a size of four: $this")
        if (answer !in options) throw Error("<answer> ($answer) must be in <options> ($options)")
    }
}

/** The [start]ing and [end]ing character offsets of an answer in a [String]. */
data class AnswerOffset(val start: Int, val end: Int) {
    init {
        if (start >= end) throw Error("<start> must be lesser than <end>: $this")
    }
}

data class HealthCheck(val quiz: Boolean, val nlp: Boolean)