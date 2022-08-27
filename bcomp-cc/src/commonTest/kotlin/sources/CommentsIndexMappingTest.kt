package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test

internal class CommentsIndexMappingTest : IndexMappingTest() {
    private class TestBuilderScope : IndexMappingTest.TestBuilderScope() {
        override val mapping = CommentsIndexMapping()
        private var pos = 0u

        infix fun TestBuilderScope.source(text: () -> String): TestBuilderScope {
            this.pos += text().length.toUInt()
            return this
        }

        infix fun TestBuilderScope.comment(text: () -> String): TestBuilderScope {
            val length = text().length.toUInt()
            this.mapping.removeCommentRange(this.pos, length)
            this.pos += length
            return this
        }
    }

    private inline fun buildTest(builder: TestBuilderScope.() -> Unit) {
        builder(TestBuilderScope())
    }

    @Test
    fun noComments() = buildTest {
        source { "abc" } source { "def" } source { "ghi" }

        assertStraightRegion(0u, 0u, 3u)
        assertStraightRegion(3u, 3u, 3u)
        assertStraightRegion(6u, 6u, 3u)
        assertStraightRegion(0u, 0u, 9u)
    }

    @Test
    fun oneComment() = buildTest {
        source { "before" } comment { "/* comment */" } source { "after" }

        assertStraightRegion(0u, 0u, 6u)
        assertMappedRegion(6u, 0u, 6u, 13u)
        assertStraightRegion(6u, 19u, 5u)
    }

    @Test
    fun manyComments() = buildTest {
        source { "0" } comment { "1" } source { "2" } comment { "3" } source { "4" } comment { "5" } source { "6" }

        assertStraightRegion(0u, 0u, 1u)
        assertMappedRegion(1u, 0u, 1u, 1u)
        assertStraightRegion(1u, 2u, 1u)
        assertMappedRegion(2u, 0u, 3u, 1u)
        assertStraightRegion(2u, 4u, 1u)
        assertMappedRegion(3u, 0u, 5u, 1u)
        assertStraightRegion(3u, 6u, 1u)
    }
}