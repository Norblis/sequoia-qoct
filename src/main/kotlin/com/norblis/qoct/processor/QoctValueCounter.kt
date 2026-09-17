package com.norblis.qoct.processor

import com.norblis.qoct.QoctFrameView

/**
 *  Counts occurrences of different values in the dataset.
 */
class QoctValueCounter() : QoctOperation {

    var valueCountsByThread = Array(0) { LongArray(0) }
    val totalCounts = LongArray(256) { 0L }

    var hasResult: Boolean = false

    private var cols: Int = -1
    private var rows: Int = -1
    private var threads: Int = -1
    private var frameSize: Int = -1

    override fun setup(
        threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int
    ) {
        this.threads = threads
        this.cols = cols
        this.rows = rows
        frameSize = cols * rows

        valueCountsByThread = Array(threads) {
            LongArray(256)
        }
    }

    override fun processFrame(
        frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int
    ) {
        for (i in 0 until frameSize) {
            val value = frameView.get(i).toUInt().toInt()
            ++valueCountsByThread[thread][value]
        }
    }

    override fun finish(frameCount: Long) {
        // Collect counts.
        for (i in 0 until totalCounts.size) {
            totalCounts[i] = valueCountsByThread.sumOf { it[i] }
        }
        hasResult = true
    }

    val result: LongArray?
        get() {
            return if (hasResult) {
                totalCounts.copyOf()
            } else {
                null
            }
        }
}