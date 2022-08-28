package io.github.landgrafhomyak.itmo.bcomp_cc.sources

internal interface CommentsParserScope: ParserScope {
    fun removeComment(start: UInt, length: UInt)
}
class CommentsParserScopeImpl(
    private var realSources: String,
    entitiesConsumer: MutableList<Entity>
) : ParserScope {
    private val entities: MutableList<Entity> = entitiesConsumer
    private var realPos: UInt = 0u
    override val pos: UInt by this::realPos
    private val mapping = CommentsIndexMapping()
    internal val outMapping: IndexMapping by this::mapping

    override fun currentChar(): Char {
        if (this.realPos >= this.realSources.length.toUInt())
            throw SourceEndedSignal()
        return this.realSources[this.realPos.toInt()]
    }

    override fun getCharAndMoveNext(): Char {
        val c = this.currentChar()
        this.realPos++
        return c
    }

    override fun moveNext() {
        if (this.realPos < this.realSources.length.toUInt())
            this.realPos++
    }

    override fun isEnded(): Boolean = this.realPos >= this.realSources.length.toUInt()

    override fun addEntity(entity: Entity) {
        this.entities.add(entity)
    }

    override fun slice(start: UInt, end: UInt): String =
        this.realSources.slice(start.toInt() until end.toInt())

    fun removeComment(start: UInt, length: UInt) {
        this.mapping.removeCommentRange(start, length)
    }

    fun buildVirtualSources(): String = this.mapping.extractVirtualSources(this.realSources)
}