package com.neelkamath.crystalskull

import com.github.javafaker.Faker
import java.time.Month
import java.time.format.TextStyle
import java.util.*

/**
 * Maps [sentences] to the [QuizQuestion]s generated (using [entities]) from them.
 *
 * Questions on dates may contain options lacking years (e.g., `June 7` instead of `June 7, 2000`). Since such questions
 * may be unnecessarily difficult and/or irrelevant to a person's study, options lacking years will be disallowed by
 * default.
 */
internal fun createQuestions(
    sentences: List<ProcessedSentence>,
    entities: List<NamedEntity>,
    allowSansYears: Boolean = false
): Map<ProcessedSentence, List<QuizQuestion>> = entities
    .associateWith { entity ->
        sentences.filter { it.entity == entity }.flatMap { it.names }.toSet()
    }
    .let { namedOptions ->
        sentences.associateWith { processed ->
            processed.names
                .filter { if (processed.entity == NamedEntity.date && !allowSansYears) containsYear(it) else true }
                .map { answer ->
                    QuizQuestion(
                        ask(processed, CorrectOption(namedOptions.getValue(processed.entity), answer), allowSansYears),
                        processed.entity
                    )
                }
        }
    }

/**
 * Creates a [QuestionAnswer] from [processed].
 *
 * If there aren't enough [CorrectOption.options], fake [ProcessedSentence.entity]s will be generated.
 */
private fun ask(
    processed: ProcessedSentence,
    correctOption: CorrectOption,
    allowSansYears: Boolean = false
): QuestionAnswer = QuestionAnswer(
    QuestionContext(processed.context.sentence.replaceFirst(correctOption.answer, "_____"), processed.context.previous),
    (correctOption.options.shuffled().take(3) + correctOption.answer)
        .let { options ->
            var set = filterOptions(correctOption.copy(options = options.toSet()), processed.entity, allowSansYears)
                .toMutableSet()
            while (set.size < 4) {
                set.add(getRandomEntity(processed.entity))
                set = filterOptions(correctOption.copy(options = set), processed.entity, allowSansYears).toMutableSet()
            }
            set
        }
        .shuffled()
        .toSet(),
    correctOption.answer
)

/** One or more [options], of which one is the correct [answer]. */
private data class CorrectOption(val options: Set<String>, val answer: String) {
    init {
        if (answer !in options) throw Error("<answer> must be in <options>: $this")
    }
}

/**
 * Removes duplicate options from the [correctOption].
 *
 * If [entity] is a [NamedEntity.date], options sans years will be removed if you don't [allowSansYears] (e.g.,
 * `June 7` is an option sans a year, but `June 7, 2000` isn't).
 */
private fun filterOptions(
    correctOption: CorrectOption,
    entity: NamedEntity,
    allowSansYears: Boolean = false
): Set<String> {
    var set = correctOption.options.toMutableSet()
    if (entity in listOf(NamedEntity.location, NamedEntity.organization, NamedEntity.person)) {
        set = removeDuplicates(set, correctOption.answer).toMutableSet()
    }
    if (entity != NamedEntity.date) return set
    return if (allowSansYears) set else set.filter { containsYear(it) }.toSet()
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

/** Generates a fake name for an [entity]. */
internal fun getRandomEntity(entity: NamedEntity): String = with(Faker()) {
    when (entity) {
        NamedEntity.date -> {
            val month = Month.values().random().getDisplayName(TextStyle.FULL, Locale.US)
            "$month ${(1..28).random()}, ${(1950..2020).random()}"
        }
        NamedEntity.location -> with(address()) { listOf(city(), cityName(), country(), state()).random() }
        NamedEntity.money -> "$${commerce().price()}"
        NamedEntity.organization -> company().name()
        NamedEntity.percentage -> (1..100).random().let { listOf("$it%", "$it.${(0..99).random()}%").random() }
        NamedEntity.person -> name().name()
        NamedEntity.time -> getRandomTime()
    }
}

/** Returns a random 12 hour formatted time (e.g., `"03:47 PM"`). */
internal fun getRandomTime(): String = with(Calendar.getInstance().apply { time = Faker().date().birthday() }) {
    val format = { value: Int -> "${if (value < 10) "0" else ""}$value" }
    "${(format(get(Calendar.HOUR) + 1))}:${format(get(Calendar.MINUTE))} ${listOf("AM", "PM").random()}"
}