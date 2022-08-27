package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.jvm.JvmStatic

internal interface IndexMapping {
    fun mapStart(index: UInt): UInt
    fun mapEnd(index: UInt): UInt

    interface MappedPosLength {
        var start: UInt
        var length: UInt
    }

    fun mapPosAndLength(dst: MappedPosLength): MappedPosLength {
        val start = dst.start
        dst.start = this.mapStart(dst.start)
        dst.length = this.mapEnd(start + dst.length) - dst.start
        return dst
    }

    interface MappedPosPos {
        var start: UInt
        var end: UInt
    }

    fun mapPosAndPos(dst: MappedPosPos): MappedPosPos {
        dst.start = this.mapStart(dst.start)
        dst.end = this.mapEnd(dst.end)
        return dst
    }

    fun interface ConsumerPosLength<R> {
        fun consume(pos: UInt, length: UInt): R
    }

    fun interface ConsumerPosPos<R> {
        fun consume(pos: UInt, Pos: UInt): R
    }

    companion object {
        @JvmStatic
        inline fun MappedPosLength(start: UInt, length: UInt): MappedPosLength = object : MappedPosLength {
            override var start: UInt = start
            override var length: UInt = length
        }

        @JvmStatic
        inline fun MappedPosPos(start: UInt, end: UInt): MappedPosPos = object : MappedPosPos {
            init {
                checkStartEnd(start, end)
            }

            override var start: UInt = start
            override var end: UInt = end
        }
    }
}