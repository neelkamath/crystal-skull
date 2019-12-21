package com.neelkamath.crystalskull

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

object NLP {
    private val service = Retrofit.Builder()
        .baseUrl(System.getenv("SPACY_SERVER_URL"))
        .client(OkHttpClient.Builder().readTimeout(2, TimeUnit.MINUTES).build())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(NLPService::class.java)

    /**
     * An HTTP request for named entity recognition.
     *
     * Although you could pass the full text as a single array item, it would be faster to split large text into
     * multiple [sections]. Each item needn't be semantically related.
     *
     * [useSense2vec] to (extremely slowly) compute similar phrases for each entity.
     */
    private data class NERRequest(
        val sections: List<String>,
        @SerializedName("sense2vec") val useSense2vec: Boolean = false
    )

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
        /** Phrases similar to the [text]. */
        val sense2vec: List<SimilarPhrase>,
        /** The token offset for the start of the entity. */
        val start: Int,
        /** The token offset for the end of the entity. */
        val end: Int,
        /** The text content of the entity with a trailing whitespace character if the last token has one. */
        @SerializedName("text_with_ws") val textWithWhitespace: String
    )

    /** A [phrase] with a [similarity] in the range of 0-1. */
    data class SimilarPhrase(val phrase: String, val similarity: Double)

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

    fun recognizeNamedEntities(strings: List<String>, useSense2vec: Boolean = false): List<NamedEntity> =
        service.recognizeNamedEntities(NERRequest(strings, useSense2vec)).execute().body()!!.data

    fun tokenize(text: String): List<String> =
        service.tokenize(TokenizerRequest(text)).execute().body()!!.tokens

    fun sentencize(data: String): List<String> =
        service.sentencize(SentencizerRequest(data)).execute().body()!!.sentences

    /** Whether the NLP service is operational. */
    fun isHealthy(): Boolean = service.checkHealth().execute().isSuccessful
}