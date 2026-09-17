package com.norblis.qoct

// Provides QOCT data from a file reader.
class QoctFileData(var dataFile: QoctFileReader) : QoctData {

    override val cols: Int
        get() = dataFile.cols
    override val rows: Int
        get() = dataFile.rows
    override val stepCount: Int
        get() = dataFile.stepCount
    override val framesPerStep: Int
        get() = dataFile.framesPerStep
    override val totalFrameCount: Long
        get() = stepCount.toLong() * dataFile.framesPerStep.toLong()


    override fun get(stepIndex: Int): Iterator<QoctFrameView> {
        return object : Iterator<QoctFrameView> {
            val src = dataFile
            var currentSegmentBytes = src.readSegment(stepIndex, 0)
            var currentHeader = Spc3Header(currentSegmentBytes)
            var frameSegmentOffset = 0

            val frame = MutableQoctFrameView(ByteArray(0), 0, src.cols, src.rows)
            val frameSize = src.cols * src.rows

            var currentSegment = 0
            var currentFrame = 0

            override fun hasNext(): Boolean {
                return currentFrame < src.framesPerStep
            }

            override fun next(): QoctFrameView {
                // Setup frame view
                if (frameSegmentOffset >= currentHeader.frameCount) {
                    // Load next segment.
                    ++currentSegment
                    currentSegmentBytes = src.readSegment(stepIndex, currentSegment)
                    frameSegmentOffset = 0
                }
                frame.byteArray = currentSegmentBytes
                frame.offset = Spc3Header.HEADER_SIZE + (frameSize * frameSegmentOffset)

                ++currentFrame
                ++frameSegmentOffset

                return frame
            }
        }
    }

    override fun frameIterator(): Iterator<QoctFrameView> {
        return object : Iterator<QoctFrameView> {
            var currentStep = 0
            var currentIterator = get(currentStep)


            override fun hasNext(): Boolean {
                return if (currentIterator.hasNext()) {
                    true
                } else if (currentStep < stepCount - 1) {
                    dataFile.framesPerStep != 0
                } else {
                    false
                }
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
}