package com.norblis.qoct

import java.io.File
import java.lang.AutoCloseable
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Create a reader to handle QOCT stored as a zip file. It is recommended that checkEntries()
 * is called before usage to ensure no file entries are missing.
 */
class QoctSpc3ZipReader(file: File) : QoctFileReader, AutoCloseable {
    val zipFile = ZipFile(file)

    override var cols: Int = 0
    override var rows: Int = 0
    override var framesPerStep: Int = 0
    override var stepCount: Int = 0
    override var segmentCount: Int = 0

    // segmentLookup(step, segment)
    var segmentLookup = ArrayList<ArrayList<ZipEntry?>>()

    private var fileEntries = ArrayList<ZipEntry>().apply {
        for (entry in zipFile.entries()) {
            if (!entry.isDirectory) {
                add(entry)
            }
        }
    }

    fun checkEntries(): Error {
        for (step in 0 until stepCount) {
            for (segment in 0 until segmentCount) {
                val entry = segmentLookup[step][segment]
                if (entry == null) {
                    return Error.MISSING_ENTRY
                }
            }
        }
        return Error.NONE
    }

    init {
        initLookup()
        readFormat()
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

    private fun entryStrippedName(string: String): String {
        val index = string.indexOfLast { it == '/' }
        return if (index == -1) {
            // Already stripped
            string
        } else {
            string.substring(startIndex = index + 1)
        }
    }

    private fun initLookup() {
        var stepIndexMax = Int.MIN_VALUE
        var segmentIndexMax = Int.MIN_VALUE
        for (entry in fileEntries) {
            val name = entryStrippedName(entry.name)
            val stepIndex = stepIndex(name)
            val segmentIndex = segmentIndex(name)
            if (stepIndex > stepIndexMax) stepIndexMax = stepIndex
            if (segmentIndex > segmentIndexMax) segmentIndexMax = segmentIndex
        }

        stepCount = stepIndexMax + 1
        segmentCount = segmentIndexMax + 1

        segmentLookup.ensureCapacity(stepCount)
        repeat(stepCount) {
            val arr = ArrayList<ZipEntry?>(segmentCount)
            segmentLookup.add(arr)
            repeat(segmentCount) {
                arr.add(null)
            }
        }

        for (entry in fileEntries) {
            val name = entryStrippedName(entry.name)
            val stepIndex = stepIndex(name)
            val segmentIndex = segmentIndex(name)
            segmentLookup[stepIndex][segmentIndex] = entry
        }
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

    // Safe to use in parallel if not reading from the same entry.
    override fun readSegment(stepIndex: Int, segmentIndex: Int): ByteArray {
        val entry = segmentLookup[stepIndex][segmentIndex]!!
        lateinit var content: ByteArray
        zipFile.getInputStream(entry).use {
            content = it.readBytes()
        }
        return content
    }

    override fun close() {
        zipFile.close()
    }

    enum class Error() {
        NONE, MISSING_ENTRY
    }
}