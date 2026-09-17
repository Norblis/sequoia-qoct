package com.norblis.qoct

class MutableQoctFrameView(
    override var byteArray: ByteArray,
    override var offset: Int,
    override var cols: Int,
    override var rows: Int,
) : QoctFrameView(byteArray, offset, cols, rows)