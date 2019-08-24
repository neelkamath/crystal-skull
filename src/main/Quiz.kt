package com.neelkamath.crystalskull

import com.github.javafaker.Faker
import java.time.Month
import java.time.format.TextStyle
import java.util.*

/** Maps [sentences] to the [QuizQuestion]s generated (using [entities]) from them. */
internal fun createQuestions(
    sentences: List<ProcessedSentence>,
    entities: List<NamedEntity>
): Map<ProcessedSentence, List<QuizQuestion>> = entities
    .associateWith { entity -> sentences.filter { it.entity == entity }.map { it.names }.flatten().toSet() }
    .let { namedOptions ->
        sentences.associateWith { processed ->
            processed.names.map { answer ->
                QuizQuestion(ask(processed, namedOptions.getValue(processed.entity), answer), processed.entity)
            }
        }
    }

/**
 * Creates a question and [answer] from [processed].
 *
 * If there aren't enough [options], fake [ProcessedSentence.entity]s will be generated.
 */
private fun ask(processed: ProcessedSentence, options: Set<String>, answer: String): QuestionAnswer = QuestionAnswer(
    QuestionContext(processed.context.sentence.replaceFirst(answer, "_____"), processed.context.previous),
    (options.shuffled().take(3) + answer)
        .let {
            var set = removeDuplicates(it.toSet(), answer).toMutableSet()
            while (set.size < 4) {
                set.add(getRandomEntity(processed.entity))
                set = removeDuplicates(set, answer).toMutableSet()
            }
            set
        }
        .shuffled()
        .toSet(),
    answer
)

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
        NamedEntity.percentage -> {
            val integer = (1..100).random()
            listOf("$integer%", "$integer.${(0..99).random()}%").random()
        }
        NamedEntity.person -> name().name()
        NamedEntity.time -> getRandomTime()
    }
}

/** Returns a random 12 hour formatted time (e.g., `"03:47 PM"`). */
internal fun getRandomTime(): String = with(Calendar.getInstance().apply { time = Faker().date().birthday() }) {
    val format = { value: Int -> "${if (value < 10) "0" else ""}$value" }
    "${(format(get(Calendar.HOUR) + 1))}:${format(get(Calendar.MINUTE))} ${listOf("AM", "PM").random()}"
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
internal fun removeDuplicates(strings: Set<String>, sought: String): Set<String> {
    val splits = strings.map { it.split(" ") }
    val unique = mutableSetOf(sought)
    val soughtSplit = sought.split(" ")
    for (split in splits) {
        if (split != soughtSplit && !soughtSplit.containsAll(split) && !split.containsAll(soughtSplit)) {
            unique.add(split.joinToString(" "))
        }
    }
    return unique
}