package io.github.landgrafhomyak.itmo.bcomp_cc.sources


internal fun PreprocessorScope.processConditionalDirectives() {
    lines@ while (!isEnded()) {
        when (skipSpaces()) {
            '#'  -> parseDirective()
            else -> findIdentifiersThisLine()
        }
        moveNext()
    }
}


private inline fun PreprocessorScope.parseDirective() {
    val directiveStart = pos
    addEntity(Entity.PreprocessorDirectiveSign(directiveStart))
    moveNext()
    try {
        val directiveNameStartChar = skipSpaces()
        if (directiveNameStartChar != '\n') {
            if (checkIdentifierStartChar(directiveNameStartChar)) {
                val directiveNameStart = pos
                try {
                    val directiveNameEndChar = skipIdentifier()
                } finally {
                    addEntity(Entity.PreprocessorDirectiveName(directiveNameStart, calcLength(directiveNameStart)))
                    when (val directiveName = slice(directiveNameStart, calcLength(directiveNameStart))) {
                        "if"      -> {}
                        "else"    -> directiveElse(directiveNameStart)
                        "elif"    -> {}
                        "endif"   -> directiveEndif(directiveNameStart)
                        "ifdef"   -> directiveIfDef(true)
                        "ifndef"  -> directiveIfDef(false)
                        "define"  -> {}
                        "undef"   -> {}
                        "include" -> {}
                        "error"   -> directiveError(directiveStart)
                        "line"    -> directiveLine()
                        "pragma"  -> directivePragma()
                    }
                }
            } else {
                addEntity(Entity.UnexpectedCharInIdentifier(pos, directiveNameStartChar))
            }
        }
    } finally {
        addEntity(Entity.WholePreprocessorDirective(directiveStart, calcLength(directiveStart)))
        injectMacro(directiveStart, calcLength(directiveStart), "")
    }
}

private inline fun PreprocessorScope.findIdentifiersThisLine() {

}

private inline fun PreprocessorScope.directiveElse(directiveNameStart: UInt) {
    @Suppress("LiftReturnOrAssignment")
    when (lastConditionResult()) {
        true  -> updateLastConditionResult(false)
        false -> updateLastConditionResult(true)
        null  ->
            addEntity(Entity.PreprocessorConditionalBranchWithoutIf(directiveNameStart, calcLength(directiveNameStart)))
    }
}

private inline fun PreprocessorScope.directiveEndif(directiveNameStart: UInt) {
    @Suppress("LiftReturnOrAssignment")
    if (lastConditionResult() != null)
        leaveIf()
    else
        addEntity(Entity.PreprocessorConditionalBranchWithoutIf(directiveNameStart, calcLength(directiveNameStart)))
}

/**
 * @param cmp `true` for `#ifdef` or `false` for `#ifndef`
 */
private inline fun PreprocessorScope.directiveIfDef(
    cmp: Boolean
) {
    val startChar: Char
    try {
        startChar = skipSpaces()
    } catch (ses: SourceEndedSignal) {
        addEntity(Entity.PreprocessorMissedCondition(pos))
        enterIf(!cmp)
        throw ses
    }

    if (startChar == '\n') {
        addEntity(Entity.PreprocessorMissedCondition(pos))
        enterIf(!cmp)
        return
    }

    if (!checkIdentifierStartChar(startChar)) {
        addEntity(Entity.UnexpectedCharInIdentifier(pos, startChar))
        skipUntilNewLine()
        enterIf(!cmp)
        return
    }

    val startPos = pos
    val result: Boolean
    try {
        skipIdentifier()
    } finally {
        result = (findMacro(slice(startPos, calcLength(pos))) != null) == cmp
        enterIf(result)
    }
    skipUntilNewLine()
}

private inline fun PreprocessorScope.directiveError(directiveStart: UInt) {
    val messageStart = pos
    try {
        skipUntilNewLine()
    } finally {
        addEntity(
            Entity.PreprocessorErrorDirective(
                directiveStart, calcLength(directiveStart),
                slice(messageStart, calcLength(messageStart))
            )
        )
    }
}

private inline fun PreprocessorScope.directiveLine() {
    val lineStartChar: Char
    try {
        lineStartChar = skipSpaces()
    } catch (ses: SourceEndedSignal) {
        addEntity(Entity.PreprocessorMissedLineNumber(pos))
        throw ses
    }

    if (lineStartChar !in '0'..'9') {
        addEntity(Entity.PreprocessorMissedLineNumber(pos))
        skipUntilNewLine()
        return
    }

    val lineStart = pos
    moveNext()
    val afterLineChar: Char
    try {
        afterLineChar = skipChars { c -> c in '0'..'9' }
    } finally {
        addEntity(Entity.IntegerLiteral(lineStart, calcLength(lineStart)))
        val lineNumber = slice(lineStart, calcLength(lineStart)).toUIntOrNull()
        if (lineNumber == null) {
            addEntity(Entity.PreprocessorInvalidLineNumber(lineStart, calcLength(lineStart)))
            skipUntilNewLine()
            return
        }
        line = lineNumber
    }

    if (skipSpaces() == '"') {
        moveNext()
        val fileStart = pos
        var lastFileChar: Char? = null
        try {
            lastFileChar = skipChars { c -> c != '"' && c != '\n' }
        } finally {
            file = slice(fileStart, calcLength(fileStart))
            if (lastFileChar != '"') {
                addEntity(Entity.UnclosedStringLiteral(pos))
            } else {
                moveNext()
            }
            addEntity(Entity.PreprocessorPathString(fileStart - 1u, calcLength(fileStart - 1u)))
        }
    }
    skipUntilNewLine()
}

private inline fun PreprocessorScope.directivePragma() {
    // doing nothing
    skipUntilNewLine()
}