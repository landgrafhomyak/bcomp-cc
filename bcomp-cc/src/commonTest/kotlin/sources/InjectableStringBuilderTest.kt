package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test
import kotlin.test.assertEquals

class InjectableStringBuilderTest {
    fun interface Macro {
        fun SourceBuilder.expansion()
    }

    interface SourceBuilder {
        interface MacroInjectorScope {
            operator fun String.compareTo(macro: Macro): Int
        }

        infix fun SourceBuilder.straight(text: () -> String): SourceBuilder
        infix fun SourceBuilder.macro(expansion: MacroInjectorScope.() -> Unit): SourceBuilder
    }

    private interface TestBuilderScope : SourceBuilder {
        fun compileMacro(macro: SourceBuilder.() -> Unit): Macro
    }

    private fun buildTest(builder: TestBuilderScope.() -> Unit) {
        val realSources = StringBuilder()
        val expectedVirtualSources = StringBuilder()

        class MacroExpansionCompiler : SourceBuilder, SourceBuilder.MacroInjectorScope {
            override fun String.compareTo(macro: Macro): Int {
                macro.apply { this@MacroExpansionCompiler.expansion() }
                return 0
            }

            override fun SourceBuilder.straight(text: () -> String): SourceBuilder {
                expectedVirtualSources.append(text())
                return this@MacroExpansionCompiler
            }

            override fun SourceBuilder.macro(expansion: SourceBuilder.MacroInjectorScope.() -> Unit): SourceBuilder {
                expansion(this@MacroExpansionCompiler)
                return this@MacroExpansionCompiler
            }
        }

        val macroExpansionCompilerObj = MacroExpansionCompiler()

        class SourceCompiler : TestBuilderScope, SourceBuilder.MacroInjectorScope {
            override fun String.compareTo(macro: Macro): Int {
                realSources.append(this@compareTo)
                macro.apply { macroExpansionCompilerObj.expansion() }
                return 0
            }

            override fun compileMacro(macro: SourceBuilder.() -> Unit): Macro = Macro(macro)

            override fun SourceBuilder.straight(text: () -> String): SourceBuilder {
                realSources.append(text())
                expectedVirtualSources.append(text())
                return this@SourceCompiler
            }

            override fun SourceBuilder.macro(expansion: SourceBuilder.MacroInjectorScope.() -> Unit): SourceBuilder {
                expansion(this@SourceCompiler)
                return this@SourceCompiler
            }
        }

        builder(SourceCompiler())

        val actualVirtualSources = InjectableStringBuilder(realSources.toString())
        var realPos = 0u
        var expectedVirtualPos = 0u

        class MacroSourceCompiler(private val builder: StringBuilder) : SourceBuilder, SourceBuilder.MacroInjectorScope {
            override fun SourceBuilder.straight(text: () -> String): SourceBuilder {
                this@MacroSourceCompiler.builder.append(text())
                return this@MacroSourceCompiler
            }

            override fun SourceBuilder.macro(expansion: SourceBuilder.MacroInjectorScope.() -> Unit): SourceBuilder {
                expansion(this@MacroSourceCompiler)
                return this@MacroSourceCompiler
            }

            override fun String.compareTo(macro: Macro): Int {
                this@MacroSourceCompiler.builder.append(this@compareTo)
                return 0
            }
        }

        class InjectorAndAsserter : TestBuilderScope, SourceBuilder.MacroInjectorScope {
            override fun compileMacro(macro: SourceBuilder.() -> Unit): Macro = Macro(macro)

            override fun SourceBuilder.straight(text: () -> String): SourceBuilder {
                for (c in text()) {
                    assertEquals(expectedVirtualPos, actualVirtualSources.virtualPos, "Virtual position in builder")
                    assertEquals(c, actualVirtualSources.getCharAndMoveNext(), "Char real_pos=${realPos++} virtual_pos=${expectedVirtualPos++}")
                }
                return this@InjectorAndAsserter
            }

            override fun SourceBuilder.macro(expansion: SourceBuilder.MacroInjectorScope.() -> Unit): SourceBuilder {
                expansion(this@InjectorAndAsserter)
                return this@InjectorAndAsserter
            }

            override fun String.compareTo(macro: Macro): Int {
                this@InjectorAndAsserter.straight { this@compareTo }
                expectedVirtualPos -= this@compareTo.length.toUInt()
                val expansion = StringBuilder()
                macro.apply { MacroSourceCompiler(expansion).expansion() }
                actualVirtualSources.injectMacro(expectedVirtualPos, this@compareTo.length.toUInt(), expansion.toString())
                macro.apply { this@InjectorAndAsserter.expansion() }
                return 0
            }
        }

        builder(InjectorAndAsserter())

        assertEquals(expectedVirtualSources.toString(), actualVirtualSources.exportVirtualSources(), "Virtual sources")
    }

    @Test
    fun straightSources() = buildTest {
        straight { "a" } straight { "b" } straight { "c" }
    }

    @Test
    fun simpleMacroOnly() = buildTest {
        val simpleMacro = compileMacro { straight { "abc" } }
        macro { "abc" >= simpleMacro }
    }

    @Test
    fun simpleMacro() = buildTest {
        val simpleMacro = compileMacro { straight { "abc" } }
        straight { "before" } macro { "MACRO" >= simpleMacro } straight { "after" }
    }

    @Test
    fun macroInMacro() = buildTest {
        val innerMacro = compileMacro { straight { "expansion" } }
        val outerMacro = compileMacro { macro { "IM" >= innerMacro } }
        straight { "before" } macro { "OM" >= outerMacro } straight { "after" }
    }
}