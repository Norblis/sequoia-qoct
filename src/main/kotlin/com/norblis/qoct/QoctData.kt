package com.norblis.qoct

interface QoctData {
    /** Get an iterator over all frames for a step.
     *  Thread safe for different stepIndex.
     *  The QoctFrameView object is reused, and will be modified the next time next() is called.
     */
    fun get(stepIndex: Int): Iterator<QoctFrameView>
    fun frameIterator(): Iterator<QoctFrameView>

    val totalFrameCount: Long
    val framesPerStep: Int
    val stepCount: Int
    val cols: Int
    val rows: Int

    val frameSize: Int
        get() = cols * rows
}