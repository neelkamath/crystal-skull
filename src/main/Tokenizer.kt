package com.neelkamath.crystalskull

import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.Span
import java.io.FileInputStream

/** [tokens] are the original [sentence]'s tokens. */
internal data class TokenizedSentence(val sentence: String, val tokens: List<String>)

/** A document useful for enhancing the accuracy of a [NameFinderME]. */
internal typealias Document = List<TokenizedSentence>

/** [names] are the entities (each being an [entity]) fround in the original [sentence]. */
internal data class ProcessedSentence(val sentence: String, val entity: NamedEntity, val names: List<String>)

/** Runs an English tokenizer on [data]. */
internal fun tokenize(data: String): List<TokenizedSentence> =
    SentenceDetectorME(SentenceModel(FileInputStream("src/main/resources/en-sent.bin"))).sentDetect(data).map {
        TokenizedSentence(
            it, TokenizerME(TokenizerModel(FileInputStream("src/main/resources/en-token.bin"))).tokenize(it).toList()
        )
    }

/** Parses English [documents] to find [entity]s. Sentences without [entity]s will be discarded. */
internal fun findNames(documents: List<Document>, entity: NamedEntity): List<ProcessedSentence> =
    mutableListOf<ProcessedSentence>().also { list ->
        val finder = NameFinderME(TokenNameFinderModel(FileInputStream("src/main/resources/en-ner-$entity.bin")))
        for (document in documents) {
            for (tokenizedSentence in document) {
                val spans = finder.find(tokenizedSentence.tokens.toTypedArray()).filter { it.prob >= .9 }
                if (spans.isNotEmpty()) list.add(process(tokenizedSentence, spans))
            }
            finder.clearAdaptiveData()
        }
    }

/** Converts the [spans] belonging to the same [Span.type] of a [tokenizedSentence]. */
private fun process(tokenizedSentence: TokenizedSentence, spans: List<Span>): ProcessedSentence = ProcessedSentence(
    tokenizedSentence.sentence,
    NamedEntity.valueOf(spans[0].type),
    spans.map { span ->
        tokenizedSentence
            .tokens
            .slice(span.start until span.end)
            .joinToString(" ")
            .let {
                if (it.endsWith(" .")) return@let it.replace(Regex("""( \.)$"""), ".")
                it
            }
            .replace(" '", "'")
            .replace(" , ", ", ")
            .replace(" %", "%")
            .let { if (spans[0].type == "money" && isSymbol(it)) it.replaceFirst(" ", "") else it }
    }
)

private fun isSymbol(string: String) = with(string.split(" ")[0]) { length == 1 && !matches(Regex("""^(\w|\d)""")) }