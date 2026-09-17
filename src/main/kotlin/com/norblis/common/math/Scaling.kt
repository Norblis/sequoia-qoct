package com.norblis.common.math

fun scaleToRange(min: Float, max: Float, newMin: Float, newMax: Float, value: Float): Float {
    return ((value - min) / (max - min)) * (newMax - newMin) + newMin
}

fun scaleToRange(min: Double, max: Double, newMin: Double, newMax: Double, value: Double): Double {
    return ((value - min) / (max - min)) * (newMax - newMin) + newMin
}

fun normalize(min: Float, max: Float, value: Float): Float {
    return ((value - min) / (max - min)).coerceIn(0.0f, 1.0f)
}

fun normalize(min: Double, max: Double, value: Double): Double {
    return ((value - min) / (max - min)).coerceIn(0.0, 1.0)
}