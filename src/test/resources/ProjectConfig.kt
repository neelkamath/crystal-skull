package io.kotlintest.provided

import io.kotlintest.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override fun parallelism() = 2
}