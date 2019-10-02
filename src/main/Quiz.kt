package com.neelkamath.crystalskull

import com.github.javafaker.Faker
import java.time.Month
import java.time.format.TextStyle
import java.util.*

/** Options lacking years may be included if you [allowSansYears]. */
internal class Quizmaster(private val allowSansYears: Boolean = false) {
    /** One or more [options], of which one is the correct [answer]. There are more [relevantOptions] than [options]. */
    internal data class CorrectOption(val relevantOptions: Set<String>, val options: Set<String>, val answer: String) {
        init {
            if (answer !in relevantOptions + options) throw Error("<answer> must be in <options>: $this")
            val intersection = relevantOptions.intersect(options)
            if (intersection.isNotEmpty()) {
                throw Error("<relevantOptions> and <options> must be disjoint (intersection: $intersection)")
            }
        }
    }

    /** Creates [QuizQuestion]s out of [sections] using [entities]. */
    internal fun quiz(
        sections: List<ProcessedSection>,
        entities: List<NamedEntity>
    ): Map<ProcessedSentence, List<QuizQuestion>> {
        val mutableMap = mutableMapOf<ProcessedSentence, List<QuizQuestion>>()
        val options = createOptions(sections, entities)
        for ((index, section) in sections.withIndex()) {
            mutableMap.putAll(section.associateWith { createQuestions(it, options, index) })
        }
        return mutableMap
    }

    /** Map of options. Its keys are [entities]. Its values are the entities found in each of the [sections]. */
    private fun createOptions(
        sections: List<ProcessedSection>,
        entities: List<NamedEntity>
    ): Map<NamedEntity, List<List<String>>> = entities.associateWith { entity ->
        sections.map { section ->
            section.filter { it.entity == entity }.flatMap { it.names }
        }
    }

    /** The [index] indicates which [options] are more relevant to the [sentence]. */
    private fun createQuestions(
        sentence: ProcessedSentence,
        options: Map<NamedEntity, List<List<String>>>,
        index: Int
    ): List<QuizQuestion> = sentence.names
        .filter { if (sentence.entity == NamedEntity.date && !allowSansYears) containsYear(it) else true }
        .map {
            val possibleOptions = options.getValue(sentence.entity)
            question(
                sentence,
                CorrectOption(
                    relevantOptions = possibleOptions[index].toSet(),
                    options = (possibleOptions.flatten() - possibleOptions[index]).toSet(),
                    answer = it
                )
            )
        }

    /** If there aren't enough [CorrectOption.options], fake options will be generated. */
    private fun question(processedSentence: ProcessedSentence, correctOption: CorrectOption): QuizQuestion =
        with(processedSentence) {
            QuizQuestion(
                context.sentence,
                getOptions(entity, correctOption),
                context.sentence.indexOf(correctOption.answer).let {
                    AnswerOffset(it, it + correctOption.answer.length)
                },
                entity,
                context.previous
            )
        }

    /** Gives four options from the [correctOption]. [entity] options will be generated if required. */
    internal fun getOptions(entity: NamedEntity, correctOption: CorrectOption): Set<String> =
        (correctOption.relevantOptions.shuffled().take(3) + correctOption.answer).let {
            val filter = { set: Set<String> -> filterOptions(set, correctOption.answer, entity).toMutableSet() }
            var set = filter(it.toSet())
            var failures = 0
            while (set.size < 4 && correctOption.options.isNotEmpty() && failures < 3) {
                val size = set.size
                set = filter(set.apply { add(correctOption.options.random()) })
                if (set.size == size) failures++
            }
            while (set.size < 4) set = filter(set.apply { add(getRandomEntity(entity)) })
            set
        }.shuffled().toSet()

    /**
     * Removes duplicate [options] of [entity]s while [keep]ing at least one.
     *
     * If [entity] is a [NamedEntity.date], options sans years will be removed if you don't [allowSansYears] (e.g.,
     * `June 7` is an option sans a year, but `June 7, 2000` isn't).
     */
    private fun filterOptions(options: Set<String>, keep: String, entity: NamedEntity): Set<String> {
        var set = options.toMutableSet()
        if (entity in listOf(NamedEntity.location, NamedEntity.organization, NamedEntity.person)) {
            set = removeDuplicates(set, keep).toMutableSet()
        }
        if (entity != NamedEntity.date) return set
        return if (allowSansYears) set else set.filter { containsYear(it) }.toSet()
    }
}

/**
 * Removes duplicate [strings] so that the [sought] after one is unique.
 *
 * Let's take an example where the strings are `"Steve Paul Jobs"`, `"Steve"`, `"Steve Wozniak"`, `
 * "Steve Gary Wozniak"`, `"Gil Steve"`, and `"Gil Amelio"`. `"Steve"` and `"Steve Gary Wozniak"` will be removed if
 * the [sought] after string is `"Steve Wozniak"`. `"Steve Paul Jobs"`, `"Steve Wozniak"`, `"Steve Gary Wozniak"`, and
 * `"Gil Steve"` will be removed if `"Steve"` is the [sought] after string. Nothing will be removed if `"Gil Amelio"` is
 * the [sought] after string.
 */
internal fun removeDuplicates(strings: Set<String>, sought: String): Set<String> = strings
    .map { it.split(" ") }
    .filter {
        val soughtSplit = sought.split(" ")
        it != soughtSplit && !soughtSplit.containsAll(it) && !it.containsAll(soughtSplit)
    }
    .map { it.joinToString(" ") }
    .toSet() + sought

/** Whether [string] contains a year (a four-digit number). */
internal fun containsYear(string: String) = string.contains(Regex("""\d\d\d\d"""))

/** Generates a fake [entity]. */
internal fun getRandomEntity(entity: NamedEntity): String = with(Faker()) {
    when (entity) {
        NamedEntity.date -> {
            val month = Month.values().random().getDisplayName(TextStyle.FULL, Locale.US)
            "$month ${(1..28).random()}, ${(1950..2020).random()}"
        }
        NamedEntity.location -> with(address()) { listOf(city(), cityName(), country(), state()).random() }
        NamedEntity.money -> "$${commerce().price()}"
        NamedEntity.organization -> company().name()
        NamedEntity.percentage -> {
            val integer = (1..100).random()
            if (integer == 100) "100%" else listOf("$integer%", "$integer.${(0..99).random()}%").random()
        }
        NamedEntity.person -> name().name()
        NamedEntity.time -> getRandomTime()
    }
}

/** Random 12 hour formatted time (e.g., `"03:47 PM"`). */
internal fun getRandomTime(): String = with(Calendar.getInstance().apply { time = Faker().date().birthday() }) {
    val format = { value: Int -> "${if (value < 10) "0" else ""}$value" }
    "${(format(get(Calendar.HOUR) + 1))}:${format(get(Calendar.MINUTE))} ${listOf("AM", "PM").random()}"
}