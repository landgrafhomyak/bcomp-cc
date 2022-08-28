package io.github.landgrafhomyak.itmo.bcomp_cc.sources

internal interface ParserScope {
    val pos: UInt

    @Throws(SourceEndedSignal::class)
    fun currentChar(): Char

    @Throws(SourceEndedSignal::class)
    fun getCharAndMoveNext(): Char

    @Throws(SourceEndedSignal::class)
    fun moveNext()
    fun isEnded(): Boolean
    fun addEntity(entity: Entity)
    fun slice(start: UInt, length: UInt): String
}