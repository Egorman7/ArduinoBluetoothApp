package com.example.arduinobluetoothapp.gyroscope

data class GyroscopeModel(
    val x: Float,
    val y: Float,
    val z: Float,
    val heading: Float,
    val pitch: Float,
    val roll: Float
){
    override fun toString(): String {
        return "[x]: $x, [y]: $y, [z]: $z, " +
                "[h]: $heading, [p]: $pitch, [r]: $roll"
    }

    operator fun minus(other: GyroscopeModel): GyroscopeModel{
        return GyroscopeModel(x - other.x, y-other.y, z-other.z, heading - other.heading, pitch-other.pitch, roll-other.roll)
    }

    fun toVector3():Vector3{
        return Vector3(x, y, z)
    }
}
