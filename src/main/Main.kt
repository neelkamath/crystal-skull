package com.neelkamath.crystalskull

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neelkamath.kwikipedia.getPage
import com.neelkamath.kwikipedia.search
import com.neelkamath.kwikipedia.searchMostViewed
import com.neelkamath.kwikipedia.searchTitle
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

/** Shared Gson configuration for the entire project. */
val gson: Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { register(ContentType.Application.Json, GsonConverter(gson)) }
    install(Routing) {
        get("search") {
            val results = search(call.request.queryParameters["query"]!!)
            call.respond(SearchResponse(results.map { Topic(it.title, it.description) }))
        }
        get("search_trending") {
            val results = searchMostViewed(call.request.queryParameters["max"]?.toInt() ?: 25)
            call.respond(SearchResponse(results.map { Topic(it.title, it.description) }))
        }
        post("quiz") {
            val request = call.receive<QuizRequest>()
            with(QuizGenerator(request)) { call.respond(if (request.text != null) quizText() else quizTopic()) }
        }
        get("health_check") { call.respond(HealthCheck(nlp = NLP.isHealthy())) }
    }
}

private class QuizGenerator(private val request: QuizRequest) {
    /** Creates a quiz for a [QuizRequest.topic] (if `null`, a random topic will be chosen). */
    suspend fun quizTopic(): QuizResponse {
        val topic = request.topic ?: getRandomTopic()
        val page = getPage(topic)
        return QuizResponse(
            if (request.max != null && request.max == 0) listOf()
            else generateQuestions(
                page
                    .filterKeys { it !in listOf("See also", "References", "Further reading", "External links") }
                    .map { processSection(it.value) }
            ),
            QuizMetadata(topic, searchTitle(topic).url),
            page["See also"]?.split("\n")
        )
    }

    /** Creates a [QuizResponse] for a non-null [QuizRequest.text]. */
    fun quizText(): QuizResponse = QuizResponse(
        if (request.max != null && request.max == 0) listOf()
        else generateQuestions(request.text!!.map { processSection(it) }),
        related = findRelatedTopics(request.text!!)
    )

    /** Processes a [section] of text (e.g., the early life of Bill Gates). */
    private fun processSection(section: String): List<ProcessedSentence> = findNames(tokenize(section))
        .let { sentences ->
            if (request.duplicateSentences) return@let sentences
            sentences.fold(mutableListOf()) { list, processed ->
                if (processed.context.sentence !in list.map { it.context.sentence }) list.add(processed)
                list
            }
        }

    private fun generateQuestions(sections: List<ProcessedSection>): List<QuizQuestion> =
        Quizmaster(request.allowSansYears).quiz(sections)
            .filterValues { it.isNotEmpty() }
            .flatMap { if (request.duplicateSentences) it.value else listOf(it.value.random()) }
            .fold(mutableListOf<QuizQuestion>()) { list, question ->
                val isUniqueAnswer = question.answer !in list.map { it.answer }
                list.apply { if (request.duplicateAnswers || isUniqueAnswer) add(question) }
            }
            .shuffled()
            .let { it.take(request.max ?: it.size) }
}

/**
 * Returns a random Wikipedia article's title (usually one which is trending in the last day).
 *
 * [searchMostViewed] occasionally returns zero search results for random long periods of time due to a Wikipedia bug.
 * To ensure an article title is always returned, we fall back to [search]ing a completely random topic.
 */
private suspend fun getRandomTopic(): String =
    searchMostViewed().let { if (it.isEmpty()) search()[0].title else it.random().title }

private val relatedTopicLabels = listOf(
    Label.PERSON,
    Label.NORP,
    Label.FAC,
    Label.ORG,
    Label.GPE,
    Label.LOC,
    Label.PRODUCT,
    Label.EVENT,
    Label.WORK_OF_ART,
    Label.LAW,
    Label.LANGUAGE
)

/** Finds topics related to [sections] on Wikipedia (ordered with the most relevant first). */
private fun findRelatedTopics(sections: List<String>): List<String> {
    val entities = findNames(sections.flatMap { tokenize(it) })
        .filter { it.label in relatedTopicLabels }
        .flatMap { it.names }
    return entities
        .associateWith { entity ->
            entities.count { it == entity }
        }
        .toList()
        .sortedByDescending { it.second }
        .map { it.first }
        .filter { entity ->
            search(entity).let { it.isNotEmpty() && it[0].title == entity }
        }
}