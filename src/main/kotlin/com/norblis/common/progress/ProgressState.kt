package com.norblis.common.progress

interface ProgressState {
    val target: Long
    val progress: Long
    val indeterminate: Boolean

    fun fractionOrNull(): Double? {
        if (indeterminate) return null
        return progress.toDouble() / target.toDouble()
    }

    fun percentOrNull(decimals: Int = 1): String? {
        val frac = fractionOrNull() ?: return null
        val pct = frac * 100.0
        return "%.${decimals}f%%".format(pct)
    }
}

interface MutableProgressState : ProgressState {
    override var target: Long
    override var progress: Long
    override var indeterminate: Boolean

    fun reset() {
        indeterminate = true
        progress = 0
        target = 1
    }

    fun begin(initialProgress: Long, target: Long) {
        this.progress = initialProgress
        this.target = target
        indeterminate = false
    }

    /**
     * Sets progress = target
     */
    fun finish() {
        progress = target
    }
}

data class ProgressSnapshot(
    override val target: Long,
    override val progress: Long,
    override val indeterminate: Boolean
) : ProgressState