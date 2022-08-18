/**
 * Util classes for building test for MacroMappings using comfortable dsl.
 */

@file:JvmName("MacroMappingsTestUtilsKt")

package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.assertEquals
import kotlin.to as makePair

/**
 * Scope provides adding to [mapping][MacroMappings] stuff (const-length regions) and macro (regions with own mapping and variable length).
 * @author Andrew Golovashevich
 */
internal interface MacroMappingsTestInjectionBuilderScope {
    /**
     * Object for avoiding invoking infix functions on `this`.
     * @author Andrew Golovashevich
     */
    @Deprecated("Internal use")
    object Line

    /**
     * Starts line with stuff.
     * @author Andrew Golovashevich
     */
    @Suppress("DEPRECATION")
    infix fun s(stuff: () -> UInt): Line = Line.s(stuff)


    /**
     * Starts line with injecting macro.
     * @author Andrew Golovashevich
     */
    @Suppress("DEPRECATION")
    infix fun m(macro: () -> MacroMappingsTestBuilderScope.TestMacro): Line = Line.m(macro)


    /**
     * Adds stuff to line.
     * @author Andrew Golovashevich
     */
    @Suppress("DEPRECATION")
    infix fun Line.s(stuff: () -> UInt): Line

    /**
     * Injects macro to line.
     * @author Andrew Golovashevich
     */
    @Suppress("DEPRECATION")
    infix fun Line.m(macro: () -> MacroMappingsTestBuilderScope.TestMacro): Line
}

/**
 * Top-level scope for building tests.
 * @author Andrew Golovashevich
 */
internal interface MacroMappingsTestBuilderScope : MacroMappingsTestInjectionBuilderScope {
    /**
     * Same as [`Pair<UInt, MacroMappingsTestInjectionBuilderScope.() -> Unit>`][Pair], but avoids using [`A.to(second: B)`][makePair], you should use
     * [define][MacroMappingsTestBuilderScope.define] instead.
     * @author Andrew Golovashevich
     */
    class TestMacro
    @Suppress("MemberVisibilityCanBePrivate")
    @Deprecated("Use 'define(...) {...}'", ReplaceWith("define"))
    internal constructor(
        /**
         * Length of word that will be replaced by [`TestMacro.expansion`][MacroMappingsTestBuilderScope.TestMacro.expansion]
         * @author Andrew Golovashevich
         */
        internal val baseLength: UInt,
        /**
         * Macro body [builder][MacroMappingsTestInjectionBuilderScope].
         * @author Andrew Golovashevich
         */
        internal val expansion: MacroMappingsTestInjectionBuilderScope.() -> Unit
    ) {
        override fun toString(): String = "<test macro base_len=${this.baseLength}>"
    }

    /**
     * Public constructor of [MacroMappingsTestBuilderScope.TestMacro].
     * @author Andrew Golovashevich
     */
    @Suppress("DEPRECATION")
    fun define(baseLength: UInt, expansion: MacroMappingsTestInjectionBuilderScope.() -> Unit) = TestMacro(baseLength, expansion)

    /**
     * Asserts that [virtual] index (assuming that it is not in macro injection) maps to [real] index.
     * @author Andrew Golovashevich
     */
    fun assertIndex(virtual: UInt, real: UInt) = this.assertIndex(virtual, real, real)

    /**
     * Asserts that [virtual] index (assuming that it is inside macro injection) map to [realStart] and [realEnd] indices.
     * @author Andrew Golovashevich
     */
    fun assertIndex(virtual: UInt, realStart: UInt, realEnd: UInt)

    /**
     * Asserts that all indices in range [virtual] (assuming that it is macro range) map to [realStart] and [realEnd] indices.
     * @author Andrew Golovashevich
     */
    fun assertMacro(virtual: UIntRange, realStart: UInt, realEnd: UInt) {
        for (index in virtual)
            this.assertIndex(index, realStart, realEnd)
    }

    /**
     * Asserts that all indices in range [virtual] (assuming that it isn't includes any macro-virtual regions) maps to indices with specified [offset].
     * @author Andrew Golovashevich
     */
    fun assertStuff(virtual: UIntRange, offset: UInt) {
        for (index in virtual)
            this.assertIndex(index, index - offset)
    }
}

/**
 * Implementation of [MacroMappingsTestInjectionBuilderScope] and [MacroMappingsTestBuilderScope] for calc string length before injecting macros.
 * @author Andrew Golovashevich
 */
private class MacroMappingsTestLengthBuilder : MacroMappingsTestBuilderScope {
    /**
     * Current accumulated length.
     * @author Andrew Golovashevich
     */
    var length: UInt = 0u

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.s(stuff: () -> UInt): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestLengthBuilder.length += stuff()
        return MacroMappingsTestInjectionBuilderScope.Line
    }

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.m(macro: () -> MacroMappingsTestBuilderScope.TestMacro): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestLengthBuilder.length += macro().baseLength
        return MacroMappingsTestInjectionBuilderScope.Line
    }

    override fun assertIndex(virtual: UInt, realStart: UInt, realEnd: UInt) {}

    override fun assertMacro(virtual: UIntRange, realStart: UInt, realEnd: UInt) {}

    override fun assertStuff(virtual: UIntRange, offset: UInt) {}
}

/**
 * Implementation of [MacroMappingsTestInjectionBuilderScope] and [MacroMappingsTestBuilderScope] to build [mapping][MacroMappings]
 * and running assertions.
 * @author Andrew Golovashevich
 */
private class MacroMappingsTestMappingsBuilder(private val map: MacroMappings) : MacroMappingsTestBuilderScope {
    /**
     * Current length after injections.
     * @author Andrew Golovashevich
     */
    private var pos = 0u


    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.s(stuff: () -> UInt): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestMappingsBuilder.pos += stuff()
        return MacroMappingsTestInjectionBuilderScope.Line
    }

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.m(macro: () -> MacroMappingsTestBuilderScope.TestMacro): MacroMappingsTestInjectionBuilderScope.Line {
        @Suppress("NAME_SHADOWING")
        val macro = macro()
        val length = MacroMappingsTestLengthBuilder()
        macro.expansion(length)
        this@MacroMappingsTestMappingsBuilder.map.inject(this@MacroMappingsTestMappingsBuilder.pos, macro.baseLength, length.length)
        macro.expansion(this@MacroMappingsTestMappingsBuilder)
        return MacroMappingsTestInjectionBuilderScope.Line
    }

    override fun assertIndex(virtual: UInt, realStart: UInt, realEnd: UInt) {
        assertEquals(realStart, this.map.mapStart(virtual), "Mapping index $virtual to start")
        assertEquals(realEnd, this.map.mapEnd(virtual), "Mapping index $virtual to end")
    }
}


/**
 * Builder of test for [MacroMappings]. Runs scope twice: for [calc real length][MacroMappingsTestLengthBuilder] and for
 * [building maps and running assertions][MacroMappingsTestMappingsBuilder].
 *
 * Scope must contain 3 parts (in this order):
 * * [Macro definitions][MacroMappingsTestBuilderScope.define]
 * * Building string from [stuff][MacroMappingsTestInjectionBuilderScope.s] and [macro][MacroMappingsTestInjectionBuilderScope.m]
 * * Assertions
 *
 * @sample MacroMappingsTest.sequentialInnerMacro
 * @author Andrew Golovashevich
 */
internal fun buildMacroMappingsTest(builder: MacroMappingsTestBuilderScope.() -> Unit) {
    val length = MacroMappingsTestLengthBuilder()
    builder(length)
    val map = MacroMappings(length.length)
    builder(MacroMappingsTestMappingsBuilder(map))
}