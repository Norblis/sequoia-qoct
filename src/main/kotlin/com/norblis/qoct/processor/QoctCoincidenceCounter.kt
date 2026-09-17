package com.norblis.qoct.processor

import com.norblis.common.voxeldata.MutableVoxelData
import com.norblis.qoct.QoctFrameView

class QoctCoincidenceCounter(
    var pixelPair: QoctProcessor.PixelPair,
) : QoctOperation {

    private var coincidenceCounts = LongArray(0)
    private var accidentalCoincidenceCounts = LongArray(0)

    private var framesPerStep: Int = -1
    private var cols: Int = -1
    private var rows: Int = -1
    private var threads: Int = -1

    private var hasResult = false

    private var lastFramePixel1Value: Byte = 0

    override fun setup(threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int) {
        this.threads = threads
        this.cols = cols
        this.rows = rows
        this.framesPerStep = framesPerStep

        coincidenceCounts = LongArray(stepCount) { 0L }
        accidentalCoincidenceCounts = LongArray(stepCount) { 0L }
    }


    override fun processFrame(frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int) {
        // Normal coincidences.
        val n = frameView.get(pixelPair.x0, pixelPair.y0) * frameView.get(pixelPair.x1, pixelPair.y1)
        coincidenceCounts[step] += n.toLong()

        // Accidental coincidences.
        val m = if (frameIndex == 0) {
            frameView.get(pixelPair.x0, pixelPair.y0) * frameView.get(pixelPair.x1, pixelPair.y1)
        } else {
            frameView.get(pixelPair.x0, pixelPair.y0) * lastFramePixel1Value
        }
        lastFramePixel1Value = frameView.get(pixelPair.x1, pixelPair.y1)
        accidentalCoincidenceCounts[step] += m.toLong()
    }

    override fun finish(frameCount: Long) {
        hasResult = true
    }

    fun resultAsCoincidenceCounts(voxelData: MutableVoxelData<Long>) {
        check(hasResult)
        voxelData.resize(1, 1, coincidenceCounts.size)
        for (i in coincidenceCounts.indices) {
            val counts = coincidenceCounts[i]
            voxelData.set(0, 0, i, counts)
        }
    }

    fun totalCoincidenceCount(): Long {
        return coincidenceCounts.sum()
    }

    fun coincidenceCount(step: Int): Long {
        return coincidenceCounts[step]
    }



    fun resultAsAccidentalCoincidenceCounts(voxelData: MutableVoxelData<Long>) {
        check(hasResult)
        voxelData.resize(1, 1, accidentalCoincidenceCounts.size)
        for (i in accidentalCoincidenceCounts.indices) {
            val counts = accidentalCoincidenceCounts[i]
            voxelData.set(0, 0, i, counts)
        }
    }
}