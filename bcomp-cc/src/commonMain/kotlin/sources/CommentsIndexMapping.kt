package io.github.landgrafhomyak.itmo.bcomp_cc.sources

internal class CommentsIndexMapping : IndexMapping {
    private class CommentRange(
        val virtualStart: UInt,
        val offset: UInt,
        val length: UInt
    ) {
        inline val realStart get() = this.realEnd - this.length
        inline val realEnd get() = this.virtualStart + this.offset
    }

    private val ranges = ArrayList<CommentRange>()
    private var offsetAccumulator: UInt = 0u
    private var lastPosition: UInt = 0u

    private inline fun map(index: UInt): CommentRange? {
        var lo = -1
        var hi = this.ranges.size
        var mi: Int
        while (lo < hi - 1) {
            mi = (lo + hi) / 2
            if (index < this.ranges[mi].virtualStart) hi = mi
            else lo = mi
        }
        return if (lo == -1) null else this.ranges[lo]
    }

    override fun mapStart(index: UInt): UInt {
        val range = this.map(index)
        @Suppress("LiftReturnOrAssignment")
        if (range == null) return index
        else return index + range.offset
    }

    override fun mapEnd(index: UInt): UInt {
        val range = this.map(index)
        @Suppress("LiftReturnOrAssignment")
        if (range == null) return index
        else return index + range.offset
    }

    fun removeCommentRange(start: UInt, length: UInt) {
        if (start < this.lastPosition)
            throw IllegalArgumentException()
        this.ranges.add(CommentRange(start - this.offsetAccumulator, this.offsetAccumulator + length, length))
        this.offsetAccumulator += length
        this.lastPosition = start + length
    }

    fun extractVirtualSources(realSources: String): String {
        val dst = StringBuilder()
        var lastPos = 0u
        for (range in this.ranges) {
            dst.appendRange(realSources, lastPos.toInt(), range.realStart.toInt())
            lastPos = range.realEnd
        }
        dst.appendRange(realSources, lastPos.toInt(), realSources.length)
        return dst.toString()
    }
}