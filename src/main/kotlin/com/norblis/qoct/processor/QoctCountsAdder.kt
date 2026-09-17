package com.norblis.qoct.processor

import com.norblis.common.voxeldata.MutableVoxelData
import com.norblis.qoct.QoctFrameView

class QoctCountsAdder(
    var pixel: QoctProcessor.Pixel,
) : QoctOperation {

    private var counts = LongArray(0)

    private var cols: Int = -1
    private var rows: Int = -1
    private var threads: Int = -1

    private var result: LongArray? = null

    override fun setup(threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int) {
        this.threads = threads
        this.cols = cols
        this.rows = rows

        counts = LongArray(stepCount) { 0L }
    }

    override fun processFrame(frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int) {
        val n = frameView.get(pixel.x, pixel.y)
        counts[step] += n.toLong()
    }

    override fun finish(frameCount: Long) {
        result = counts.copyOf()
    }

    fun resultAsCounts(voxelData: MutableVoxelData<Long>) {
        check(result != null)
        voxelData.resize(1, 1, result!!.size)
        for (i in result!!.indices) {
            val counts = result!![i]
            voxelData.set(0, 0, i, counts)
        }
    }

}