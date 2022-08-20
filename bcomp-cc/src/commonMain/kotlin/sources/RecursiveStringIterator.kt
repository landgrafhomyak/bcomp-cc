package io.github.landgrafhomyak.itmo.bcomp_cc.sources

/**
 * One of classes, that allows running [C preprocessor](https://en.wikipedia.org/wiki/C_preprocessor) inline
 * while parsing sources.
 * Provides string iterator functionality with virtual expanding and slices.
 *
 * Example:
 * ```c
 * #define Kb * 1024
 * #define Mb Kb Kb
 *
 * 64 Mb + 16
 * ```
 *
 * Order of chars returning and [their virtual indices][MacroMappings] (arrows `\-^v!|` breaks `.`):
 * ```
 *            * 1024!* 1024!
 *            ^     |^     |
 *            |     ||     |
 *            \-\   v\-\   v
 *            Kb!... Kb!   !
 *            ^            |
 *            |            |
 *            \-\          v
 * string: 64 Mb!.......... + 16
 * ```
 * @param topLvl Real string before any injections.
 * @see MacroMappings
 * @see ParserScope
 * @author Andrew Golovashevich
 */
internal class RecursiveStringIterator(topLvl: String) : Iterator<Char> {
    /**
     * Level of injection. Similar to string iterator, but without loosing iterated string.
     * @author Andrew Golovashevich
     */
    private class Level(
        /**
         * String to iterate.
         * @author Andrew Golovashevich
         */
        val string: String
    ) {
        /**
         * Iterator position.
         * @author Andrew Golovashevich
         */
        var pos: UInt = 0u

        /**
         * Length of string.
         * @author Andrew Golovashevich
         */
        inline val length: UInt get() = this.string.length.toUInt()
    }

    /**
     * Macro expanding stack.
     * @author Andrew Golovashevich
     */
    @Suppress("RemoveRedundantQualifierName")
    private val stack = ArrayList<RecursiveStringIterator.Level>()

    /**
     * Offset accumulator for mapping virtual indices to real for slicing.
     * @author Andrew Golovashevich
     */
    private var offset = 0u

    /**
     * Virtual index, before which slices not allowed.
     * @author Andrew Golovashevich
     */
    private var cutPos = 0u

    init {
        @Suppress("RemoveRedundantQualifierName")
        this.stack.add(RecursiveStringIterator.Level(topLvl))
    }

    /**
     * Adds new level expansion level.
     * @param string macro expansion.
     * @param realLength length of macro before expansion.
     * @author Andrew Golovashevich
     */
    fun addExpansion(string: String, realLength: UInt) {
        val lvl = this.stack.lastOrNull() ?: this.throwNoCharsLeft()
        this.offset = this.offset + lvl.pos - realLength
        this.cutPos = this.offset
        @Suppress("RemoveRedundantQualifierName")
        this.stack.add(RecursiveStringIterator.Level(string))
    }

    /**
     * Throws error that no chars left.
     * @author Andrew Golovashevich
     */
    private inline fun throwNoCharsLeft(): Nothing = throw NoSuchElementException("No chars left in source")

    /**
     * Asserts that [level][RecursiveStringIterator.Level] on top of [stack][RecursiveStringIterator.stack]
     * have unconsumed chars.
     *
     * This function is not part of [move][RecursiveStringIterator.move] because after moving to end of
     * [string][RecursiveStringIterator.Level.string] user can get [slices][RecursiveStringIterator.stack]
     * from ended level.
     *
     * @return Top of [stack][RecursiveStringIterator.stack].
     * @author Andrew Golovashevich
     */
    @Suppress("RemoveRedundantQualifierName")
    private inline fun assertStackPos(): RecursiveStringIterator.Level {
        val lvl = this.stack.lastOrNull() ?: this.throwNoCharsLeft()
        @Suppress("LiftReturnOrAssignment")
        if (lvl.pos >= lvl.length) {
            this.offset += this.stack.removeLast().length
            @Suppress("NAME_SHADOWING")
            val lvl = this.stack.lastOrNull() ?: this.throwNoCharsLeft()
            this.offset += lvl.length
            return lvl
        } else {
            return lvl
        }
    }

    /**
     * Returns char on current position.
     * If no chars left, raises error.
     * @author Andrew Golovashevich
     */
    fun get(): Char {
        val lvl = this.assertStackPos()
        return lvl.string[lvl.pos.toInt()]
    }

    /**
     * Moves internal pointer to next pos.
     * If no chars left, raises error.
     * @author Andrew Golovashevich
     */
    fun move() {
        val lvl = this.assertStackPos()
        ++lvl.pos
    }

    /**
     * Returns char on current position and moves internal pointer.
     * If no chars left, raises error.
     * @author Andrew Golovashevich
     */
    fun getAndMove(): Char {
        val lvl = this.assertStackPos()
        return lvl.string[(lvl.pos++).toInt()]
    }

    /**
     * Returns slice from virtual range from [virtualStart] (inclusive) to [virtualEnd] (exclusive).
     * Not allows ranges that was in other expansion levels.
     * @author Andrew Golovashevich
     */
    fun slice(virtualStart: UInt, virtualEnd: UInt): String {
        if (virtualEnd < virtualStart) throw IllegalArgumentException("End index must be greater then start")
        if (virtualStart < this.cutPos) throw IllegalArgumentException("Slice from different macro regions")
        val lvl = this.stack.lastOrNull() ?: this.throwNoCharsLeft()
        return lvl.string.slice((virtualStart - this.offset).toInt() until (virtualEnd - this.offset).toInt())
    }

    override fun hasNext(): Boolean {
        for (lvl in this.stack.reversed()) {
            if (lvl.pos < lvl.length)
                return true
        }
        return false
    }

    @Deprecated("Can be used only for iteration", ReplaceWith("getAndMove"))
    override fun next(): Char = this.getAndMove()
}