package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.test.Test

internal class MacroMappingsTest {
    @Test
    fun simpleStuff() = buildMacroMappingsTest {
        s { 1u } s { 2u }
    }

    @Test
    fun simpleMacro() = buildMacroMappingsTest {
        val a = define(1u) { s { 10u } }
        s { 2u } m { a } s { 2u }
    }
}

























