package com.neelkamath.crystalskull

import com.neelkamath.crystalskull.NameFinder.findNames
import com.neelkamath.crystalskull.Tokenizer.tokenize
import com.neelkamath.kwikipedia.getPage
import com.neelkamath.kwikipedia.search
import com.neelkamath.kwikipedia.searchMostViewed
import com.neelkamath.kwikipedia.searchTitle
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { gson() }
    install(Routing) {
        get("search") {
            val results = search(call.request.queryParameters["query"]!!)
            call.respond(SearchResponse(results.map { Topic(it.title, it.description) }))
        }
        get("search_trending") {
            val results = searchMostViewed(call.request.queryParameters["max"]?.toInt() ?: 5)
            call.respond(SearchResponse(results.map { Topic(it.title, it.description) }))
        }
        post("quiz") { quiz(this) }
        get("health_check") { call.respond(HttpStatusCode.NoContent) }
    }
}

/** Deals with HTTP POST requests to the `/quiz` endpoint. */
private suspend fun quiz(context: PipelineContext<Unit, ApplicationCall>) = with(context) {
    val request = call.receive<QuizRequest>()
    if (request.topic != null && request.text != null) {
        call.respond(HttpStatusCode.BadRequest, QuizRequest.invalidMessage)
    } else {
        with(QuizGenerator(request)) { call.respond(if (request.text != null) quizText() else quizTopic()) }
    }
}

internal class QuizGenerator(private val request: QuizRequest) {
    /** Creates a quiz for a [QuizRequest.topic] (if `null`, a random topic will be chosen). */
    internal suspend fun quizTopic(): QuizResponse {
        val topic = request.topic ?: searchMostViewed().random().title
        val page = getPage(topic)
        return QuizResponse(
            if (request.max != null && request.max == 0) {
                listOf()
            } else {
                generateQuestions(
                    page
                        .filterKeys { it !in listOf("See also", "References", "Further reading", "External links") }
                        .map { processSection(it.value) }
                )
            },
            QuizMetadata(topic, searchTitle(topic).url),
            page["See also"]?.split("\n")
        )
    }

    /** Creates a [QuizResponse] for a non-null [QuizRequest.text]. */
    internal suspend fun quizText(): QuizResponse = QuizResponse(
        if (request.max != null && request.max == 0) {
            listOf()
        } else {
            generateQuestions(request.text!!.map { processSection(it) })
        },
        related = findRelatedTopics(request.text!!)
    )

    /** Processes a [section] of text (e.g., the early life of Bill Gates). */
    private fun processSection(section: String): List<ProcessedSentence> = request
        .types
        .flatMap { findNames(tokenize(section), it) }
        .let { sentences ->
            if (request.duplicateSentences) return@let sentences
            sentences.fold(mutableListOf()) { list, processed ->
                if (processed.context.sentence !in list.map { it.context.sentence }) list.add(processed)
                list
            }
        }

    internal fun generateQuestions(sections: List<ProcessedSection>): List<QuizQuestion> =
        Quizmaster(request.allowSansYears)
            .quiz(sections, request.types)
            .filterValues { it.isNotEmpty() }
            .flatMap { if (request.duplicateSentences) it.value else listOf(it.value.random()) }
            .fold(mutableListOf<QuizQuestion>()) { list, question ->
                val isUniqueAnswer = question.answer !in list.map { it.answer }
                list.apply { if (request.duplicateAnswers || isUniqueAnswer) add(question) }
            }
            .shuffled()
            .let { if (request.max == null) it else it.take(request.max) }
}

/** Finds topics related to [sections] on Wikipedia (ordered with the most relevant first). */
internal suspend fun findRelatedTopics(sections: List<String>): List<String> {
    val entities = listOf(NamedEntity.person, NamedEntity.organization, NamedEntity.location).flatMap { entity ->
        findNames(sections.map { tokenize(it) }, entity).flatMap { it.names }
    }
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