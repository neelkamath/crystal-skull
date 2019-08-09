package com.neelkamath.crystalskull.test

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.neelkamath.crystalskull.main
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertTrue

class SearchTest {
    @Test
    fun `Searching for "appl" should include the fruit and tech company as results`() = withTestApplication(
        Application::main
    ) {
        handleRequest(HttpMethod.Get, "search?query=appl").response.run {
            val topics = Gson().fromJson(content, JsonObject::class.java).keySet()
            assertTrue(topics.containsAll(listOf("Apple", "Apple Inc.")), "The topics were instead: $topics")
        }
    }
}