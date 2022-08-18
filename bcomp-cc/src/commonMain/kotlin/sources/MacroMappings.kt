package io.github.landgrafhomyak.itmo.bcomp_cc.sources

/**
 * One of classes, that allows running [C preprocessor](https://en.wikipedia.org/wiki/C_preprocessor) inline while parsing sources.
 * Provides builder and access for mapping [lexemes'][Lexeme] entities from indices inside
 * [macro](https://en.wikipedia.org/wiki/C_preprocessor#Macro_definition_and_expansion) expansion to indices where macro used.
 *
 * Example program
 * ```c
 * #define EXPR ) + 10000 + (
 *
 * (20000 EXPR 30000)
 * ```
 *
 * Virtual lexemes:
 * ```
 * (20000 ) + 10000 + ( 30000)
 *  {___}     {___}     {___}
 *    int constant literals
 * {______}           {______}
 *         expressions
 * ```
 *
 * Mapped lexemes:
 * ```
 * (20000 EXPR 30000)
 *  {___}      {___}
 *   int constant literals (lexeme for 10000 was removed because inside expansion)
 *        {__}
 *        macro
 * {_________}
 *    left expression
 *        {_________}
 *            right expression
 * ```
 *
 * @see ParserScope
 * @author Andrew Golovashevich
 */
internal class MacroMappings(baseLength: UInt) {
    /**
     * Internal class for saving ranges and offsets of injections.
     * @author Andrew Golovashevich
     */
    private class Injection(
        /**
         * Virtual position where expansion starts.
         * @author Andrew Golovashevich
         */
        val position: UInt,
        /**
         * Length of macro definition.
         * @author Andrew Golovashevich
         */
        val realLength: UInt,

        /**
         * Virtual length of expansion.
         * @author Andrew Golovashevich
         */
        var virtualLength: UInt,
        /**
         * Offset to get real start position: `real = `[`virtual`][MacroMappings.Injection.position]` - offset`
         * @author Andrew Golovashevich
         */
        val offset: UInt,
    ) {
        override fun toString(): String = "<macro injection metadata ${this.realLength}=>${this.virtualLength} offset=${this.offset}>"
    }

    /**
     * Injections data. Sorted by [Injection.position] and didn't contain overlapping regions.
     * @author Andrew Golovashevich
     */
    @Suppress("RemoveRedundantQualifierName")
    private val data = ArrayList<MacroMappings.Injection>()

    /**
     * Virtual length of string.
     * @author Andrew Golovashevich
     */
    var length: UInt = baseLength
        private set

    /**
     * Adds info about injection to mapping. If [position][where] is virtual (inside macro expansion), it will be injected to appropriated child injection.
     * @param where Start position of macro use. Virtual expansion will be started at this index too.
     * @param realLength Real length of macro definition.
     * @param virtualLength Length of expansion with injected macro parameters, but without nested macro.
     * @author Andrew Golovashevich
     */
    @Suppress("RemoveRedundantQualifierName")
    fun inject(where: UInt, realLength: UInt, virtualLength: UInt) {
        this.length += virtualLength - realLength
        if (this.data.isEmpty()) {
            this.data.add(MacroMappings.Injection(where, realLength, virtualLength, 0u))
            return
        }
        val last = this.data.last()
        if (where < last.position) {
            throw IllegalArgumentException()
        }
        if (where - last.position < last.virtualLength) {
            last.virtualLength += virtualLength - realLength
        } else {
            this.data.add(MacroMappings.Injection(where, realLength, virtualLength, last.offset + last.virtualLength - last.realLength))
        }
    }

    /**
     * Internal function to find region for specified virtual [index]. Uses binary search because [data array][MacroMappings.data] is sorted.
     * @return Maximal region where [Injection.position] <= [index] or `null`, if [index] is real (no injections before it).
     * [Index][index] can be after the returned region.
     * @author Andrew Golovashevich
     */
    @Suppress("RemoveRedundantQualifierName", "NOTHING_TO_INLINE")
    private inline fun map(index: UInt): MacroMappings.Injection? {
        var lo = -1
        var hi = this.data.size
        var mi: Int
        while (lo < hi - 1) {
            mi = (lo + hi) / 2
            if (index < this.data[mi].position) hi = mi
            else lo = mi
        }
        return if (lo == -1) null else this.data[lo]
    }

    /**
     * Maps virtual [index] to real. If it's inside macro expansion, start pos of macro use will be returned.
     * (ex. right expression in [class example][MacroMappings])
     * @author Andrew Golovashevich
     */
    fun mapStart(index: UInt): UInt {
        val injection = this.map(index) ?: return index
        @Suppress("LiftReturnOrAssignment")
        if (index >= injection.position + injection.virtualLength)
            return index - (injection.offset + injection.virtualLength - injection.realLength)
        else
            return injection.position - injection.offset
    }

    /**
     * Maps virtual [index] to real. If it's inside macro expansion, end pos of macro use will be returned.
     * (ex. left expression in [class example][MacroMappings])
     * @author Andrew Golovashevich
     */
    fun mapEnd(index: UInt): UInt {
        val injection = this.map(index) ?: return index
        @Suppress("LiftReturnOrAssignment")
        if (index >= injection.position + injection.virtualLength)
            return index - (injection.offset + injection.virtualLength - injection.realLength)
        else
            return injection.position - injection.offset + injection.realLength
    }
}