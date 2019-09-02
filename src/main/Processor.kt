package com.neelkamath.crystalskull

import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.Span
import java.io.FileInputStream

/** A document useful for enhancing the accuracy of a [NameFinderME]. */
internal typealias Document = List<TokenizedSentence>

/** [tokens] are the original [sentence]'s tokens. */
internal data class TokenizedSentence(val sentence: String, val tokens: List<String>)

/** [names] are the entities (each being an [entity]) fround in the [context]. */
internal data class ProcessedSentence(val context: ProcessedContext, val entity: NamedEntity, val names: List<String>)

/** The original [sentence] processed, and if there was one, the sentence [previous] to it. */
internal data class ProcessedContext(val sentence: String, val previous: String? = null)

object Tokenizer {
    /** English tokenizer. */
    private val tokenizer: Lazy<TokenizerME> =
        lazy { TokenizerME(TokenizerModel(FileInputStream("src/main/resources/en-token.bin"))) }
    /** English sentence detector. */
    private val sentenceDetector: Lazy<SentenceDetectorME> =
        lazy { SentenceDetectorME(SentenceModel(FileInputStream("src/main/resources/en-sent.bin"))) }

    /** Runs an English tokenizer on [data]. */
    @Synchronized
    internal fun tokenize(data: String): List<TokenizedSentence> =
        sentenceDetector.value.sentDetect(data).map { TokenizedSentence(it, tokenizer.value.tokenize(it).toList()) }
}

object NameFinder {
    /** English name finders. */
    private val nameFinders: Map<NamedEntity, Lazy<NameFinderME>> = NamedEntity.values().associate {
        it to lazy { getNameFinder(it) }
    }

    /** English name finder for [entity]. */
    private fun getNameFinder(entity: NamedEntity): NameFinderME =
        NameFinderME(TokenNameFinderModel(FileInputStream("src/main/resources/en-ner-$entity.bin")))

    /** Parses English [documents] to find [entity]s. Sentences without [entity]s will be discarded. */
    @Synchronized
    internal fun findNames(documents: List<Document>, entity: NamedEntity): List<ProcessedSentence> {
        val list = mutableListOf<ProcessedSentence>()
        val finder = nameFinders.getValue(entity).value
        for (document in documents) {
            for ((index, tokenizedSentence) in document.withIndex()) {
                val spans = finder.find(tokenizedSentence.tokens.toTypedArray()).filter { it.prob >= .9 }
                if (spans.isNotEmpty()) {
                    list.add(process(spans, tokenizedSentence, document.elementAtOrNull(index - 1)?.sentence))
                }
            }
            finder.clearAdaptiveData()
        }
        return list
    }

    /**
     * Converts the [spans] belonging to the same [Span.type] of a [tokenizedSentence].
     *
     * If there was one, include the [previous] sentence to [tokenizedSentence].
     */
    private fun process(spans: List<Span>, tokenizedSentence: TokenizedSentence, previous: String?): ProcessedSentence =
        ProcessedSentence(
            ProcessedContext(tokenizedSentence.sentence, previous),
            NamedEntity.valueOf(spans[0].type),
            spans.map { span ->
                tokenizedSentence
                    .tokens
                    .slice(span.start until span.end)
                    .joinToString(" ")
                    .let { if (it.endsWith(" .")) it.replace(Regex("""( \.)$"""), ".") else it }
                    .replace(" '", "'")
                    .replace(" , ", ", ")
                    .replace(" %", "%")
                    .let { if (spans[0].type == "money" && isSymbol(it)) it.replaceFirst(" ", "") else it }
            }
        )

    private fun isSymbol(string: String) = with(string.split(" ")[0]) { length == 1 && !matches(Regex("""^(\w|\d)""")) }
}