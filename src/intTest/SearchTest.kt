package com.neelkamath.crystalskull.intTest

import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

// Tests for the `/search` endpoint.
class SearchTest : StringSpec({
    val results = Server.requestSearch("appl")

    "A search response should have a status code of 200" { results.code() shouldBe 200 }

    "Searching for a misspelled topic should return relevant topics" {
        val topics = results.body()!!.topics
        withClue(topics.toString()) {
            topics.map { it.topic } shouldContainAll listOf("Apple", "Apple Inc.")
        }
    }
})