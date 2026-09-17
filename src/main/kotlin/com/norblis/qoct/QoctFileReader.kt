package com.norblis.qoct

// Reads files.
interface QoctFileReader {
    // Safe to use in parallel if not reading from the same entry.
    fun readSegment(stepIndex: Int, segmentIndex: Int): ByteArray

    val cols: Int
    val rows: Int
    val framesPerStep: Int
    val segmentCount: Int
    val stepCount: Int
}