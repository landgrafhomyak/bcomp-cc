package io.github.landgrafhomyak.itmo.bcomp_cc

// todo make default headers
/**
 * Provider of [headers](https://en.wikipedia.org/wiki/Include_directive#C/C++) sources.
 * @author Andrew Golovashevich
 */
interface HeadersProvider {
    /**
     * Searches for local header (#include "header").
     * @return Sources of header or `null` if it isn't found.
     * @author Andrew Golovashevich
     */
    fun localHeader(path: String): String?

    /**
     * Searches for global header (#include $lt;header&gt;).
     * @return Sources of header or `null` if it isn't found.
     * @author Andrew Golovashevich
     */
    fun globalHeader(path: String): String?
}