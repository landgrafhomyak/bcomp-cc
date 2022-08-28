package io.github.landgrafhomyak.itmo.bcomp_cc.sources

@Suppress("RemoveRedundantQualifierName")
sealed class Entity(
    @set:Deprecated("For internal usage", level = DeprecationLevel.HIDDEN)
    final override var start: UInt,
    @set:Deprecated("For internal usage", level = DeprecationLevel.HIDDEN)
    final override var length: UInt
) : IndexMapping.MappedPosLength/*,  IndexMapping.MappedPosPos */ {
    /*
    @Deprecated("For internal usage", level = DeprecationLevel.HIDDEN)
    override var end: UInt
        get() = this.start + this.length
        set(value) {
            checkStartEnd(this.start, value)
            this.length = value - this.start
        }
    */
    /**
     * Category of entities for marking warnings.
     */
    @Suppress("ClassName")
    sealed interface cWarning {
        val message: String
    }

    /**
     * Category of entities for marking error.
     */
    @Suppress("ClassName")
    sealed interface cError {
        val message: String
    }

    @Suppress("ClassName")
    sealed interface cComment

    class OneLineComment(start: UInt, length: UInt) :
        Entity(start, length), Entity.cComment

    class MultiLineComment(start: UInt, length: UInt) :
        Entity(start, length), Entity.cComment

    class UnclosedMultilineComment(start: UInt) :
        Entity(start, 1u), Entity.cComment, Entity.cWarning {
        override val message: String get() = "Unclosed multiline comment"
    }

    /**
     * Category of entities related to preprocessor directives.
     */
    @Suppress("ClassName")
    sealed interface cPreprocessorDirective


    class RedundantSemicolon(start: UInt) :
        Entity(start, 1u), Entity.cPreprocessorDirective, Entity.cWarning {
        override val message: String = "Redundant semicolon"
    }

    /**
     * Whole directive declaration from '#' to it's end.
     */
    class WholePreprocessorDirective(start: UInt, length: UInt) :
        Entity(start, length), Entity.cPreprocessorDirective

    /**
     * Sign '#' that starts preprocessor directive declaration.
     * Always have [length][Entity.length] 1.
     */
    class PreprocessorDirectiveSign(start: UInt) :
        Entity(start, 1u), Entity.cPreprocessorDirective

    class PreprocessorDirectiveWithoutName(start: UInt, length: UInt) :
        Entity(start, length), Entity.cPreprocessorDirective, Entity.cError {
        override val message: String = "Missed preprocessor name"
    }

    class UnexpectedCharsInIdentifier(start: UInt, wrongChar: Char) :
        Entity(start, 1u), Entity.cError {
        override val message: String = "Unexpected char '$wrongChar' in identifier"
    }

    class PreprocessorIncludePath(start: UInt, length: UInt) :
        Entity(start, length), Entity.cPreprocessorDirective

    class UnclosedStringLiteral(start: UInt) :
        Entity(start, 1u), Entity.cError {
        override val message: String = "Unclosed string literal"
    }

    /**
     * Directive name, eg. 'include' or 'ifdef'.
     */
    class PreprocessorDirectiveName(start: UInt, length: UInt) :
        Entity(start, length), Entity.cPreprocessorDirective
}