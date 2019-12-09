// Functional tests (i.e., HTTP API black box tests).

package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.*
import com.neelkamath.crystalskull.test.Server.quiz
import com.neelkamath.crystalskull.test.Server.requestHealthCheck
import com.neelkamath.crystalskull.test.Server.requestQuiz
import com.neelkamath.crystalskull.test.Server.requestSearch
import com.neelkamath.crystalskull.test.Server.requestTrendingSearch
import com.neelkamath.crystalskull.test.Server.searchTrending
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveAtLeastSize
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.matchers.shouldBeInRange
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

class SearchTest : StringSpec({
    val results = requestSearch("appl")

    "A search response should have a status code of 200" { results.code() shouldBe 200 }

    "Searching for a misspelled topic should return relevant topics" {
        val topics = results.body()!!.topics
        withClue(topics.toString()) {
            topics.map { it.topic } shouldContainAll listOf("Apple", "Apple Inc.")
        }
    }
})

// Tests for the `/search_trending` endpoint.
class TrendingSearchTest : StringSpec({
    val results = requestTrendingSearch()

    "A search response should have a status code of 200" { results.code() shouldBe 200 }

    "At least 1 and at most 25 trending topics should be returned by default" {
        results.body()!!.topics.size shouldBeInRange 1..25
    }

    "Requesting seven topics should beckon at least one and at most seven results" {
        searchTrending(7).size shouldBeInRange 1..7
    }
})

// Tests for the `/quiz` endpoint.
class QuizTest : StringSpec({
    // The topic "Apple Inc." is used often since it has an abundance of questions and question types to test on.
    val topic = "Apple Inc."
    val response = quiz(QuizRequest(topic))

    "A quiz response should have a status code of 200" { requestQuiz(QuizRequest()).code() shouldBe 200 }

    "Requesting a quiz for a certain topic should beckon a response for the same topic" {
        response.metadata!!.topic shouldBe topic
    }

    "A quiz on a particular topic should contain the URL of the content's source" {
        response.metadata!!.url shouldBe "https://en.wikipedia.org/wiki/Apple_Inc."
    }

    "A quiz on supplied text shouldn't have topic metadata" {
        quiz(QuizRequest(text = listOf("Bill Gates founded Microsoft."))).metadata.shouldBeNull()
    }

    "Requesting seven quiz questions should beckon at most seven questions in the response" {
        quiz(QuizRequest("Apple Inc.", max = 7)).quiz.size shouldBeLessThanOrEqual 7
    }

    fun QuizResponse.getAnswersSansYears(): List<String> =
        quiz.filter { it.type == Label.DATE }.map { it.answer }.filter { !it.contains(Regex("""\d\d\d\d""")) }

    "Questions sans years should be absent by default" { response.getAnswersSansYears().shouldBeEmpty() }

    "Questions sans years should be present if requested" {
        quiz(QuizRequest("Apple Inc.", allowSansYears = true)).getAnswersSansYears() shouldHaveAtLeastSize 1
    }

    "Duplicate answers should be absent by default" {
        response.quiz.map { it.answer }.let { it.toSet() shouldHaveSize it.size }
    }

    "Duplicate answers should be present if requested" {
        quiz(QuizRequest("Apple Inc.", duplicateAnswers = true)).quiz.map { it.answer }.let {
            it.size shouldBeGreaterThan it.toSet().size
        }
    }

    "Duplicate questions should be absent by default" {
        val sentence = "Bill Gates is rich"
        quiz(QuizRequest(text = listOf(sentence, sentence))).quiz.map { it.question }.let {
            it.toSet() shouldHaveSize it.size
        }
    }

    "Duplicate questions should be present if requested" {
        quiz(QuizRequest("Apple Inc.", duplicateSentences = true)).quiz.map { it.question }.let {
            it.size shouldBeGreaterThan it.toSet().size
        }
    }

    fun testContext(hasContext: Boolean) {
        val sentence1 = "Bill Gates founded Microsoft."
        val sentence2 = "He was born on October 28, 1955."
        val request = QuizRequest(text = listOf("$sentence1 $sentence2"))
        val context = quiz(request).quiz.filter { it.question == if (hasContext) sentence2 else sentence1 }[0].context
        if (hasContext) context.shouldNotBeNull() else context.shouldBeNull()
    }

    "A question on the first sentence in a passage shouldn't have a context" { testContext(hasContext = false) }

    "A question on the second sentence in a passage should have a context" { testContext(hasContext = true) }

    "Each question should have four options" {
        response.quiz.map { it.options }.filter { it.size != 4 }.shouldBeEmpty()
    }

    "Each question's answer should match the substring indicated by the answer offset" {
        response.quiz
            .map { it.answer }
            .zip(response.quiz.map { it.question.substring(it.answerOffset.start until it.answerOffset.end) })
            .filter { it.first != it.second }
            .shouldBeEmpty()
    }

    "Related topic names should allow for quizzes to be generated off of them" {
        response.related!!
            .associateWith { quiz(QuizRequest(it)) }
            .mapValues { it.value.metadata!!.topic }
            .filter { it.key != it.value }
            .let {
                withClue("Quizzes generated for unrelated topics: $it") { it.size shouldBe 0 }
            }
    }

    "Related topics should be sorted in order of relevance" {
        withClue("The greater the number of occurrences of a topic, the greater its relevance") {
            val request = QuizRequest(text = listOf("Bill Gates founded Microsoft.", "Bill Gates was born in 1955."))
            quiz(request).related shouldBe listOf("Bill Gates", "Microsoft")
        }
    }

    fun testOptions(response: QuizResponse, label: Label, pattern: Regex) = response
        .quiz
        .filter { it.type == label }
        .flatMap { it.options }
        .filterNot { it.matches(pattern) }
        .shouldBeEmpty()

    "When there are a shortage of dates in the supplied text, realistic options should be generated" {
        val request = QuizRequest(text = listOf("He was born on October 28, 1955."))
        val month = Regex("(January|February|March|April|May|June|July|August|September|October|November|December)")
        testOptions(quiz(request), Label.DATE, Regex("""$month ([1-9]|[12][0-9]|3[01]), \d\d\d\d"""))
    }

    "When there are a shortage of times in the supplied text, realistic options should be generated" {
        testOptions(quiz(QuizRequest(text = listOf("It's 02:39 PM now."))), Label.TIME, Regex("""\d\d:\d\d [AP]M"""))
    }

    "When there are a shortage of percentages in the supplied text, realistic options should be generated" {
        val request = QuizRequest(text = listOf("I got 90% on my test."))
        testOptions(quiz(request), Label.PERCENT, Regex("""(100|\d{1,2}(\.\d{1,2})?)%"""))
    }

    "Options should only be taken from the section the question is from unless more are required" {
        val name1 = "Nikola Tesla"
        val name2 = "Albert Einstein"
        val name3 = "Christopher Robbin"
        val sentence1 = "$name1 is first."
        val sentence2 = "$name2 is second."
        val sentence3 = "$name3 is third."
        val section = "$sentence1 $sentence2 $sentence3"
        quiz(
            QuizRequest(
                text = listOf(
                    "Bill Gates was born on October 28, 1955.",
                    section,
                    "Galileo Galilei is fourth. Narendra Modi is fifth. Steve Jobs is sixth."
                )
            )
        ).quiz.filter { it.question == sentence1 }[0].options shouldContainAll listOf(name1, name2, name3)
    }
})

// Tests for the `/health_check` endpoint.
class HealthCheckTest : StringSpec({
    "A health check request should beckon a status code of 200" { requestHealthCheck().code() shouldBe 200 }
})

private object Server {
    private val service = Retrofit.Builder()
        .baseUrl(System.getenv("CRYSTAL_SKULL_URL"))
        .client(OkHttpClient.Builder().readTimeout(2, TimeUnit.MINUTES).build())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(QuizService::class.java)

    private interface QuizService {
        @GET("search")
        fun search(@Query("query") topic: String): Call<SearchResponse>

        @GET("search_trending")
        fun searchTrending(): Call<SearchResponse>

        @GET("search_trending")
        fun searchTrending(@Query("max") max: Int): Call<SearchResponse>

        @POST("quiz")
        fun quiz(@Body request: QuizRequest): Call<QuizResponse>

        @GET("health_check")
        fun checkHealth(): Call<HealthCheck>
    }

    fun requestSearch(query: String): Response<SearchResponse> = service.search(query).execute()

    fun requestTrendingSearch(): Response<SearchResponse> = service.searchTrending().execute()

    fun searchTrending(max: Int): List<Topic> = service.searchTrending(max).execute().body()!!.topics

    fun quiz(request: QuizRequest): QuizResponse = requestQuiz(request).body()!!

    fun requestQuiz(request: QuizRequest): Response<QuizResponse> = service.quiz(request).execute()

    fun requestHealthCheck(): Response<HealthCheck> = service.checkHealth().execute()
}
