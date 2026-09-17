package com.norblis.qoct

import java.io.File

class QoctSpc3DirectoryReader(val directory: File) : QoctFileReader {

    override var cols: Int = 0
    override var rows: Int = 0
    override var framesPerStep: Int = 0
    override var stepCount: Int = 0
    override var segmentCount: Int = 0

    override fun readSegment(stepIndex: Int, segmentIndex: Int): ByteArray {
        val file = findFile(stepIndex, segmentIndex)
        return file.readBytes()
    }

    fun checkEntries(): Error {
        for (step in 0 until stepCount) {
            for (segment in 0 until segmentCount) {
                val file = findFile(step, segment)
                if (!file.exists()) {
                    return Error.MISSING_ENTRY
                }
            }
        }
        return Error.NONE
    }

    init {
        if (!directory.isDirectory) {
            throw IllegalArgumentException("Not a directory.")
        }

        var stepIndexMax = Int.MIN_VALUE
        var segmentIndexMax = Int.MIN_VALUE
        for (file in directory.listFiles()) {
            val name = file.name
            val stepIndex = stepIndex(name)
            val segmentIndex = segmentIndex(name)
            if (stepIndex > stepIndexMax) stepIndexMax = stepIndex
            if (segmentIndex > segmentIndexMax) segmentIndexMax = segmentIndex
        }
        stepCount = stepIndexMax + 1
        segmentCount = segmentIndexMax + 1

        readFormat()
    }

    fun findFile(stepIndex: Int, segmentIndex: Int): File {
        return File("${directory.path}/QOCT${stepIndex}_R${segmentIndex}.spc3")
    }

    // Reads the header of the first and last file of a step to determine data format.
    private fun readFormat() {
        val content = readSegment(0, 0)
        val headerFirst = Spc3Header(content)

        if (headerFirst.bbp != 8) {
            RuntimeException("Unsupported Bytes Per Pixel")
        }
        cols = headerFirst.cols
        rows = headerFirst.rows

        var headerLast = Spc3Header(readSegment(0, segmentCount - 1))

        framesPerStep = headerFirst.frameCount * (segmentCount - 1) + headerLast.frameCount
    }

    private fun stepIndex(strippedName: String): Int {
        val index = strippedName.indexOf('_')
        val stringNumber = strippedName.substring(4, index)
        return stringNumber.toInt()
    }

    private fun segmentIndex(strippedName: String): Int {
        val indexStart = strippedName.indexOf('R') + 1
        val indexEnd = strippedName.indexOf('.')
        val stringNumber = strippedName.substring(indexStart, indexEnd)
        return stringNumber.toInt()
    }

    enum class Error() {
        NONE, MISSING_ENTRY
    }
}