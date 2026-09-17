package com.norblis.qoct

/**
 * @param stepData The data for each step of the scan. Each step is a sequence of frames.
 * @param frameCols The number of columns in each frame.
 * @param frameRows The number of rows in each frame.
 */
class QoctByteData(
    val stepData: Array<ByteArray>,
    frameCols: Int,
    frameRows: Int
) : QoctData {


    private class FrameIterator(
        private val data: ByteArray,
        private val cols: Int,
        private val rows: Int
    ) : Iterator<QoctFrameView> {
        private var offset = 0
        private val frameSize = cols * rows

        override fun next(): QoctFrameView {
            val frame = QoctFrameView(data, offset, cols, rows)
            offset += frameSize
            return frame
        }

        override fun hasNext(): Boolean {
            return offset < data.size
        }
    }

    override fun get(stepIndex: Int): Iterator<QoctFrameView> {
        return FrameIterator(stepData[stepIndex], cols, rows)
    }

    override fun frameIterator(): Iterator<QoctFrameView> {
        val totalSteps = stepData.size
        return object : Iterator<QoctFrameView> {
            private var currentStep = 0
            private var currentIterator = get(currentStep)

            override fun hasNext(): Boolean {
                if (currentIterator.hasNext()) {
                    return true
                }
                // We are at the last frame of the current step.
                return currentStep < totalSteps - 1
            }

            override fun next(): QoctFrameView {
                if (!currentIterator.hasNext()) {
                    currentStep++
                    currentIterator = get(currentStep)
                }
                return currentIterator.next()
            }
        }
    }

    override val framesPerStep: Int = stepData.getOrNull(0)?.size ?: 0

    init {
        for (frames in stepData) {
            require(frames.size == framesPerStep) { "All steps must have the same number of frames" }
        }
    }

    override val stepCount: Int
        get() = stepData.size

    override val totalFrameCount: Long
        get() = framesPerStep.toLong() * stepCount

    override val cols: Int = frameCols
    override val rows: Int = frameRows
}