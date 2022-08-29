package io.github.landgrafhomyak.itmo.bcomp_cc.sources

class InjectableStringBuilder(realSources: String) {
    private var string = realSources.toCharArray()
    var virtualPos: UInt = 0u
        private set
    var line: UInt = 0u
    private var virtualPosOfNextRealSources = 0u

    fun exportVirtualSources() = this.string.concatToString()

    fun injectMacro(start: UInt, length: UInt, expansion: String) {
        val newString = CharArray(this.string.size - length.toInt() + expansion.length)
        this.string.copyInto(
            destination = newString,
            destinationOffset = 0,
            startIndex = 0,
            endIndex = start.toInt()
        )
        expansion.toCharArray().copyInto(
            destination = newString,
            destinationOffset = start.toInt()
        )
        this.string.copyInto(
            destination = newString,
            destinationOffset = start.toInt() + expansion.length,
            startIndex = (start + length).toInt(),
            endIndex = this.string.size
        )
        this.string = newString
        this.virtualPos = start
        if (start > this.virtualPosOfNextRealSources)
            this.virtualPosOfNextRealSources = start + expansion.length.toUInt()
        else
            this.virtualPosOfNextRealSources = this.virtualPosOfNextRealSources - length + expansion.length.toUInt()
    }

    fun currentChar() = this.string[this.virtualPos.toInt()]
    fun getCharAndMoveNext(): Char {
        val c = this.string[this.virtualPos.toInt()]
        if (c == '\n' && this.virtualPos > this.virtualPosOfNextRealSources)
            ++this.line
        ++this.virtualPos
        return c
    }

    fun moveNext() {
        this.getCharAndMoveNext()
    }

    fun slice(start: UInt, length: UInt) = this.string.concatToString(start.toInt(), (start + length).toInt())
}