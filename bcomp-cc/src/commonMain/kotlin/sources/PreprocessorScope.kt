package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import io.github.landgrafhomyak.itmo.bcomp_cc.Macro

internal interface PreprocessorScope : ParserScope {
    fun injectMacro(realStart: UInt, realLength: UInt, expansion: String)
    fun enterIf(conditionResult: Boolean)
    fun updateLastConditionResult(conditionResult: Boolean)
    fun leaveIf()
    fun lastConditionResult(): Boolean?
    fun findMacro(name: String): Macro?
    var line: UInt
    var file: String
}