package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.assertEquals

internal abstract class IndexMappingTest {
    protected abstract class TestBuilderScope {
        protected abstract val mapping: IndexMapping

        fun assertStraightRegion(virtualStart: UInt, realStart: UInt, length: UInt) {
            for (offset in 0u until length) {
                assertEquals(realStart + offset, this.mapping.mapStart(virtualStart + offset))
                assertEquals(realStart + offset, this.mapping.mapEnd(virtualStart + offset))
            }
        }

        fun assertMappedRegion(virtualStart: UInt, virtualLength: UInt, realStart: UInt, realLength: UInt) {
            val realEnd = realStart + realLength
            for (offset in 0u until virtualLength) {
                assertEquals(realStart, this.mapping.mapStart(virtualStart + offset))
                assertEquals(realEnd, this.mapping.mapEnd(virtualStart + offset))
            }
        }
    }
}