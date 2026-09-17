package com.norblis.qoct.processor

import com.norblis.qoct.QoctFrameView

class QoctEffectiveFrameCounter(private val pixels: List<QoctProcessor.Pixel>) : QoctOperation {

    private var counts = IntArray(0)
    private var done = false

    override fun setup(threads: Int, cols: Int, rows: Int, stepCount: Int, framesPerStep: Int) {
        counts = IntArray(stepCount)
    }

    override fun processFrame(frameView: QoctFrameView, frameIndex: Int, step: Int, thread: Int) {
        var isEffective = false

        // A frame is effective if it has a value for one of the pixels.
        // If the pixel list is empty; check all pixels.
        if (pixels.isEmpty()) {
            for (x in 0 until frameView.cols) {
                for (y in 0 until frameView.rows) {
                    if (frameView.get(x, y) > 0) {
                        isEffective = true
                        break
                    }
                    if (isEffective) break
                }
            }
        } else {
            // Check the pixels in the list.
            for (pixel in pixels) {
                if (frameView.get(pixel.x, pixel.y) > 0) {
                    isEffective = true
                    break
                }
            }
        }

        if (isEffective) {
            ++counts[step]
        }
    }

    override fun finish(frameCount: Long) {
        done = true
    }

    fun getFrameCounts(): IntArray {
        check(done)
        return counts.copyOf()
    }


}