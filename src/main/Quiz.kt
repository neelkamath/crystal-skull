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
    processed.sentence.replaceFirst(answer, "_____"),
    (options.shuffled().take(3) + answer)
        .toMutableSet()
        .apply { while (size < 4) add(getRandomEntity(processed.entity)) }
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

/** Returns a random time (e.g., `"03:47 PM"`). */
internal fun getRandomTime(): String = with(Calendar.getInstance().apply { time = Faker().date().birthday() }) {
    val format = { value: Int -> "${if (value < 10) "0" else ""}$value" }
    "${(get(Calendar.HOUR) + 1).let(format)}:${get(Calendar.MINUTE).let(format)} ${listOf("AM", "PM").random()}"
}