package com.norblis.qoct.processor

import com.norblis.common.math.scaleToRange
import com.norblis.qoct.QoctFrameView
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

// Calculates the average of provided frames and generates an image
class QoctFrameAverager : QoctOperation {

    private var sumArrays = Array(0) { LongArray(0) }

    private var cols: Int = -1
    private var rows: Int = -1
    private var threads: Int = -1
    private var frameSize: Int = -1

    private var totalFrameCount = -1L

    private var resultSum: LongArray? = null

    val result: DoubleArray?
        get() {
            return if (resultSum == null) {
                null
            } else {
                val sums = resultSum!!
                val averageArray = DoubleArray(sums.size)
                for (i in averageArray.indices) {
                    averageArray[i] = sums[i].toDouble() / totalFrameCount.toDouble()
                }
                averageArray
            }
        }

    fun resultAsImage(): BufferedImage? {
        return if (resultSum == null) {
            null
        } else {
            // Create image.
            val sums = resultSum!!
            val min = sums.minOrNull()!!.toDouble()
            val max = sums.maxOrNull()!!.toDouble()
            val image = BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY)
            val dataArray = (image.raster.dataBuffer as DataBufferByte).data
            for (i in 0 until frameSize) {
                val d = scaleToRange(min, max, 0.0, 255.0, sums[i].toDouble())
                dataArray[i] = d.toInt().toByte()
            }
            image
        }
    }

    override fun setup(threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int) {
        this.threads = threads
        this.cols = cols
        this.rows = rows

        frameSize = cols * rows
        if (frameSize < 2) {
            throw IllegalStateException("Frame size too small.")
        }
        sumArrays = Array(threads) {
            LongArray(frameSize)
        }
    }

    override fun processFrame(frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int) {
        for (i in 0 until frameSize) {
            sumArrays[thread][i] = frameView.get(i) + sumArrays[thread][i]
        }
    }

    override fun finish(frameCount: Long) {
        // Collect all sums
        resultSum = LongArray(frameSize)
        for (arr in sumArrays) {
            for (i in arr.indices) {
                resultSum!![i] += arr[i]
            }
        }
        totalFrameCount = frameCount
    }
}