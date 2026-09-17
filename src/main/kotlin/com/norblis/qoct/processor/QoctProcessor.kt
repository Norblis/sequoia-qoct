package com.norblis.qoct.processor

import com.norblis.common.concurrency.suspendingParallelFor
import com.norblis.common.progress.MutableProgressState
import com.norblis.qoct.QoctData
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlin.math.min

class QoctProcessor(
    private var data: QoctData, private val progressUpdater: MutableProgressState? = null,
) {

    /**
     * Processes all frames for a list of operations.
     */
    suspend fun processAllFrames(
        threads: Int, operations: List<QoctOperation>,
    ) {
        processSampledFrames(data.framesPerStep, threads, operations)
    }

    suspend fun processAllFrames(
        threads: Int, vararg operations: QoctOperation,
    ) {
        processAllFrames(threads, operations.toList())
    }

    /**
     * Process the first 'samplesPerStep' frames of each step
     */
    suspend fun processSampledFrames(
        samplesPerStep: Int, threads: Int, operations: List<QoctOperation>,
    ) {
        require(operations.isNotEmpty())

        val effectiveSamplesPerStep = min(data.framesPerStep, samplesPerStep)
        val effectiveTotalFrameCount = effectiveSamplesPerStep * data.stepCount

        for (operation in operations) {
            operation.setup(threads, data.cols, data.rows, data.stepCount, effectiveSamplesPerStep)
        }
        val frameCountArray = LongArray(threads)

        progressUpdater?.begin(0, effectiveTotalFrameCount.toLong())
        val progressUpdateRate = min(DEFAULT_PROGRESS_UPDATE_RATE, effectiveTotalFrameCount / 100)
        var nextProgressUpdate = 0

        suspendingParallelFor(threads, data.stepCount) { step, thread, _ ->
            val iterator = data.get(step)
            var frameIndex = 0
            while (iterator.hasNext() && frameIndex < effectiveSamplesPerStep && coroutineContext.isActive) {
                val frame = iterator.next()
                for (op in operations) {
                    op.processFrame(frame, frameIndex, step, thread)
                }
                ++frameCountArray[thread]
                ++frameIndex

                if (thread == 0 && progressUpdater != null && frameCountArray[thread] > nextProgressUpdate) {
                    progressUpdater.progress = frameCountArray.sum() // Only estimation due to shared memory access.
                    nextProgressUpdate += progressUpdateRate
                }
            }
        }

        val totalFrameCount = frameCountArray.sum()

        for (op in operations) {
            op.finish(totalFrameCount)
        }
        progressUpdater?.finish()
    }

    suspend fun processSampledFrames(
        samplesPerStep: Int, threads: Int, vararg operations: QoctOperation,
    ) {
        processSampledFrames(samplesPerStep, threads, operations.toList())
    }


    // Estimates the two brightest pixels by looking at a sample of the frames
    suspend fun findTwoBrightest(samplesPerStep: Int, threads: Int): PixelPair {
        require(samplesPerStep > 0)
        require(threads > 0)
        val frameSize = data.cols * data.rows
        require(frameSize >= 2) { "Frame size too small" }

        val effectiveSamplesPerStep = min(samplesPerStep, data.framesPerStep)
        val sumArrays = Array(threads) { LongArray(frameSize) }

        suspendingParallelFor(threads, data.stepCount) { step, thread, iterations ->
            val iterator = data.get(step)
            var frameIndex = 0L
            val sumArray = sumArrays[thread]
            while (iterator.hasNext() && frameIndex < effectiveSamplesPerStep && coroutineContext.isActive) {
                val frame = iterator.next()
                for (i in 0 until frameSize) {
                    sumArray[i] = sumArray[i] + frame.get(i)
                }
                ++frameIndex
            }
        }

        // Collect result from each thread.
        val sumArray = LongArray(frameSize) { i ->
            sumArrays.sumOf { it[i] }
        }


        // Find the two largest elements.
        var highestValue = -1L
        var secondHighestValue = -1L
        var highestIndex = 0
        var secondHighestIndex = 0

        if (sumArray[0] > sumArray[1]) {
            highestValue = sumArray[0]
            highestIndex = 0
            secondHighestIndex = 1
            secondHighestValue = sumArray[1]
        } else {
            highestValue = sumArray[1]
            highestIndex = 1
            secondHighestValue = sumArray[0]
            secondHighestIndex = 0
        }

        for (i in sumArray.indices) {
            val e = sumArray[i]
            if (e > highestValue) {
                secondHighestValue = highestValue
                secondHighestIndex = highestIndex
                highestValue = e
                highestIndex = i
            } else if (e > secondHighestValue) {
                secondHighestValue = e
                secondHighestIndex = i
            }
        }

        val pixelPair = PixelPair(
            highestIndex % data.cols,
            highestIndex / data.cols,
            secondHighestIndex % data.cols,
            secondHighestIndex / data.cols
        )
        return pixelPair
    }

    data class PixelPair(val x0: Int, val y0: Int, val x1: Int, val y1: Int) {
        constructor(pixel1: Pixel, pixel2: Pixel) : this(pixel1.x, pixel1.y, pixel2.x, pixel2.y)

        override fun toString(): String {
            return "($x0, $y0), ($x1, $y1)"
        }
    }

    data class Pixel(val x: Int, val y: Int) {
        override fun toString(): String {
            return "($x, $y)"
        }
    }

    companion object {
        const val DEFAULT_PROGRESS_UPDATE_RATE = 32768
    }
}