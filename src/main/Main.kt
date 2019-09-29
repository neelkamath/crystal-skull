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
                call.respond(if (request.text != null) quizText(request) else quizTopic(request))
            }
        }
        get("health_check") { call.respond("OK") }
    }
}

/** Creates a quiz for a [QuizRequest.topic]. If the [QuizRequest.topic] is `null`, a random topic will be chosen. */
private suspend fun quizTopic(request: QuizRequest): QuizResponse {
    val topic = request.topic ?: search()[0].title
    val page = getPage(topic)
    val documents = page
        .filterKeys { it !in listOf("See also", "References", "Further reading", "External links") }
        .values
    val processedSentences = processSentences(documents.toList(), request.configuration)
    return QuizResponse(
        assembleQuestions(processedSentences, request.configuration, request.max),
        QuizMetadata(topic, getUrl(topic)),
        page["See also"]?.split("\n")
    )
}

/** Creates a [QuizResponse] for a non-null [QuizRequest.text]. */
private suspend fun quizText(request: QuizRequest): QuizResponse = with(request) {
    val processedSentences = processSentences(text!!, configuration)
    QuizResponse(assembleQuestions(processedSentences, configuration, max), related = findRelatedTopics(text))
}

/** Processes [sections] of text (e.g., a section on the early life of Bill Gates) according to the [configuration]. */
private fun processSentences(sections: List<String>, configuration: QuizConfiguration): List<ProcessedSentence> =
    configuration.types
        .flatMap { entity ->
            findNames(sections.map { tokenize(it) }, entity)
        }
        .let { if (configuration.duplicates.duplicateSentences) it else filterSentences(it) }

/** Assembles [QuizQuestion]s from [sentences] according to the [config]. */
private fun assembleQuestions(
    sentences: List<ProcessedSentence>,
    config: QuizConfiguration,
    max: Int?
): List<QuizQuestion> = generateQuestions(sentences, config).let { if (max == null) it else it.take(max) }

/** Filters unique [sentences] by their [ProcessedContext.sentence]s. */
private fun filterSentences(sentences: List<ProcessedSentence>): List<ProcessedSentence> =
    sentences.fold(mutableListOf()) { list, processed ->
        if (processed.context.sentence !in list.map { it.context.sentence }) list.add(processed)
        list
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

/** Generates [QuizQuestion]s from [sentences] as stated in the [configuration]. */
internal fun generateQuestions(
    sentences: List<ProcessedSentence>,
    configuration: QuizConfiguration
): List<QuizQuestion> = createQuestions(sentences, configuration.types, configuration.allowSansYears)
    .filterValues { it.isNotEmpty() }
    .flatMap { if (configuration.duplicates.duplicateSentences) it.value else listOf(it.value.random()) }
    .fold(mutableListOf<QuizQuestion>()) { list, question ->
        val isUniqueAnswer = question.questionAnswer.answer !in list.map { it.questionAnswer.answer }
        list.apply { if (configuration.duplicates.duplicateAnswers || isUniqueAnswer) add(question) }
    }
    .shuffled()