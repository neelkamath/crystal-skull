package com.neelkamath.crystalskull

import com.neelkamath.crystalskull.NameFinder.findNames
import com.neelkamath.crystalskull.Tokenizer.tokenize
import com.neelkamath.kwikipedia.getPage
import com.neelkamath.kwikipedia.getUrl
import com.neelkamath.kwikipedia.search
import io.ktor.application.Application
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

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { gson() }
    install(Routing) {
        get("search") {
            val results = search(call.request.queryParameters["query"]!!).toList()
            call.respond(SearchResponse(results.map { Topic(it.title, it.description) }))
        }
        post("quiz") {
            val request = call.receive<QuizRequest>()
            if (request.topic != null && request.text != null) {
                call.respond(HttpStatusCode.BadRequest, QuizRequest.invalidMessage)
            } else {
                with(QuizGenerator(request)) { call.respond(if (request.text != null) quizText() else quizTopic()) }
            }
        }
        get("health_check") { call.respond(HttpStatusCode.NoContent) }
    }
}

internal class QuizGenerator(private val request: QuizRequest) {
    /** Creates a quiz for a [QuizRequest.topic] (if `null`, a random topic will be chosen. */
    internal suspend fun quizTopic(): QuizResponse {
        val topic = request.topic ?: search()[0].title
        val page = getPage(topic)
        val documents = page
            .filterKeys { it !in listOf("See also", "References", "Further reading", "External links") }
            .values
        val processedSentences = processSentences(documents.toList())
        return QuizResponse(
            assembleQuestions(processedSentences),
            QuizMetadata(topic, getUrl(topic)),
            page["See also"]?.split("\n")
        )
    }

    /** Creates a [QuizResponse] for a non-null [QuizRequest.text]. */
    internal suspend fun quizText(): QuizResponse =
        QuizResponse(assembleQuestions(processSentences(request.text!!)), related = findRelatedTopics(request.text))

    /** Creates [ProcessedSentence]s from [sections] of text (e.g., a section on the early life of Bill Gates). */
    private fun processSentences(sections: List<String>): List<ProcessedSentence> = request.types
        .flatMap { entity ->
            findNames(sections.map { tokenize(it) }, entity)
        }
        .let { sentences ->
            if (request.duplicateSentences) return@let sentences
            sentences.fold(mutableListOf()) { list, processed ->
                if (processed.context.sentence !in list.map { it.context.sentence }) list.add(processed)
                list
            }
        }

    private fun assembleQuestions(sentences: List<ProcessedSentence>): List<QuizQuestion> =
        generateQuestions(sentences).let { if (request.max == null) it else it.take(request.max) }

    internal fun generateQuestions(sentences: List<ProcessedSentence>): List<QuizQuestion> =
        createQuestions(sentences, request.types, request.allowSansYears)
            .filterValues { it.isNotEmpty() }
            .flatMap { if (request.duplicateSentences) it.value else listOf(it.value.random()) }
            .fold(mutableListOf<QuizQuestion>()) { list, question ->
                val isUniqueAnswer = question.answer !in list.map { it.answer }
                list.apply { if (request.duplicateAnswers || isUniqueAnswer) add(question) }
            }
            .shuffled()
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