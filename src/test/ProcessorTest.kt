package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.ProcessedSentence
import com.neelkamath.crystalskull.cleanSense2vec
import com.neelkamath.crystalskull.findNames
import com.neelkamath.crystalskull.removeCaseInsensitiveDuplicates
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class NameFinderTest : StringSpec({
    val names = findNames(
        listOf("My first name is John.", "My second name is Doe.", "This has no tokens.", "My mom's name is Mary.")
    ).fold(mutableListOf<ProcessedSentence>()) { accumulator, name ->
        if (name.context.sentence in accumulator.map { it.context.sentence }) return@fold accumulator
        accumulator.apply { add(name) }
    }

    "Sentences with sentences prior to them should have their context set to their previous sentence" {
        val context = names[1].context
        withClue(context.toString()) { context.previous.shouldNotBeNull() }
    }

    "A sentence without a previous sentence should have its context set to null" {
        names[0].context.previous.shouldBeNull()
    }

    "A sentence should have its context set to its previous sentence even if the previous sentence is sans tokens" {
        names[2].context.previous shouldBe "This sentence has no tokens."
    }
})

class Sense2vecCleanerTest : StringSpec({
    "Leading and trailing whitespace should be trimmed" {
        cleanSense2vec(listOf(" Bill Gates  ", "Apple", " Steve", "Jobs ")) shouldBe
                listOf("Bill Gates", "Apple", "Steve", "Jobs")
    }

    """Phrases containing "&gt;" and "&lt;" should be removed""" {
        cleanSense2vec(listOf(" &gt;Brad", "Chris&lt;", "Brandon ")) shouldBe listOf("Brandon")
    }

    "Duplicates should be removed case-insensitively" {
        cleanSense2vec(listOf("Brandon", " brandon", "ChriS  ", "chris ")) shouldBe listOf("Brandon", "ChriS")
    }
})

class CaseInsensitiveDuplicatesRemoverTest : StringSpec({
    "Case-sensitive duplicates should be removed" {
        removeCaseInsensitiveDuplicates(listOf("Bob", "Bob", "Bill")) shouldBe listOf("Bob", "Bill")
    }

    "Case-insensitive duplicates should be removed" {
        val duplicates = listOf("Billy", "billy", "biLly", "bob", "Bobby", "Bobby")
        removeCaseInsensitiveDuplicates(duplicates) shouldBe listOf("Billy", "bob", "Bobby")
    }

    "Given a set of duplicates, the first one should be the one to be retained" {
        val duplicates = listOf("John", "apple", "Apple", "pear", "ApplE", "jOhN", "Pear")
        removeCaseInsensitiveDuplicates(duplicates) shouldBe listOf("John", "apple", "pear")
    }
})