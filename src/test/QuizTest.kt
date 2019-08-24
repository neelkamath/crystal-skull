package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.NamedEntity
import com.neelkamath.crystalskull.getRandomEntity
import com.neelkamath.crystalskull.getRandomTime
import com.neelkamath.crystalskull.removeDuplicates
import io.kotlintest.matchers.string.shouldMatch
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
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

class DuplicateRemoverTest : StringSpec() {
    init {
        val testSet =
            setOf("Steve Paul Jobs", "Steve", "Steve Wozniak", "Steve Gary Wozniak", "Gil Steve", "Gil Amelio")
        val test = { set: Set<String>, sought: String, duplicates: Set<String> ->
            withClue("Sought: $sought") { removeDuplicates(set, sought) shouldBe set - duplicates }
        }

        "Smaller duplicates must be removed when a bigger string is sought" {
            test(testSet, "Steve Wozniak", setOf("Steve", "Steve Gary Wozniak"))
        }

        "Bigger duplicates must be removed when a smaller string is sought" {
            test(testSet, "Steve", setOf("Steve Paul Jobs", "Steve Wozniak", "Steve Gary Wozniak", "Gil Steve"))
        }

        "String containing unique terms mustn't be removed when there are no duplicates" {
            test(setOf("Neel K.", "Pasquale S.", "Lord G."), "Neel K.", setOf())
        }

        "Strings containing the same terms mustn't be removed when there are no duplicates" {
            test(testSet, "Gil Amelio", setOf())
        }
    }
}