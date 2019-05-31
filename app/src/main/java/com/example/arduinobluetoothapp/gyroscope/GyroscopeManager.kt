package com.example.arduinobluetoothapp.gyroscope

import com.example.arduinobluetoothapp.GyroscopeData
import com.example.arduinobluetoothapp.writeMessage
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs
import kotlin.math.acos

class GyroscopeManager {
    private var gyroscopeZero: GyroscopeModel? = null
    private var gyroscopeModel: GyroscopeModel? = null
    var threshold = 1.3f

    fun getTrackedDelta(other: GyroscopeModel?, value: GyroscopeData) : Float?{
        if(gyroscopeModel == null){
            gyroscopeModel = other
            return null
        }
        if(value == GyroscopeData.ANGLE) return getAngle(other?.toVector3())
        val delta = getSignedGyroDelta(other)
        val dataMap = mutableMapOf(
                GyroscopeData.X to delta.x,
                GyroscopeData.Y to delta.y,
                GyroscopeData.Z to delta.z,
                GyroscopeData.HEADING to delta.heading,
                GyroscopeData.PITCH to delta.pitch,
                GyroscopeData.ROLL to delta.roll
        )
        gyroscopeModel = other
        return if(abs(dataMap[value]!!) > threshold) dataMap[value] else null
    }

    private fun getAngle(other: Vector3?): Float{
        return if(other!=null) acos((other * getZeroVector()) / (other.magnitude() * getZeroVector().magnitude())) else 0f
    }

    private fun getSignedGyroDelta(other: GyroscopeModel?): GyroscopeModel{
        return GyroscopeModel(gyroscopeModel!!.x - other!!.x, gyroscopeModel!!.y - other.y,
            gyroscopeModel!!.z - other.z, gyroscopeModel!!.heading - other.heading,
            gyroscopeModel!!.pitch - other.pitch, gyroscopeModel!!.roll - other.roll)
    }

    fun setGyroZero(data: GyroscopeModel){
        gyroscopeZero = data
    }

    fun getVector3Direction(current: GyroscopeModel?): Vector3{
        if(current == null || gyroscopeZero==null) return Vector3(0f,0f,0f)
        val d = current - gyroscopeZero!!
        return Vector3(d.x, d.y, d.z)
    }

    fun getZeroVector(): Vector3{
        if(gyroscopeZero == null) return Vector3(0f,0f,0f) else
        return Vector3(gyroscopeZero!!.x, gyroscopeZero!!.y, gyroscopeZero!!.z)
    }


    // currently unused
    fun getBiggestDeltaData(other: GyroscopeModel?) : Pair<GyroscopeData, Float>?{
        if(gyroscopeModel == null){
            gyroscopeModel = other
            return null
        }
        val delta = getSignedGyroDelta(other)
        val dataMap = mutableMapOf(
            GyroscopeData.X to delta.x,
            GyroscopeData.Y to delta.y,
            GyroscopeData.Z to delta.z,
            GyroscopeData.HEADING to delta.heading,
            GyroscopeData.PITCH to delta.pitch,
            GyroscopeData.ROLL to delta.roll
        )
        gyroscopeModel = other
        dataMap.maxBy { abs(it.value) }?.let{
            if(abs(it.value) >= threshold) return Pair(it.key, it.value)
        }
        return null
    }
}