@file:JvmName("ParserUtilsKt")

package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import kotlin.jvm.JvmName


internal inline fun checkSpaceChar(c: Char) = c == ' ' || c == '\t'

internal inline fun checkSpaceOrNewLineChar(c: Char) = c == '\n' || checkSpaceChar(c)

internal inline fun checkIdentifierStartChar(c: Char): Boolean = c == '_' || c in 'a'..'z' || c in 'A'..'Z'

internal inline fun checkIdentifierChar(c: Char): Boolean = c in '0'..'9' || checkIdentifierStartChar(c)

internal inline fun ParserScope.skipChars(onChar: (Char) -> Boolean): Char {
    while (true) {
        val c = currentChar()
        if (!onChar(c))
            return c
        moveNext()
    }
}

internal inline fun ParserScope.skipSpaces(onNonSpaceChar: (Char) -> Boolean = { false }): Char =
    skipChars { c -> checkSpaceChar(c) || onNonSpaceChar(c) }


internal inline fun ParserScope.skipSpacesAndLines(onNonSpaceChar: (Char) -> Boolean = { false }): Char =
    this.skipChars { c -> checkSpaceOrNewLineChar(c) || onNonSpaceChar(c) }

internal inline fun ParserScope.skipUntilNewLine(onNonLineChar: (Char) -> Boolean = { true }): Char =
    this.skipChars { c -> c != '\n' && onNonLineChar(c) }


internal inline fun ParserScope.skipIdentifier(onNonIdentifierChar: (Char) -> Boolean = { false }): Char =
    this.skipChars { c -> checkIdentifierChar(c) || onNonIdentifierChar(c) }

internal inline fun ParserScope.parseStringLiteral(endChar: Char = '"'): String {
    val s = StringBuilder()
    while (true) {
        val c = currentChar()
        if (c == endChar)
            break
    }
    return s.toString()
}


internal inline fun ParserScope.calcLength(start: UInt): UInt {
    if (pos < start) throw IllegalArgumentException()
    return pos - start
}





