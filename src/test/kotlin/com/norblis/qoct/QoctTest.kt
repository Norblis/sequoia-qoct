package com.norblis.qoct

import com.norblis.qoct.processor.QoctCoincidenceCounter
import com.norblis.qoct.processor.QoctProcessor
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QoctTest {

    @Test
    fun simpleQoctTest() = runTest {
        // 5 Frames of size 5x1.
        val step0Data = byteArrayOf(
            0, 1, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 1, 0, 1, 1,
            0, 0, 0, 0, 1,
            1, 1, 1, 1, 1
        )

        val step1Data = byteArrayOf(
            0, 1, 1, 1, 0,
            0, 0, 0, 1, 0,
            0, 1, 1, 0, 0,
            0, 1, 0, 0, 1,
            0, 0, 0, 1, 0
        )
        val expectedBrightest = QoctProcessor.PixelPair(1, 0, 3, 0)
        // Step 0 has 1 coincidence in frame 2 and 4; step 1 has 1 coincidence in frame 0.
        val expectedCoincidenceCountStep0 = 2L
        val expectedCoincidenceCountStep1 = 1L

        val data = arrayOf(step0Data, step1Data)

        val qoctData = QoctByteData(data, 5, 1)
        val processor = QoctProcessor(qoctData)

        val pair = processor.findTwoBrightest(5, 1)
        assertEquals(expectedBrightest, pair)


        val coincidenceCounter = QoctCoincidenceCounter(pair)
        val operations = listOf(
            coincidenceCounter
        )
        processor.processAllFrames(1, operations)

        val coincidenceCountStep0 = coincidenceCounter.coincidenceCount(0)
        val coincidenceCountStep1 = coincidenceCounter.coincidenceCount(1)

        assertEquals(expectedCoincidenceCountStep0, coincidenceCountStep0)
        assertEquals(expectedCoincidenceCountStep1, coincidenceCountStep1)
    }
}