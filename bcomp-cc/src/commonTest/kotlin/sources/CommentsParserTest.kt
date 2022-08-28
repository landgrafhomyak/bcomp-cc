package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommentsParserTest {
    private interface TestBuilderScope {
        infix fun TestBuilderScope.source(text: () -> String): TestBuilderScope
        infix fun TestBuilderScope.onelineComment(text: () -> String): TestBuilderScope
        infix fun TestBuilderScope.multilineComment(text: () -> String): TestBuilderScope
    }

    private fun buildTest(builder: TestBuilderScope.() -> Unit) {
        val mapping = CommentsIndexMapping()
        val realSourcesBuilder = StringBuilder()
        val virtualSourcesBuilder = StringBuilder()
        val expectedEntities = ArrayList<Entity>()
        val actualEntities = ArrayList<Entity>()
        var builderRealPos: UInt = 0u

        class TestBuilderScopeImpl : TestBuilderScope {
            override fun TestBuilderScope.source(text: () -> String): TestBuilderScope {
                @Suppress("NAME_SHADOWING")
                val text = text()
                realSourcesBuilder.append(text)
                virtualSourcesBuilder.append(text)
                builderRealPos += text.length.toUInt()
                return this
            }

            override fun TestBuilderScope.onelineComment(text: () -> String): TestBuilderScope {
                @Suppress("NAME_SHADOWING")
                val text = text()
                realSourcesBuilder.append(text)
                expectedEntities.add(Entity.OneLineComment(builderRealPos, text.length.toUInt()))
                builderRealPos += text.length.toUInt()
                return this
            }

            override fun TestBuilderScope.multilineComment(text: () -> String): TestBuilderScope {
                @Suppress("NAME_SHADOWING")
                val text = text()
                realSourcesBuilder.append(text)
                expectedEntities.add(Entity.MultiLineComment(builderRealPos, text.length.toUInt()))
                builderRealPos += text.length.toUInt()
                return this
            }
        }

        builder(TestBuilderScopeImpl())

        val realSources = realSourcesBuilder.toString()

        class CommentsParserTestScope : CommentsParserScope {
            override fun removeComment(start: UInt, length: UInt) {
                mapping.removeCommentRange(start, length)
            }

            override var pos: UInt = 0u
                private set

            override fun currentChar(): Char {
                if (this.pos >= realSources.length.toUInt())
                    throw SourceEndedSignal()
                return realSources[this.pos.toInt()]
            }

            override fun getCharAndMoveNext(): Char {
                val c = this.currentChar()
                this.pos++
                return c
            }

            override fun moveNext() {
                if (this.pos < realSources.length.toUInt())
                    this.pos++
            }

            override fun isEnded(): Boolean = this.pos >= realSources.length.toUInt()

            override fun addEntity(entity: Entity) {
                actualEntities.add(entity)
            }

            override fun slice(start: UInt, length: UInt): String =
                realSources.slice(start.toInt() until (start + length).toInt())
        }
        try {
            CommentsParserTestScope().ewRemoveComments()
        } catch (_: SourceEndedSignal) {
        }
        val expectedVirtualSources = virtualSourcesBuilder.toString()
        val actualVirtualSources = mapping.extractVirtualSources(realSources)

        assertEquals(expectedVirtualSources, actualVirtualSources)
    }

    @Test
    fun sourcesOnly() = buildTest {
        source { "source1" }
        source { "source2" }
        source { "source3" }
    }

    @Test
    fun onelineCommentOnly() = buildTest {
        onelineComment { "// one line comment" }
    }

    @Test
    fun multilineCommentOnly() = buildTest {
        onelineComment {
            """/**
                * multi
                * line
                * comment
                */"""
        }
    }

    @Test
    fun commentInsideString() = buildTest {
        source { "\"/*   qwerqwr qewrqwe r */ // qwerqwe r\"" }
    }

    @Test
    fun commentAtEnd() = buildTest {
        source { "/" }
    }

    @Test
    fun brokenMultilineComment() = buildTest {
        multilineComment { "/*/ this comment too" }
    }

    @Test
    fun program1() = buildTest {
        onelineComment { "// Hello world example" } source { "\n" }
        source { "#  " } multilineComment { "/* eqrew */" } source { " include <stdio.h>\n" }
        source { "\n" }
        onelineComment { "// entry point" } source { "\n" }
        source { "void main() {\n" }
        source { "    printf(\"qewrqwerqwerwqerqwerqwerwqerwqreq/**/wer\");" }
        onelineComment { "// print function" } source { "\n" }
        source { "}\n" }
        multilineComment { "/* the end */" }

    }
}
