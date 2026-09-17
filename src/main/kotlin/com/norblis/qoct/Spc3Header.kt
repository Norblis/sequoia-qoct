package com.norblis.qoct

import java.nio.ByteBuffer
import java.nio.ByteOrder

class Spc3Header(
    data: ByteArray
) {
    val cols: Int
    val rows: Int
    val bbp: Int
    val integrationTime: Int
    val frameCount: Int

    init {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        cols = buffer.get(HEADER_OFFSET_COLS).toInt()
        rows = buffer.get(HEADER_OFFSET_ROWS).toInt()
        bbp = buffer.get(HEADER_OFFSET_BPP).toInt()
        integrationTime = buffer.getShort(HEADER_OFFSET_INTEGRATION_TIME).toInt() // TODO: This one might be off.
        frameCount = buffer.getInt(HEADER_OFFSET_FRAME_CNT)
    }

    companion object {
        const val HEADER_SIZE = 1032

        private const val HEADER_OFFSET_ROWS = 108
        private const val HEADER_OFFSET_COLS = 109
        private const val HEADER_OFFSET_BPP = 110
        private const val HEADER_OFFSET_INTEGRATION_TIME = 112
        private const val HEADER_OFFSET_FRAME_CNT = 122
    }
}