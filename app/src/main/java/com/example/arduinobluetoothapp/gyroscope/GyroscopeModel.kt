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
}
