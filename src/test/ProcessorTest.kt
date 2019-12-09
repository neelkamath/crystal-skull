package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.ProcessedSentence
import com.neelkamath.crystalskull.TokenizedSentence
import com.neelkamath.crystalskull.findNames
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class NameFinderTest : StringSpec({
    val names = findNames(
        listOf(
            TokenizedSentence("My first name is John.", listOf("John")),
            TokenizedSentence("My second name is Doe.", listOf("Doe")),
            TokenizedSentence("This sentence has no tokens.", listOf()),
            TokenizedSentence("My mom's name is Mary.", listOf("Mary"))
        )
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