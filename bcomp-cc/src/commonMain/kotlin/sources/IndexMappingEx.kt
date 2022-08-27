@file:JvmName("IndexMappingEx")

package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.jvm.JvmName

internal inline operator fun IndexMapping.MappedPosLength.component1(): UInt = this.start
internal inline operator fun IndexMapping.MappedPosLength.component2(): UInt = this.length

internal inline operator fun IndexMapping.MappedPosPos.component1(): UInt = this.start
internal inline operator fun IndexMapping.MappedPosPos.component2(): UInt = this.end

internal inline fun checkStartEnd(start: UInt, end: UInt) {
    if (end < start) throw IllegalArgumentException("'end' must be greater or equal to 'start'")
}

internal inline fun IndexMapping.mapPosAndLength(start: UInt, length: UInt): IndexMapping.MappedPosLength {
    val dst = object : IndexMapping.MappedPosLength {
        override var start: UInt = start
        override var length: UInt = length
    }
    return this.mapPosAndLength(dst)
}

internal inline fun IndexMapping.mapPosAndPos(start: UInt, end: UInt): IndexMapping.MappedPosPos {
    val dst = object : IndexMapping.MappedPosPos {
        override var start: UInt = start
        override var end: UInt = end
            set(value) {
                if (value < this.start) throw IllegalArgumentException("'end' must be greater or equal to 'start'")
                field = value
            }
    }
    return this.mapPosAndPos(dst)
}


internal inline fun <R> IndexMapping.mapPosAndLength(start: UInt, length: UInt, consumer: IndexMapping.ConsumerPosLength<R>): R {
    val data = this.mapPosAndLength(start, length)
    return consumer.consume(data.start, data.length)
}

internal inline fun <R> IndexMapping.mapPosAndPos(start: UInt, length: UInt, consumer: IndexMapping.ConsumerPosPos<R>): R {
    val data = this.mapPosAndPos(start, length)
    return consumer.consume(data.start, data.end)
}

internal inline fun IndexMapping.MappedPosLength.toMappedPosPos() = object : IndexMapping.MappedPosPos {
    override var start: UInt = this@toMappedPosPos.start
    override var end: UInt = this.start + this@toMappedPosPos.length
        set(value) {
            checkStartEnd(this.start, value)
            field = value
        }
}

internal inline fun IndexMapping.MappedPosPos.toMappedPosLength() = object : IndexMapping.MappedPosLength {
    override var start: UInt = this@toMappedPosLength.start
    override var length: UInt

    init {
        val end = this@toMappedPosLength.end
        checkStartEnd(this.start, end)
        this.length = end - this.start
    }
}

private class MappedPosPosBoundToMappedPosLength
constructor(
    val _source: IndexMapping.MappedPosLength
) : IndexMapping.MappedPosPos {
    override var start: UInt
        get() = this._source.start
        set(value) {
            this._source.start = value
        }
    override var end: UInt
        get() = this._source.start + this._source.length
        set(value) {
            checkStartEnd(this._source.start, end)
            this._source.length = value - this._source.start
        }
}

internal fun IndexMapping.MappedPosLength.asMappedPosPos(): IndexMapping.MappedPosPos {
    if (this is MappedPosLengthBoundToMappedPosPos) return this._source
    return MappedPosPosBoundToMappedPosLength(this)
}

private class MappedPosLengthBoundToMappedPosPos
constructor(
    val _source: IndexMapping.MappedPosPos
) : IndexMapping.MappedPosLength {
    override var start: UInt
        get() = this._source.start
        set(value) {
            checkStartEnd(value, this._source.end)
            this._source.start = value
        }
    override var length: UInt
        get() = this._source.end - this._source.start
        set(value) {
            this._source.end = this._source.end + value
        }
}

internal fun IndexMapping.MappedPosPos.asMappedPosLength(): IndexMapping.MappedPosLength {
    if (this is MappedPosPosBoundToMappedPosLength) return this._source
    return MappedPosLengthBoundToMappedPosPos(this)
}
