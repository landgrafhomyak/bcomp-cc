package io.github.landgrafhomyak.itmo.bcomp_cc.sources

internal fun CommentsParserScope.ewRemoveComments() {
    mainloop@ while (!isEnded()) {
        when (skipChars { c -> c != '"' && c != '/' }) {
            '/' -> {
                val start = pos
                moveNext()
                when (currentChar()) {
                    '*'  -> {
                        try {
                            moveNext()
                            multilineComment@ while (true) {
                                skipChars { c -> c != '*' }
                                moveNext()
                                if (currentChar() != '/') {
                                    moveNext()
                                    continue@multilineComment
                                }
                                moveNext()
                                break@multilineComment
                            }
                        } catch (_: SourceEndedSignal) {
                            addEntity(Entity.UnclosedMultilineComment(pos))
                        }
                        addEntity(Entity.MultiLineComment(start, calcLength(start)))
                        removeComment(start, calcLength(start))
                    }
                    '/'  -> {
                        try {
                            skipUntilNewLine()
                        } catch (_: SourceEndedSignal) {
                        }
                        addEntity(Entity.OneLineComment(start, calcLength(start)))
                        removeComment(start, calcLength(start))
                        moveNext()
                        continue@mainloop
                    }
                    else -> continue@mainloop
                }
            }
            '"' -> {
                string@ while (true) {
                    moveNext()
                    when (skipChars { c -> c != '"' && c != '\\' && c != '\n' }) {
                        '"', '\n' -> {
                            moveNext()
                            break@string
                        }
                        '\\'      -> {
                            moveNext()
                            moveNext()
                            continue@string
                        }
                    }
                }
            }
        }
    }
}