package com.neelkamath.crystalskull

import com.github.javafaker.Faker
import java.time.Month
import java.time.format.TextStyle
import java.util.*

/** Options lacking years may be included if you [allowSansYears]. */
class Quizmaster(private val allowSansYears: Boolean = false) {
    /** One or more [options], of which one is the correct [answer]. There are more [relevantOptions] than [options]. */
    private data class CorrectOption(val relevantOptions: Set<String>, val options: Set<String>, val answer: String) {
        init {
            if (answer !in relevantOptions + options) throw Error("<answer> must be in <options>: $this")
            val intersection = relevantOptions.intersect(options)
            if (intersection.isNotEmpty())
                throw Error("<relevantOptions> and <options> must be disjoint (intersection: $intersection)")
        }
    }

    /** Creates [QuizQuestion]s out of [sections]. */
    fun quiz(sections: List<ProcessedSection>): Map<ProcessedSentence, List<QuizQuestion>> {
        val map = mutableMapOf<ProcessedSentence, List<QuizQuestion>>()
        val options = createOptions(sections)
        for ((index, section) in sections.withIndex())
            map.putAll(section.associateWith { createQuestions(it, options, index) })
        return map
    }

    /** Map of options whose values are the entities found in each of the [sections]. */
    private fun createOptions(sections: List<ProcessedSection>): Map<Label, List<List<String>>> =
        Label.values().toList().associateWith { entity ->
            sections.map { section ->
                section.filter { it.label == entity }.flatMap { it.names }
            }
        }

    /** The [index] indicates which [options] are more relevant to the [sentence]. */
    private fun createQuestions(
        sentence: ProcessedSentence,
        options: Map<Label, List<List<String>>>,
        index: Int
    ): List<QuizQuestion> = sentence.names
        .filter { if (sentence.label == Label.DATE && !allowSansYears) it.containsYear() else true }
        .map {
            val possibleOptions = options.getValue(sentence.label)
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
                getOptions(label, correctOption),
                context.sentence.indexOf(correctOption.answer).let {
                    AnswerOffset(it, it + correctOption.answer.length)
                },
                label,
                context.previous
            )
        }

    /** Gives four options from the [correctOption]. [entity] options will be generated if required. */
    private fun getOptions(entity: Label, correctOption: CorrectOption): Set<String> =
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
     * If [entity] is a [Label.DATE], options sans years will be removed if you don't [allowSansYears] (e.g.,
     * `June 7` is an option sans a year, but `June 7, 2000` isn't).
     */
    private fun filterOptions(options: Set<String>, keep: String, entity: Label): Set<String> {
        var set = options.toMutableSet()
        if (entity in listOf(
                Label.PERSON,
                Label.NORP,
                Label.FAC,
                Label.ORG,
                Label.GPE,
                Label.LOC,
                Label.PRODUCT,
                Label.EVENT,
                Label.WORK_OF_ART,
                Label.LAW,
                Label.LANGUAGE
            )
        ) set = removeDuplicates(set, keep).toMutableSet()
        if (entity != Label.DATE) return set
        return if (allowSansYears) set else set.filter { it.containsYear() }.toSet()
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
fun removeDuplicates(strings: Set<String>, sought: String): Set<String> = strings
    .map { it.split(" ") }
    .filter {
        val soughtSplit = sought.split(" ")
        it != soughtSplit && !soughtSplit.containsAll(it) && !it.containsAll(soughtSplit)
    }
    .map { it.joinToString(" ") }
    .toSet() + sought

/** Whether it contains a year (a four-digit number). */
private fun String.containsYear() = contains(Regex("""\d\d\d\d"""))

/** Generates a fake [entity]. */
private fun getRandomEntity(entity: Label): String = with(Faker()) {
    when (entity) {
        Label.PERSON, Label.EVENT -> name().name() // Named hurricanes, etc. are labeled events, which look like names.
        Label.NORP -> demographic().demonym()
        Label.FAC, Label.GPE, Label.LOC -> with(address()) { listOf(city(), cityName(), country(), state()).random() }
        Label.ORG -> company().name()
        Label.PRODUCT -> listOf(food().spice(), food().ingredient(), music().instrument()).random()
        Label.WORK_OF_ART -> listOf(book().title(), rockBand().name()).random()
        Label.LAW -> book().title() // Laws are documents made into laws, which look like books.
        Label.LANGUAGE ->
            /*
             If a question's options already contain a few of these languages, they will be removed as duplicates. This
             will cause a shortage of options for language questions. Hence, there must be several languages which may
             be chosen. This will also increase the apparent randomness of the options shown across language questions.
             */
            listOf("English", "Tamil", "French", "Italian", "Japanese", "Chinese", "Arabic", "Armenian").random()
        Label.DATE -> {
            val month = Month.values().random().getDisplayName(TextStyle.FULL, Locale.US)
            "$month ${(1..28).random()}, ${(1950..2020).random()}"
        }
        Label.TIME -> with(Calendar.getInstance().apply { time = Faker().date().birthday() }) {
            val format = { value: Int -> "${if (value < 10) "0" else ""}$value" }
            "${(format(get(Calendar.HOUR) + 1))}:${format(get(Calendar.MINUTE))} ${listOf("AM", "PM").random()}"
        }
        Label.PERCENT -> {
            val integer = (1..100).random()
            if (integer == 100) "100%" else listOf("$integer%", "$integer.${(0..99).random()}%").random()
        }
        Label.MONEY -> "$${commerce().price()}"
        Label.QUANTITY ->
            listOf(space().distanceMeasurement(), "${(1..1000).random()} ${listOf("kg", "lb").random()}").random()
        Label.ORDINAL, Label.CARDINAL -> number().digit()
    }
}