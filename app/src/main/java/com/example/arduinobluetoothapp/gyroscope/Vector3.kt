package com.example.arduinobluetoothapp.gyroscope

import kotlin.math.sqrt

class Vector3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    fun magnitude(): Float{
        return sqrt(x*x + y*y + z*z)
    }

    operator fun times(other: Vector3): Float{
        return x*other.x + y*other.y + z*other.z
    }
}