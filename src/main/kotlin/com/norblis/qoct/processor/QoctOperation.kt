package com.norblis.qoct.processor

import com.norblis.qoct.QoctFrameView

interface QoctOperation {

    /**
     * @param threads The total number of threads the run method can be called from.
     */
    fun setup(threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int)

    // Is called in parallel. At most one thread per step.
    fun processFrame(frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int)

    // Called when all frames have been processed.
    fun finish(frameCount: Long)
}