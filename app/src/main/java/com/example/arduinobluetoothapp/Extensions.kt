package com.example.arduinobluetoothapp

import android.widget.TextView
import com.example.arduinobluetoothapp.gyroscope.GyroscopeModel

fun TextView.writeMessage(message: String?){
    if(message!=null) append("$message\n")
}

fun TextView.writeMessage(tag: String?, messsage: String?){
    writeMessage("[$tag]: $messsage")
}

fun String.parseGyroscopeModel(): GyroscopeModel{
    val data = this.split("/")
    return GyroscopeModel(data[0].toFloat(), data[1].toFloat(), data[2].toFloat(), data[5].toFloat(), data[4].toFloat(), data[3].toFloat())
}