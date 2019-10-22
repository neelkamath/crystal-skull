package com.neelkamath.crystalskull

import java.io.FileInputStream

/** A document useful for enhancing the accuracy of a [NameFinderME]. */
internal typealias Document = List<TokenizedSentence>

/** A section of text (e.g., the early life of Bill Gates). */
internal typealias ProcessedSection = List<ProcessedSentence>

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
            list.addAll(findNames(document, entity, finder))
            finder.clearAdaptiveData()
        }
        return list
    }

    /**
     * Finds [entity]s in an English [document] (sentences without [entity]s will be discarded).
     *
     * If you are finding names in [Document]s, you can supply your own [finder]. This way you can enhance the results
     * by calling [NameFinderME.clearAdaptiveData] after each finding.
     */
    @Synchronized
    internal fun findNames(
        document: Document,
        entity: NamedEntity,
        finder: NameFinderME = nameFinders.getValue(entity).value
    ): List<ProcessedSentence> {
        val list = mutableListOf<ProcessedSentence>()
        for ((index, sentence) in document.withIndex()) {
            finder.find(sentence.tokens.toTypedArray()).filter { it.prob >= .9 }.takeIf { it.isNotEmpty() }.let {
                list.add(process(it, sentence, document.elementAtOrNull(index - 1)?.sentence))
            }
        }
        return list
    }

    /**
     * Converts the [spans] belonging to the same [Span.type] of a [tokenizedSentence].
     *
     * If there was one, include the [previous] sentence to [tokenizedSentence].
     */
    private fun process(spans: List<Span>, tokenizedSentence: TokenizedSentence, previous: String?): ProcessedSentence {
        if (spans.any { it.type != spans[0].type }) throw Error("All <spans> must have the same <Span.type>")
        return ProcessedSentence(
            ProcessedContext(tokenizedSentence.sentence, previous),
            NamedEntity.valueOf(spans[0].type),
            spans.map { detokenizeNames(tokenizedSentence.tokens, it) }
        )
    }

    /** Gets the entity [span]ned in [tokens]. */
    private fun detokenizeNames(tokens: List<String>, span: Span): String = tokens
        .slice(span.start until span.end)
        .joinToString(" ")
        .let { if (it.endsWith(" .")) it.replace(Regex("""( \.)$"""), ".") else it }
        .replace(" '", "'")
        .replace(" , ", ", ")
        .replace(" %", "%")
        .replace("( ", "(")
        .let { if (span.type == "money" && isSymbol(it)) it.replaceFirst(" ", "") else it }

    private fun isSymbol(string: String) = with(string.split(" ")[0]) { length == 1 && !matches(Regex("""^(\w|\d)""")) }
}