package com.norblis.common.voxeldata

class VoxelDataImpl<T>(
    private val init: () -> T,
) : MutableVoxelData<T> {

    override var shapeX: Int = 0
        private set
    override var shapeY: Int = 0
        private set
    override var shapeZ: Int = 0
        private set

    override val size: Int
        get() = shapeX * shapeY * shapeZ

    private val buffer = ArrayList<T>()

    override fun get(x: Int, y: Int, z: Int): T {
        val index = x + y * shapeX + z * shapeX * shapeY
        return buffer[index]
    }

    override fun get(index: Int): T {
        return buffer[index]
    }

    override fun forEachCoordinate(action: (x: Int, y: Int, z: Int, v: T) -> Unit) {
        for (x in 0 until shapeX)
            for (y in 0 until shapeY)
                for (z in 0 until shapeZ)
                    action(
                        x, y, z, get(x, y, z)
                    )
    }

    override fun forEachIndexed(action: (index: Int, n: T) -> Unit) {
        for (i in 0 until size)
            action(i, buffer[i])
    }


    override fun set(x: Int, y: Int, z: Int, value: T) {
        val index = x + y * shapeX + z * shapeX * shapeY
        buffer[index] = value
    }

    override fun resize(sizeX: Int, sizeY: Int, sizeZ: Int) {
        buffer.clear()
        val newSize = sizeX * sizeY * sizeZ
        buffer.ensureCapacity(newSize)
        repeat(newSize) { buffer.add(init()) }
    }
}