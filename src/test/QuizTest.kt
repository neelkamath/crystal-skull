package com.neelkamath.crystalskull.test

import com.neelkamath.crystalskull.getRandomEntity
import com.neelkamath.crystalskull.getRandomTime
import kotlin.test.Test
import kotlin.test.assertTrue

class EntityGeneratorTest {
    @Test
    fun `Randomly generated date entities must look real`() = getRandomEntity("date").let {
        val month = Regex("""(January|February|March|April|May|June|July|August|September|October|November|December)""")
        assertTrue(it.matches(Regex("""$month ([1-9]|[12][0-9]|3[01]), \d\d\d\d""")), "Actual: $it")
    }

    @Test
    fun `Randomly generated percentages must look real`() = repeat(5) {
        getRandomEntity("percentage").let {
            assertTrue(it.matches(Regex("""\d{1,2}%""")) || it.matches(Regex("""\d{1,2}\.\d{1,2}%""")), "Actual: $it")
        }
    }
}

class TimeGeneratorTest {
    @Test
    fun `Randomly generated times must look realistic`() = getRandomTime().let {
        assertTrue(it.matches(Regex("""\d\d:\d\d [AP]M""")), "Actual: $it")
    }
}