package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test

/**
 * Tests for class [MacroMappings].
 *
 * Description for test contains image of test string, where `*` is stuff and letters - macro injections.
 * Above the string written indices before injection, under - after.
 * @author Andrew Golovashevich
 */
internal class MacroMappingsTest {
    /**
     * Simple string without macro.
     * ```
     *         *:  |  0 |  1    2 |
     *                *    *    *
     *   virtual:  |  0 |  1    2 |
     * ```
     */
    @Test
    fun simpleStuff() = buildMacroMappingsTest {
        s { 1u } s { 2u }

        assertStuff(0u..2u, 0u)
    }


    /**
     * String with one macro.
     * ```
     *         a:            [  0    1    2    3    4 ]
     *         *:  |  0    1 |                        |  4    5 |
     *                *    *    a    a    a    a    a    *    *
     *   virtual:  |  0    1 |  2    3    4    5    6 |  7    8 |
     * ```
     * @author Andrew Golovashevich
     */
    @Test
    fun simpleMacro() = buildMacroMappingsTest {
        val a = define(2u) { s { 5u } }

        s { 2u } m { a } s { 2u }

        assertStuff(0u..1u, 0u)
        assertMacro(2u..6u, 2u, 4u)
        assertStuff(7u..8u, 3u)
    }

    /**
     * String with inner macro.
     * ```
     *         b:                      [  0    1    2    3    4 ]
     *         a:            [  0    1 |                        |  3    4 ]
     *         *:  |  0    1 |                                            |  4    5 |
     *                *    *    a    a    b    b    b    b    b    a    a    *    *
     *   virtual:  |  0    1 |  2    3 |  4    5    6    7    8 |  9   10 | 11   12 |
     * ```
     * @author Andrew Golovashevich
     */
    @Test
    fun innerMacro() = buildMacroMappingsTest {
        val a = define(2u) { s { 5u } }
        val b = define(2u) { s { 2u } m { a } s { 2u } }

        s { 2u } m { b } s { 2u }

        assertStuff(0u..1u, 0u)
        assertMacro(2u..10u, 2u, 4u)
        assertStuff(11u..12u, 7u)
    }
    /**
     * String with 2 sequential macro.
     * ```
     *         a:            [  0    1    2    3    4 ]         [  0    1    2    3    4 ]
     *         *:  |  0    1 |                        |  4    5 |                        |  8    9 |
     *                *    *    a    a    a    a    a    *    *    a    a    a    a    a    *    *
     *   virtual:  |  0    1 |  2    3    4    5    6 |  7    8 |  9   10   11   12   13 | 14   15 |
     * ```
     * @author Andrew Golovashevich
     */
    @Test
    fun sequentialMacro() = buildMacroMappingsTest {
        val a = define(2u) { s { 5u } }

        s { 2u } m { a } s { 2u } m { a } s { 2u }

        assertStuff(0u..1u, 0u)
        assertMacro(2u..6u, 2u, 4u)
        assertStuff(7u..8u, 3u)
        assertMacro(9u..13u, 6u, 8u)
        assertStuff(14u..15u, 6u)
    }

    /**
     * String with 2 sequential macro and 1 inner.
     * ```
     *         a:            [  0    1    2    3    4 ]         [  0    1    2    3    4 ]
     *         b:                                     [  0    1 |                        |  4    5 ]
     *         *:  |  0    1 |                        |                                            |  6    7 |
     *                *    *    a    a    a    a    a    b    b    a    a    a    a    a    b    b    *    *
     *   virtual:  |  0    1 |  2    3    4    5    6 |  7    8 |  9   10   11   12   13 | 14   15 | 16   17 |
     * ```
     * @author Andrew Golovashevich
     */
    @Test
    fun sequentialInnerMacro() = buildMacroMappingsTest {
        val a = define(2u) { s { 5u } }
        val b = define(2u) { s { 2u } m { a } s { 2u } }

        s { 2u } m { a } m { b } s { 2u }

        assertStuff(0u..1u, 0u)
        assertMacro(2u..6u, 2u, 4u)
        assertMacro(7u..15u, 4u, 6u)
        assertStuff(16u..17u, 10u)
    }
}

























