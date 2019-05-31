package com.example.arduinobluetoothapp

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RadioButton
import android.widget.SeekBar
import com.example.arduinobluetoothapp.gyroscope.GyroscopeManager
import kotlinx.android.synthetic.main.activity_main.*
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.interfaces.DeviceCallback
import java.lang.IllegalStateException
import kotlin.math.abs
import kotlin.math.acos

class MainActivity : AppCompatActivity() {

    private val manager: GyroscopeManager by lazy {
        GyroscopeManager()
    }

    private var calibrateClicked: Boolean = false

    private val bt: Bluetooth by lazy {
        Bluetooth(this).apply {
            setDeviceCallback(object: DeviceCallback{
                override fun onDeviceDisconnected(device: BluetoothDevice?, message: String?) {
                    runOnUiThread {
                        main_device_selection_text.text = "No connected device!"
                        main_log.writeMessage("Device","Disconnected!" )
                    }
                }

                override fun onDeviceConnected(device: BluetoothDevice?) {
                    runOnUiThread {
                        main_device_selection_text.text = device?.name
                        main_log.writeMessage("Device", "Connected!")
                    }
                }

                override fun onConnectError(device: BluetoothDevice?, message: String?) {
                    runOnUiThread{
                        main_log.writeMessage("Device", "Connection error!")
                    }
                }

                override fun onMessage(message: String?) {
                    runOnUiThread {
                        val gyroData = message?.parseGyroscopeModel()
                        if(calibrateClicked && gyroData!=null){
                            manager.setGyroZero(gyroData)
                            calibrateClicked = false
                            main_log.writeMessage("Zero", "Calibrated to $gyroData")
                        }
                        val dim = getTrackedDim(findViewById<RadioButton>(main_radiogroup.checkedRadioButtonId).text.toString())
                        val delta = manager.getTrackedDelta(gyroData, dim)
                        if(delta!=null){
                            if(dim != GyroscopeData.ANGLE) {
                                main_log.writeMessage(dim.name, "$delta \t" + if (delta > 0f) "RIGHT" else "LEFT")

                                if (main_toogle_brightness.isChecked) {
                                    setWindowBness(delta)
                                }
                                if (main_toogle_volume.isChecked) {
                                    setMediaVolume(delta)
                                }
                            } else {
                                if(delta > 0.2f){
                                    val dd = delta - 0.2f
                                    if (main_toogle_brightness.isChecked) {
                                        setWindowBness(delta)
                                    }
                                    if (main_toogle_volume.isChecked) {
                                        setMediaVolume(delta)
                                    }
                                }
                                else{
                                    val dd = -0.2f
                                    if (main_toogle_brightness.isChecked) {
                                        setWindowBness(delta)
                                    }
                                    if (main_toogle_volume.isChecked) {
                                        setMediaVolume(delta)
                                    }
                                }
                            }
                        }
//                        if(gyroData!=null) {
//                            val vec = gyroData.toVector3()
//                            val angle = acos((vec * manager.getZeroVector()) / (vec.magnitude() * manager.getZeroVector().magnitude()))
//                            main_log.writeMessage("Angle", "$angle")
//                        }
                        main_scroll.fullScroll(View.FOCUS_DOWN)
                    }
                }

                override fun onError(errorCode: Int) {
                    runOnUiThread{
                        main_log.writeMessage("Error", "Error with code [$errorCode]")
                    }
                }
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_sense.max = 500
        main_sense.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val t =(if(seekBar!!.progress < 50) 50f else seekBar.progress.toFloat())/100f
                manager.threshold = t
                main_sense_text.text = "$t"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        main_sense.progress = 135

        main_device_selection_button.setOnClickListener{
            if(bt.isConnected) bt.disconnect()
            connectToDevice()
        }

        main_button_calibrate.setOnClickListener {
            calibrateClicked = true
        }

    }

    override fun onStart() {
        super.onStart()
        bt.onStart()
        if(bt.isEnabled){
            main_log.writeMessage("App", "Searching for HC-06...")
            connectToDevice()
        } else{
            bt.showEnableDialog(this)
        }
    }


    private fun setWindowBness(delta: Float){
        val current = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        val n = current + (-delta.toInt() * 12)
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, if(n<0) 0 else if(n>255) 255 else n)
        val params = window.attributes
        params.screenBrightness += delta/64f
        window.attributes = params

    }

    private fun setMediaVolume(delta: Float){
        val am : AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if(delta < 0){
            var i = 0f
            while(i < abs(delta)){
                am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                i+=1f
                main_log.writeMessage("Volume", "${am.getStreamVolume(AudioManager.STREAM_MUSIC)}")
            }
        } else{
            var i = 0f
            while(i < abs(delta)){
                am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                i+=1f
                main_log.writeMessage("Volume", "${am.getStreamVolume(AudioManager.STREAM_MUSIC)}")
            }

        }
    }

    private fun connectToDevice(){
        bt.pairedDevices.forEach {
            if(it.name.contains("HC")){
                bt.connectToDevice(it)
            }
        }
        if(!bt.isConnected){
            main_log.writeMessage("Error", "Can't find paired device!")
        }
    }

    private fun getTrackedDim(name: String): GyroscopeData{
        return when(name){
            "X" -> GyroscopeData.X
            "Y" -> GyroscopeData.Y
            "Z" -> GyroscopeData.Z
            "Heading" -> GyroscopeData.HEADING
            "Pitch" -> GyroscopeData.PITCH
            "Roll" -> GyroscopeData.ROLL
            "Angle" ->  GyroscopeData.ANGLE
            else -> throw IllegalStateException("no such name")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bt.onActivityResult(requestCode, resultCode)
        if(requestCode == 1111 && resultCode != Activity.RESULT_CANCELED){
            connectToDevice()
        }
    }

    override fun onStop() {
        super.onStop()
        bt.onStop()
    }
}
