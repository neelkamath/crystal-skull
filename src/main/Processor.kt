package com.neelkamath.crystalskull

import com.neelkamath.crystalskull.NLP.recognizeNamedEntities
import com.neelkamath.crystalskull.NLP.sense2vec
import com.neelkamath.crystalskull.NLP.sentencize

/** A section of text, such as one on the early life of Bill Gates. */
typealias ProcessedSection = List<ProcessedSentence>

/** [tokens] are the original [sentence]'s tokens. */
data class TokenizedSentence(val sentence: String, val tokens: List<String>)

/** [names] are the entities (each having the same [label]) fround in the [context]. */
data class ProcessedSentence(val context: ProcessedContext, val label: Label, val names: List<String>)

/** The original [sentence] processed, and if there was one, the sentence [previous] to it. */
data class ProcessedContext(val sentence: String, val previous: String? = null)

/** Tokenizes English [data]. */
fun tokenize(data: String): List<TokenizedSentence> = sentencize(data).map { TokenizedSentence(it, NLP.tokenize(it)) }

/**
 * Uses NER on an English [document].
 *
 * The [ProcessedContext.previous] sentence from each returned [ProcessedSentence] is the sentence prior to each
 * [TokenizedSentence] in the [document]. So if you are using the [ProcessedContext.previous] sentence, you must only
 * pass semantically related contents in the [document] (e.g., a single paragraph). Otherwise, the first sentence from
 * the previous paragraph would be used as in the [ProcessedContext] even though it isn't contextual.
 */
fun findNames(document: List<TokenizedSentence>): List<ProcessedSentence> {
    val sentences = document.map { it.sentence }
    val data = recognizeNamedEntities(sentences)
    return data.map { namedEntity ->
        val sentence = namedEntity.text
        val context = ProcessedContext(sentence, sentences.elementAtOrNull(sentences.indexOf(sentence) - 1))
        namedEntity.entities.filter { it.text != "%" }.groupBy { it.label }.mapValues { entity ->
            ProcessedSentence(context, entity.key, entity.value.map { it.text })
        }
    }.flatMap { it.values }
}

/**
 * (Slowly) computes phrases similar to the named [entity] in the [sentence].
 *
 * For example, if the [sentence] was `"Bill Gates created Microsoft."` and the [entity] was `"Microsoft"`, the
 * computed phrases may be `"Apple"` and `"Google"`.
 */
fun computeSimilarPhrases(sentence: String, entity: String): List<String> =
    cleanSense2vec(sense2vec(sentence, entity).map { it.phrase })

/** Removes dirty data from sense2vec output. */
fun cleanSense2vec(phrases: List<String>): List<String> =
    phrases.map { it.trim() }.filterNot { it.contains(Regex("&[lg]t;")) }.let { removeCaseInsensitiveDuplicates(it) }

/**
 * Case-insensitively removes duplicate [strings] (e.g., `listOf("Bill", "bill")` would become `listOf("Bill")`).
 *
 * If there are duplicate elements, the first one is taken.
 */
fun removeCaseInsensitiveDuplicates(strings: List<String>): List<String> =
    strings.fold(mutableListOf()) { list, string ->
        if (string.toLowerCase() !in list.map { it.toLowerCase() }) list.add(string)
        list
    }