package io.github.landgrafhomyak.itmo.bcomp_cc.sources


internal interface MacroMappingsTestInjectionBuilderScope {
    @Deprecated("Internal use")
    object Line

    /**
     * Avoiding jvm conflicts with [MacroFactory][MacroMappingsTestInjectionBuilderScope.MacroFactory]
     * because kotlin lambdas in java are `Function<A1, R>`.
     */
    fun interface StuffFactory {
        fun getStuffLength(): UInt
    }

    /**
     * Avoiding jvm conflicts with [StuffFactory][MacroMappingsTestInjectionBuilderScope.StuffFactory]
     * because kotlin lambdas in java are `Function<A1, R>`.
     */
    fun interface MacroFactory {
        fun getMacro(): MacroMappingsTestBuilderScope.TestMacro
    }

    /**
     * Starts line with [stuff][MacroMappingsTestInjectionBuilderScope.StuffFactory] to injection.
     */
    @Suppress("DEPRECATION")
    infix fun s(stuff: StuffFactory): Line = Line.s(stuff)


    /**
     * Starts line with injecting [macro][MacroMappingsTestInjectionBuilderScope.MacroFactory].
     */
    @Suppress("DEPRECATION")
    infix fun m(macro: MacroFactory): Line = Line.m(macro)


    /**
     * Adds new [stuff][MacroMappingsTestInjectionBuilderScope.StuffFactory] to injection.
     */
    @Suppress("DEPRECATION")
    infix fun Line.s(stuff: StuffFactory): Line

    /**
     * Injects [macro][MacroMappingsTestInjectionBuilderScope.MacroFactory].
     */
    @Suppress("DEPRECATION")
    infix fun Line.m(macro: MacroFactory): Line
}

internal interface MacroMappingsTestBuilderScope : MacroMappingsTestInjectionBuilderScope {
    class TestMacro
    @Suppress("MemberVisibilityCanBePrivate")
    @Deprecated("Use 'define(...) {...}'", ReplaceWith("define"))
    internal constructor(
        internal val baseLength: UInt, internal val replacement: MacroMappingsTestInjectionBuilderScope.() -> Unit
    ) {
        override fun toString(): String = "<test macro base_len=${this.baseLength}>"
    }

    @Suppress("DEPRECATION")
    fun define(baseLength: UInt, replacement: MacroMappingsTestInjectionBuilderScope.() -> Unit) = TestMacro(baseLength, replacement)
}

private class MacroMappingsTestMappingsBuilder(private val map: MacroMappings) : MacroMappingsTestBuilderScope {
    private var pos = 0u

    @Suppress("NOTHING_TO_INLINE")
    private inline fun add(stuff: MacroMappingsTestInjectionBuilderScope.StuffFactory) {
        this.pos += stuff.getStuffLength()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun add(macro: MacroMappingsTestInjectionBuilderScope.MacroFactory) {
        @Suppress("NAME_SHADOWING")
        val macro = macro.getMacro()
        val length = MacroMappingsTestLengthBuilder()
        macro.replacement(length)
        this.map.inject(this.pos, macro.baseLength, length.length)
        macro.replacement(this)
    }

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.s(stuff: MacroMappingsTestInjectionBuilderScope.StuffFactory): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestMappingsBuilder.add(stuff)
        return MacroMappingsTestInjectionBuilderScope.Line
    }

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.m(macro: MacroMappingsTestInjectionBuilderScope.MacroFactory): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestMappingsBuilder.add(macro)
        return MacroMappingsTestInjectionBuilderScope.Line
    }
}


private class MacroMappingsTestLengthBuilder : MacroMappingsTestBuilderScope {
    var length: UInt = 0u

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.s(stuff: MacroMappingsTestInjectionBuilderScope.StuffFactory): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestLengthBuilder.length += stuff.getStuffLength()
        return MacroMappingsTestInjectionBuilderScope.Line
    }

    @Suppress("DEPRECATION")
    override infix fun MacroMappingsTestInjectionBuilderScope.Line.m(macro: MacroMappingsTestInjectionBuilderScope.MacroFactory): MacroMappingsTestInjectionBuilderScope.Line {
        this@MacroMappingsTestLengthBuilder.length += macro.getMacro().baseLength
        return MacroMappingsTestInjectionBuilderScope.Line
    }
}

internal fun buildMacroMappingsTest(builder: MacroMappingsTestBuilderScope.() -> Unit) {
    val length = MacroMappingsTestLengthBuilder()
    builder(length)
    val map = MacroMappings(length.length)
    builder(MacroMappingsTestMappingsBuilder(map))
}