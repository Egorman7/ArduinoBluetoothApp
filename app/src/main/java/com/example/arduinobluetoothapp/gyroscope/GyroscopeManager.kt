package com.example.arduinobluetoothapp.gyroscope

import android.util.Log
import com.example.arduinobluetoothapp.GyroscopeData
import kotlin.math.abs

class GyroscopeManager {
    private var gyroscopeModel: GyroscopeModel? = null

    var threshold = 1.3f

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

    fun getTrackedDelta(other: GyroscopeModel?, value: GyroscopeData) : Float?{
        if(gyroscopeModel == null){
            gyroscopeModel = other
            return null
        }
        val delta = getSignedGyroDelta(other)
        //Log.d("Manager", value.name)
        val dataMap = mutableMapOf(
                GyroscopeData.X to delta.x,
                GyroscopeData.Y to delta.y,
                GyroscopeData.Z to delta.z,
                GyroscopeData.HEADING to delta.heading,
                GyroscopeData.PITCH to delta.pitch,
                GyroscopeData.ROLL to delta.roll
        )
        //Log.d("Manager", "$dataMap")
        gyroscopeModel = other
        return if(abs(dataMap[value]!!) > threshold) dataMap[value] else null
    }

    private fun getSignedGyroDelta(other: GyroscopeModel?): GyroscopeModel{
        return GyroscopeModel(gyroscopeModel!!.x - other!!.x, gyroscopeModel!!.y - other.y,
            gyroscopeModel!!.z - other.z, gyroscopeModel!!.heading - other.heading,
            gyroscopeModel!!.pitch - other.pitch, gyroscopeModel!!.roll - other.roll)
    }
}