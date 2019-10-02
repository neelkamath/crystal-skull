package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.string.shouldMatch
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class RelevantOptionTest : StringSpec({
    "Options must only be taken from the section the question is from unless more are required" {
        val relevantOptions = setOf("Apple", "Google", "Microsoft")
        Quizmaster()
            .getOptions(
                NamedEntity.person,
                Quizmaster.CorrectOption(
                    relevantOptions,
                    options = setOf("IBM", "Mozilla", "Nintendo", "SEGA"),
                    answer = "Google"
                )
            ) shouldContainAll relevantOptions
    }
})

class DuplicatesRemoverTest : StringSpec() {
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

class YearContainerTest : StringSpec({
    "Dates without four digits mustn't be considered as containing years" {
        val date = "August 7"
        withClue(date) { containsYear(date).shouldBeFalse() }
    }

    "Dates containing four digits must be considered as containing years" {
        val date1 = "07-06-2000"
        withClue(date1) { containsYear(date1).shouldBeTrue() }
        val date2 = "June 7, 2000"
        withClue(date2) { containsYear(date2).shouldBeTrue() }
    }
})

class EntityGeneratorTest : StringSpec({
    "Randomly generated dates must look real" {
        val month = Regex("""(January|February|March|April|May|June|July|August|September|October|November|December)""")
        repeat(100) {
            getRandomEntity(NamedEntity.date) shouldMatch Regex("""$month ([1-9]|[12][0-9]|3[01]), \d\d\d\d""")
        }
    }

    "Randomly generated percentages must look real" {
        repeat(10) { getRandomEntity(NamedEntity.percentage) shouldMatch Regex("""(100|\d{1,2}(\.\d{1,2})?)%""") }
    }
})

class TimeGeneratorTest : StringSpec({
    "Randomly generated times must look real" {
        repeat(100) { getRandomTime() shouldMatch Regex("""\d\d:\d\d [AP]M""") }
    }
})