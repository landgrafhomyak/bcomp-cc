package io.github.landgrafhomyak.itmo.bcomp_cc.sources

internal interface IndexMapping {
    fun mapStart(index: UInt): UInt
    fun mapEnd(index: UInt): UInt

    interface MappedPosLength {
        var start: UInt
        var length: UInt
    }

    fun mapPosAndLength(dist: MappedPosLength): MappedPosLength {
        val start = dist.start
        dist.start = this.mapStart(dist.start)
        dist.length = this.mapEnd(start + dist.length) - dist.start
        return dist
    }

    interface MappedPosPos {
        var start: UInt
        var end: UInt
    }

    fun mapPosAndPos(dist: MappedPosPos): MappedPosPos {
        dist.start = this.mapStart(dist.start)
        dist.end = this.mapEnd(dist.end)
        return dist
    }

    fun interface ConsumerPosLength<R> {
        fun consume(pos: UInt, length: UInt): R
    }

    fun interface ConsumerPosPos<R> {
        fun consume(pos: UInt, Pos: UInt): R
    }
}