package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private inline fun assertStringIterator(it: RecursiveStringIterator, expected: String) {
    for (e in expected) {
        val a = it.getAndMove()
        assertEquals(e, a, "Asserting char in string recursive iterator")
    }
}

/**
 * Tests for [RecursiveStringIterator].
 * @author Andrew Golovashevich
 */
internal class RecursiveStringIteratorTest {
    @Test
    fun flat() {
        val s = "banana"
        val it = RecursiveStringIterator(s)
        assertStringIterator(it, s)
        assertEquals(s, it.slice(0u, s.length.toUInt()))
        assertFailsWith(NoSuchElementException::class) {
            it.get()
        }
    }

    @Test
    fun nestedLevel() {
        val s3 = "macro"
        val s2 = "before $s3"
        val s5 = " after"
        val s1 = "$s2$s5"
        val it = RecursiveStringIterator(s1)
        assertStringIterator(it, s2)
        assertEquals(s2, it.slice(0u, s2.length.toUInt()))
        val s4 = "inside"
        it.addExpansion(s4, s3.length.toUInt())
        assertFailsWith(IllegalArgumentException::class) {
            assertEquals(s2, it.slice(0u, 1u))
        }
        assertStringIterator(it, s4)
        assertStringIterator(it, s5)
        assertFailsWith(NoSuchElementException::class) {
            it.get()
        }
    }
}