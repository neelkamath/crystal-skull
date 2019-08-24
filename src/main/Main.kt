package com.neelkamath.crystalskull

import com.neelkamath.kwikipedia.getPage
import com.neelkamath.kwikipedia.getUrl
import com.neelkamath.kwikipedia.search
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.awaitAll

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { gson { } }
    install(Routing) {
        get("search") {
            val results = search(call.request.queryParameters["query"]!!).toList()
            call.respond(SearchResponse(results.map { Topic(it.title, it.description) }))
        }
        post("quiz") { postQuiz(this) }
    }
}

/** Handles an HTTP POST request with a [context] to the `/quiz` endpoint. */
private suspend fun postQuiz(context: PipelineContext<Unit, ApplicationCall>) = with(context) {
    val (topic, configuration, max) = call.receive<QuizRequest>()
    val documents = getPage(topic)
        .filterKeys { it !in listOf("See also", "References", "Further reading", "External links") }
        .map { tokenize(it.value) }
    val processedSentences = configuration.types
        .map { findNamesAsync(documents, it) }
        .awaitAll()
        .flatten()
        .let { sentences ->
            if (configuration.duplicateSentences) return@let sentences
            sentences.fold(mutableListOf<ProcessedSentence>()) { list, processed ->
                list.also { if (processed.sentence !in list.map { it.sentence }) list.add(processed) }
            }
        }
    val questions = generateQuestions(processedSentences, configuration)
    call.respond(QuizResponse(topic, if (max == null) questions else questions.take(max), getUrl(topic)))
}

/** Generates [QuizQuestion]s from [sentences] as stated in the [configuration]. */
internal fun generateQuestions(
    sentences: List<ProcessedSentence>,
    configuration: QuizConfiguration
): List<QuizQuestion> = createQuestions(sentences, configuration.types)
    .map { if (configuration.duplicateSentences) it.value else listOf(it.value.random()) }
    .flatten()
    .fold(mutableListOf<QuizQuestion>()) { list, question ->
        if (configuration.duplicateAnswers) {
            list.add(question)
        } else {
            if (question.questionAnswer.answer !in list.map { it.questionAnswer.answer }) list.add(question)
        }
        list
    }
    .shuffled()