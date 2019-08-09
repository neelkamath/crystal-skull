package com.neelkamath.crystalskull

import com.neelkamath.kwikipedia.search
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

// TODO: Test that the documentation link the GitHub UI and README are correct after GitLab Pages has been deployed.
// TODO: Update deploy instructions in fork.md, .gitlab-ci.yml, and README.md.
// TODO: Test that all the fork and README instructions actually work

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { gson { } }
    install(Routing) { get("search") { call.respond(search(call.request.queryParameters["query"]!!)) } }
}