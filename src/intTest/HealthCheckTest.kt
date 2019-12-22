package com.neelkamath.crystalskull.intTest

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

// Tests for the `/health_check` endpoint.
class HealthCheckTest : StringSpec({
    "A health check request should beckon a status code of 200" { Server.requestHealthCheck().code() shouldBe 200 }
})