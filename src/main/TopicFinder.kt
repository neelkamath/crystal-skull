package com.neelkamath.crystalskull

import com.neelkamath.kwikipedia.search
import com.neelkamath.kwikipedia.searchMostViewed
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Returns a random Wikipedia article's title (usually one which is trending in the last day).
 *
 * [searchMostViewed] occasionally returns zero search results for random long periods of time due to a Wikipedia bug.
 * To ensure an article title is always returned, we fall back to [search]ing a completely random topic.
 */
suspend fun getRandomTopic(): String =
    searchMostViewed().let { if (it.isEmpty()) search()[0].title else it.random().title }

/** Finds topics related to [sections] on Wikipedia (ordered with the most relevant first). */
suspend fun findRelatedTopics(sections: List<String>): List<String> {
    val entities = findRelatedEntities(sections)
    return entities
        .associateWith { entity ->
            entities.count { it == entity }
        }
        .toList()
        .sortedByDescending { it.second }
        .map { it.first }
        .filter { entity ->
            search(entity).let { it.isNotEmpty() && it[0].title == entity }
        }
}

/** Finds named entities in [sections] which are labelled with [relatedTopicLabels]. */
private suspend fun findRelatedEntities(sections: List<String>): List<String> {
    val tokenizedSections = coroutineScope {
        sections
            .map {
                async { tokenize(it) }
            }
            .awaitAll()
            .flatten()
    }
    return findNames(tokenizedSections.map { it.sentence })
        .filter { it.label in relatedTopicLabels }
        .flatMap { it.names }
}

private val relatedTopicLabels = listOf(
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