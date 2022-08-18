package io.github.landgrafhomyak.itmo.bcomp_cc.sources

internal class MacroMappings(baseLength: UInt) {

    private class Injection(
        val position: UInt,
        @JvmField
        val child: MacroMappings,
        val offset: UInt,
    )

    @Suppress("RemoveRedundantQualifierName")
    private val mappings = ArrayList<MacroMappings.Injection>()

    var length: UInt = baseLength
        private set

    @Suppress("RemoveRedundantQualifierName")
    fun inject(where: UInt, oldLength: UInt, newLength: UInt) {
        this.length += this.length + newLength - oldLength
        if (this.mappings.isEmpty()) {
            this.mappings.add(MacroMappings.Injection(where, MacroMappings(newLength), 0u))
            return
        }
        val last = this.mappings.last()
        if (where < last.position) {
            throw IllegalArgumentException()
        }
        if (where - last.position < last.child.length) {
            last.child.inject(where - last.position, oldLength, newLength)
        }
    }
}