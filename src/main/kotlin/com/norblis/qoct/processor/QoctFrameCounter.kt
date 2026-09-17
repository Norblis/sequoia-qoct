package com.norblis.qoct.processor

import com.norblis.qoct.QoctFrameView

class QoctFrameCounter() : QoctOperation {

    private var counts = IntArray(0)
    private var done = false

    override fun setup(threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int) {
        counts = IntArray(stepCount)
    }

    override fun processFrame(frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int) {
        ++counts[step]
    }

    override fun finish(frameCount: Long) {
        done = true
    }

    fun getFrameCounts(): IntArray {
        check(done)
        return counts.copyOf()
    }


}