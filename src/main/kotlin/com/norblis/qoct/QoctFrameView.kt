package com.norblis.qoct

open class QoctFrameView(
    open val byteArray: ByteArray,
    open val offset: Int,
    open val cols: Int,
    open val rows: Int,
) {
    fun get(x: Int, y: Int): Byte {
        val i = x + y * cols
        return byteArray[offset + i]
    }

    fun get(index: Int): Byte {
        return byteArray[offset + index]
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Frame(cols=$cols, rows=$rows, offset=$offset)\n")
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                sb.append(get(x, y).toUByte()).append(" ") // show as unsigned
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}