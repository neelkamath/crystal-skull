package com.neelkamath.crystalskull

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
// TODO: Update deploy instructions in fork.md, .gitlab-ci.yml, and README.md. Credit that blog if it worked.

fun Application.main() {
    install(CallLogging)
    install(ContentNegotiation) { gson { } }
    install(Routing) { get("search") { call.respond("Hollo Bob") } } // TODO: Revert after testing Docker pushes.
}