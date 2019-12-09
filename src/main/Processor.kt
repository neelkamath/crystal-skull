package com.neelkamath.crystalskull

import com.google.gson.annotations.SerializedName
import com.neelkamath.crystalskull.NLP.recognizeNamedEntities
import com.neelkamath.crystalskull.NLP.sentencize
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

object NLP {
    private val service = Retrofit.Builder()
        .baseUrl(System.getenv("SPACY_SERVER_URL"))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(NLPService::class.java)

    /**
     * Although you could pass the full text as a single array item, it would be faster to split large text into
     * multiple items. Each item needn't be semantically related.
     */
    private data class NERRequest(val sections: List<String>)

    private data class NERResponse(val data: List<NamedEntity>)

    /** The recognized named [entities] in the sentence's [text]. */
    data class NamedEntity(val entities: List<RecognizedEntity>, val text: String)

    data class RecognizedEntity(
        /** Recognized entity. */
        val text: String,
        val label: Label,
        /** The character offset for the start of the entity. */
        val startChar: Int,
        /** The character offset for the end of the entity. */
        val endChar: Int,
        val lemma: String,
        /** The token offset for the start of the entity. */
        val start: Int,
        /** The token offset for the end of the entity. */
        val end: Int,
        /** The text content of the entity with a trailing whitespace character if the last token has one. */
        @SerializedName("text_with_ws") val textWithWhitespace: String
    )

    private data class TokenizerRequest(val text: String)

    private data class TokenizerResponse(val tokens: List<String>)

    private data class SentencizerRequest(val text: String)

    private data class SentencizerResponse(val sentences: List<String>)

    private interface NLPService {
        @POST("ner")
        fun recognizeNamedEntities(@Body request: NERRequest): Call<NERResponse>

        @POST("tokenizer")
        fun tokenize(@Body request: TokenizerRequest): Call<TokenizerResponse>

        @POST("sentencizer")
        fun sentencize(@Body request: SentencizerRequest): Call<SentencizerResponse>

        @GET("health_check")
        fun checkHealth(): Call<Response<Unit>>
    }

    fun recognizeNamedEntities(strings: List<String>): List<NamedEntity> =
        service.recognizeNamedEntities(NERRequest(strings)).execute().body()!!.data

    fun tokenize(text: String): List<String> =
        service.tokenize(TokenizerRequest(text)).execute().body()!!.tokens

    fun sentencize(data: String): List<String> =
        service.sentencize(SentencizerRequest(data)).execute().body()!!.sentences

    /** Whether the NLP service is operational. */
    fun isHealthy(): Boolean = service.checkHealth().execute().isSuccessful
}

/** A section of text, such as one on the early life of Bill Gates. */
typealias ProcessedSection = List<ProcessedSentence>

/** [tokens] are the original [sentence]'s tokens. */
data class TokenizedSentence(val sentence: String, val tokens: List<String>)

/** [names] are the entities (each having the same [label]) fround in the [context]. */
data class ProcessedSentence(val context: ProcessedContext, val label: Label, val names: List<String>)

/** The original [sentence] processed, and if there was one, the sentence [previous] to it. */
data class ProcessedContext(val sentence: String, val previous: String? = null)

/** Tokenizes English [data]. */
fun tokenize(data: String): List<TokenizedSentence> =
    sentencize(data).map { TokenizedSentence(it, NLP.tokenize(it)) }

/**
 * Uses NER on an English [document].
 *
 * The [ProcessedContext.previous] sentence from each returned [ProcessedSentence] is the sentence prior to each
 * [TokenizedSentence] in the [document]. So if you are using the [ProcessedContext.previous] sentence, you must only
 * pass semantically related contents in the [document] (e.g., a single paragraph). Otherwise, the first sentence from
 * the previous paragraph would be used as in the [ProcessedContext] even though it isn't contextual.
 */
fun findNames(document: List<TokenizedSentence>): List<ProcessedSentence> {
    val sentences = document.map { it.sentence }
    val data = recognizeNamedEntities(sentences)
    return data.mapIndexed { index, namedEntity ->
        val sentence = data.elementAt(index).text
        val context = ProcessedContext(sentence, sentences.elementAtOrNull(sentences.indexOf(sentence) - 1))
        namedEntity.entities.filter { it.text != "%" }.groupBy { it.label }.mapValues { entity ->
            ProcessedSentence(context, entity.key, entity.value.map { it.text })
        }
    }.flatMap { it.values }
}