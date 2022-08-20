package io.github.landgrafhomyak.itmo.bcomp_cc

/**
 * Meta info about [C macro](https://en.wikipedia.org/wiki/C_preprocessor#Macro_definition_and_expansion).
 * @author Andrew Golovashevich
 */
abstract class Macro(
    /**
     * Name of macro.
     * @author Andrew Golovashevich
     */
    val name: String,
    /**
     * Count of macro arguments.
     * If it's `null`, macro is object-like.
     * @author Andrew Golovashevich
     */
    val argsCount: UInt?
) {
    /**
     * Macro expansion builder.
     * [args] length must be equals to [argsCount][Macro.argsCount].
     * @author Andrew Golovashevich
     */
    abstract fun buildReplacement(file: String, line: String, args: Array<String>)
}