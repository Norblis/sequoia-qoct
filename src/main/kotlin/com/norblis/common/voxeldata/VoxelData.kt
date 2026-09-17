package com.norblis.common.voxeldata

interface VoxelData<out T> {
    val shapeX: Int
    val shapeY: Int
    val shapeZ: Int

    val shape: Triple<Int, Int, Int>
        get() = Triple(shapeX, shapeY, shapeZ)

    val size: Int

    /**
     * Get the value with coordinate (x,y,z)
     */
    fun get(x: Int, y: Int, z: Int): T
    fun get(index: Int): T

    fun forEachCoordinate(action: (x: Int, y: Int, z: Int, v: T) -> Unit)
    fun forEachIndexed(action: (index: Int, n: T) -> Unit)
}

interface MutableVoxelData<T> : VoxelData<T> {
    fun set(x: Int, y: Int, z: Int, value: T)
    fun resize(sizeX: Int, sizeY: Int, sizeZ: Int)

    fun updateFrom(from: VoxelData<T>) {
        resize(from.shapeX, from.shapeY, from.shapeZ)
        from.forEachCoordinate { x, y, z, v ->
            set(x, y, z, v)
        }
    }
}