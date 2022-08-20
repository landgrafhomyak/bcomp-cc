package io.github.landgrafhomyak.itmo.bcomp_cc.sources

import io.github.landgrafhomyak.itmo.bcomp_cc.HeadersProvider
import io.github.landgrafhomyak.itmo.bcomp_cc.Macro

@Suppress("KDocUnresolvedReference")
/**
 * Dsl scope for writing parser.
 * @param source Source string.
 * @param entities entities [list][MutableList] to store [entities][Entity].
 * @param warnings [list][MutableList] to store [warning][Entity.cWarning] [entities][Entity].
 * @param errors [list][MutableList] to store [error][Entity.cError] [entities][Entity].
 * @param macro predefined [macro][Macro].
 * @param headers [provider to headers sources][HeadersProvider].
 * @author Andrew Golovashevich
 */
internal class ParserScope(
    source: String,
    /**
     * [List][MutableList] to store parsed [entities][Entity].
     * @author Andrew Golovashevich
     */
    private val entities: MutableList<Entity>,

    /**
     * [List][MutableList] to store parsed [warning][Entity.cWarning] [entities][Entity].
     * @author Andrew Golovashevich
     */
    private val warnings: MutableList<Entity>,

    /**
     * [List][MutableList] to store parsed [errors][Entity.cError] [entities][Entity].
     * @author Andrew Golovashevich
     */
    private val errors: MutableList<Entity>,
    macro: Map<String, Macro> = emptyMap(),

    /**
     * [Provider to headers sources][HeadersProvider].
     * @author Andrew Golovashevich
     */
    private val headers: HeadersProvider = object : HeadersProvider {
        override fun localHeader(path: String): String? = null
        override fun globalHeader(path: String): String? = null
    }
) {
    /**
     * Storage of [macro][Macro] for easier access from parser.
     * @author Andrew Golovashevich
     */
    private val macro = HashMap<String, Macro>()

    init {
        this.macro += macro
    }

    /**
     * [Virtual sources][RecursiveStringIterator] for parsing.
     * @see getChar
     * @see getCharAndMoveNext
     * @see moveNext
     * @author Andrew Golovashevich
     */
    private val source = RecursiveStringIterator(source)

    /**
     * [Mappings][MacroMappings] for mapping virtual indices to real.
     * @see addEntity
     * @author Andrew Golovashevich
     */
    private val mappings = MacroMappings(source.length.toUInt())

    /**
     * Current virtual index.
     * @author Andrew Golovashevich
     */
    internal var pos: UInt = 0u
        private set

    /**
     * Returns char on current position.
     * @see getCharAndMoveNext
     * @author Andrew Golovashevich
     */
    internal inline fun getChar(): Char = this.source.get()

    /**
     * Returns char on current position and moves [position][ParserScope.pos] to next char.
     * @see getChar
     * @see moveNext
     * @author Andrew Golovashevich
     */
    internal inline fun getCharAndMoveNext(): Char = this.source.getAndMove()

    /**
     * Moves [position][ParserScope.pos] to next char.
     * @see getCharAndMoveNext
     * @author Andrew Golovashevich
     */
    inline fun moveNext() {
        this.source.move()
        this.pos++
    }

    /**
     * Adds entity to [resulting pool][ParserScope.entities].
     * If entity is inside macro expansion, it wouldn't be added.
     * @author Andrew Golovashevich
     */
    inline fun addEntity(entity: Entity) {
        this.mappings.map(entity.start, entity.length) { start, length ->
            entity.start = start
            entity.length = length
            this.entities.add(entity)
            if (entity is Entity.cWarning)
                this.warnings.add(entity)
            if (entity is Entity.cError)
                this.errors.add(entity)
        }
    }

    /**
     * Adds macro or include expansion as virtual source.
     * @author Andrew Golovashevich
     */
    inline fun addExpansion(realLength: UInt, expansion: String) {
        this.pos -= realLength
        this.source.addExpansion(expansion, realLength)
        this.mappings.addExpansion(this.pos, realLength, expansion.length.toUInt())
    }

    /**
     * Returns macro by [name] if it exists in [global parser's storage][ParserScope.macro].
     * @author Andrew Golovashevich
     */
    inline fun getMacro(name: String): Macro? = this.macro[name]

    /**
     * Saves macro to [global parser's storage][ParserScope.macro].
     * @author Andrew Golovashevich
     */
    inline fun setMacro(name: String, macro: Macro) {
        this.macro[name] = macro
    }

    /**
     * Removes macro from [global parser's storage][ParserScope.macro].
     * @return `true` if macro was defined [global parser's storage][ParserScope.macro] otherwise `false`.
     * @author Andrew Golovashevich
     */
    inline fun deleteMacro(name: String): Boolean = this.macro.remove(name) != null

    /**
     * Searches for local header.
     * @author Andrew Golovashevich
     */
    inline fun getLocalHeader(path: String): String? = this.headers.localHeader(path)

    /**
     * Searches for global header.
     * @author Andrew Golovashevich
     */
    inline fun getGlobalHeader(path: String): String? = this.headers.globalHeader(path)
}