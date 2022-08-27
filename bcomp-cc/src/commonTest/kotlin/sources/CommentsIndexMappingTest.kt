package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommentsIndexMappingTest : IndexMappingTest() {
    private class TestBuilderScope : IndexMappingTest.TestBuilderScope() {
        private fun interface TestAssertion {
            fun assert()
        }

        inner class StraightAssertion(
            private val virtualStart: UInt,
            private val realStart: UInt,
            private val length: UInt,
        ) : TestAssertion {
            override fun assert() {
                this@TestBuilderScope.assertStraightRegion(
                    this.virtualStart,
                    this.realStart,
                    this.length
                )
            }
        }

        inner class MappedAssertion(
            private val virtualStart: UInt,
            private val virtualLength: UInt,
            private val realStart: UInt,
            private val realLength: UInt,
        ) : TestAssertion {
            override fun assert() {
                this@TestBuilderScope.assertMappedRegion(
                    this.virtualStart,
                    this.virtualLength,
                    this.realStart,
                    this.realLength
                )
            }
        }

        override val mapping = CommentsIndexMapping()
        private var realPos = 0u
        private var virtualPos = 0u
        private val assertions = ArrayList<TestAssertion>()
        private val realSources = StringBuilder()
        private val virtualSources = StringBuilder()

        infix fun TestBuilderScope.source(text: () -> String): TestBuilderScope {
            val source = text()
            this.virtualSources.append(source)
            this.realSources.append(source)
            val length = source.length.toUInt()
            this.assertions.add(StraightAssertion(this.virtualPos, this.realPos, length))
            this.realPos += length
            this.virtualPos += length
            return this
        }

        infix fun TestBuilderScope.comment(text: () -> String): TestBuilderScope {
            val source = text()
            this.realSources.append(source)
            val length = source.length.toUInt()
            this.assertions.add(MappedAssertion(this.virtualPos, 0u, this.realPos, length))
            this.mapping.removeCommentRange(this.realPos, length)
            this.realPos += length
            return this
        }

        fun runAssertions() {
            for (assertion in this.assertions)
                assertion.assert()
            assertEquals(this.virtualSources.toString(), this.mapping.extractVirtualSources(this.realSources.toString()))
        }
    }

    private inline fun buildTest(builder: TestBuilderScope.() -> Unit) {
        val scope = TestBuilderScope()
        builder(scope)
        scope.runAssertions()
    }

    @Test
    fun noComments() = buildTest {
        source { "abc" } source { "def" } source { "ghi" }
    }

    @Test
    fun oneComment() = buildTest {
        source { "before" } comment { "/* comment */" } source { "after" }
    }

    @Test
    fun manyComments() = buildTest {
        source { "source" } comment { "/* comment */" } source { "source" } comment { "/* comment */" }
        source { "source" } comment { "/* comment */" } source { "source" }
    }

    @Test
    fun commentAtStart() = buildTest {
        comment { "/* comment */" } source { "source" }
    }

    @Test
    fun commentAtEnd() = buildTest {
        source { "source" } comment { "/* comment */" }
    }

    @Test
    fun commentAtStartAndEnd() = buildTest {
        comment { "/* comment */" } source { "source" } comment { "/* comment */" }
    }

    @Test
    fun commentAtStartAndMiddleAndEnd() = buildTest {
        comment { "/* comment */" } source { "source" } comment { "/* comment */" }
        source { "source" } comment { "/* comment */" }
    }

    @Test
    fun justOneComment() = buildTest {
        comment { "/* comment */" }
    }

    @Test
    fun manyCommentsNoSources() = buildTest {
        for (i in 0 until 100)
            comment { "/* comment */" }
    }

    @Test
    fun soManyComments() = buildTest {
        for (i in 0 until 20) {
            for (j in 0 until 20)
                source { "source" } comment { "/* comment */" }
            for (j in 0 until 20)
                comment { "/* comment */" }
            for (j in 0 until 20)
                comment { "/* comment */" } source { "source" }
        }
    }
}