package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.NamedEntity
import com.neelkamath.crystalskull.getRandomEntity
import com.neelkamath.crystalskull.getRandomTime
import io.kotlintest.matchers.string.shouldMatch
import io.kotlintest.specs.StringSpec

class EntityGeneratorTest : StringSpec({
    "Randomly generated dates must look real" {
        val month = Regex("""(January|February|March|April|May|June|July|August|September|October|November|December)""")
        getRandomEntity(NamedEntity.date) shouldMatch Regex("""$month ([1-9]|[12][0-9]|3[01]), \d\d\d\d""")
    }

    "Randomly generated percentages must look real" {
        repeat(5) { getRandomEntity(NamedEntity.percentage) shouldMatch Regex("""\d{1,2}(\.\d{1,2})?%""") }
    }
})

class TimeGeneratorTest : StringSpec({
    "Randomly generated times must look real" { getRandomTime() shouldMatch Regex("""\d\d:\d\d [AP]M""") }
})