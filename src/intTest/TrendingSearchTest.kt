package com.neelkamath.crystalskull.intTest

import io.kotlintest.matchers.collections.shouldHaveAtMostSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

// Tests for the `/search_trending` endpoint.
class TrendingSearchTest : StringSpec({
    val results = Server.requestTrendingSearch()

    "A search response should have a status code of 200" { results.code() shouldBe 200 }

    "At most 25 trending topics should be returned by default" { results.body()!!.topics shouldHaveAtMostSize 25 }

    "Requesting seven topics should beckon at most seven results" { Server.searchTrending(7) shouldHaveAtMostSize 7 }
})