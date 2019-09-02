package com.neelkamath.crystalskull

import com.neelkamath.crystalskull.NameFinder.findNames
import com.neelkamath.crystalskull.Tokenizer.tokenize
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

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { gson() }
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
    val page = getPage(topic)
    val documents = page
        .filterKeys { it !in listOf("See also", "References", "Further reading", "External links") }
        .map { tokenize(it.value) }
    val processedSentences = configuration.types.flatMap { findNames(documents, it) }.let { sentences ->
        if (configuration.duplicates.duplicateSentences) return@let sentences
        sentences.fold(mutableListOf<ProcessedSentence>()) { list, processed ->
            if (processed.context.sentence !in list.map { it.context.sentence }) list.add(processed)
            list
        }
    }
    call.respond(
        QuizResponse(
            QuizMetadata(topic, getUrl(topic)),
            generateQuestions(processedSentences, configuration).let { if (max == null) it else it.take(max) },
            page["See also"]?.split("\n")
        )
    )
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