package com.example.arduinobluetoothapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.example.arduinobluetoothapp.gyroscope.GyroscopeManager
import kotlinx.android.synthetic.main.activity_main.*
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.interfaces.DeviceCallback
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    private val manager: GyroscopeManager by lazy {
        GyroscopeManager()
    }

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
                        val dim = getTrackedDim(findViewById<RadioButton>(main_radiogroup.checkedRadioButtonId).text.toString())
                        val delta = manager.getTrackedDelta(gyroData, dim)
                        if(delta!=null){
                            main_log.writeMessage(dim.name, "$delta \t" + if (delta > 0f) "RIGHT" else "LEFT")
                            main_scroll.fullScroll(View.FOCUS_DOWN)
                            if(main_toogle.isChecked){
                                setWindowBness(delta)
                            }
                        }
////                        main_log.writeMessage(gyroData.toString())
//                        val res = manager.getBiggestDeltaData(gyroData)
//                        if(res?.first != null){
//                            main_log.writeMessage("${res.first.name} - (${res.second}) - " + if (res.second > 5f) "RIGHT" else "LEFT")
//                            main_scroll.fullScroll(View.FOCUS_DOWN)
//                    }
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

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                val t =(if(seekBar!!.progress < 50) 50f else seekBar.progress.toFloat())/100f
//                manager.threshold = t
//                main_sense_text.text = "$t"
            }
        })
    }

    override fun onStart() {
        super.onStart()
        bt.onStart()
        if(bt.isEnabled){
            main_log.writeMessage("App", "Searching for HC-06...")
            bt.pairedDevices.forEach {
                if(it.name.contains("HC")){
                    bt.connectToDevice(it)
                }
            }
            if(bt.isConnected){
                main_log.writeMessage("Error", "Can't find paired device!")
            }
        } else{
            bt.showEnableDialog(this)
        }
    }


    private fun setWindowBness(delta: Float){
//        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
//                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
//                startActivity(intent)
//        }
        val current = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        val n = current + delta.toInt()
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, if(n<0) 0 else if(n>255) 255 else n)
        //val params = WindowManager.LayoutParams()
        val params = window.attributes
        params.screenBrightness += delta/64f
        window.attributes = params

    }

    private fun getTrackedDim(name: String): GyroscopeData{
        return when(name){
            "X" -> GyroscopeData.X
            "Y" -> GyroscopeData.Y
            "Z" -> GyroscopeData.Z
            "Heading" -> GyroscopeData.HEADING
            "Pitch" -> GyroscopeData.PITCH
            "Roll" -> GyroscopeData.ROLL
            else -> throw IllegalStateException("no such name")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bt.onActivityResult(requestCode, resultCode)
    }

    override fun onStop() {
        super.onStop()
        bt.onStop()
    }

//    val bt: BluetoothSPP by lazy{
//        BluetoothSPP(this).apply {
//            setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener{
//                override fun onDeviceDisconnected() {
//                    main_log.writeMessage("Device", "Disconnected!")
//                    disableInput()
//                }
//
//                override fun onDeviceConnected(name: String?, address: String?) {
//                    main_log.writeMessage("Device", "$name connected ($address)!")
//                    main_device_selection_text.text = "$name ($address)"
//                }
//
//                override fun onDeviceConnectionFailed() {
//                    main_log.writeMessage("Device", "Connection failed!")
//                    disableInput()
//                }
//            })
//            setOnDataReceivedListener{bytes, message ->
//                main_log.writeMessage("Received Bytes", String(bytes))
//                main_log.writeMessage("Received String", message)
//            }
//        }
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        disableInput()
//        main_log.writeMessage("Welcome to ArduinoBluetoothApp!")
//        main_device_selection_container.setOnClickListener{
//            searchDevices()
//        }
//        if(bt.isBluetoothAvailable) {
//            if(!bt.isBluetoothEnabled) {
//                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_ENABLE)
//            } else {
//                bt.setupService()
//                bt.startService(BluetoothState.DEVICE_OTHER)
//                searchDevices()
//            }
//        } else{
//            main_log.writeMessage("Error","Bluetooth unavailable!")
//        }
//    }
//
//    private fun disableInput(){
//        main_button.isEnabled = false
//        main_input.isEnabled = false
//    }
////
//    private fun setupBluetooth(){
//        main_button.isEnabled = true
//        main_input.isEnabled = true
////        main_button.setOnClickListener{
////            bt.send(main_input.text.toString(), true)
////            main_input.setText("")
////        }
//
//    }
//
//    private fun searchDevices(){
//        startActivityForResult(Intent(applicationContext, DeviceList::class.java), REQUEST_BLUETOOTH_DEVICELIST)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        //super.onActivityResult(requestCode, resultCode, data)
//        when(requestCode){
//            REQUEST_BLUETOOTH_ENABLE ->{
//                if(resultCode == Activity.RESULT_OK){
//                    bt.setupService()
//                    bt.startService(BluetoothState.DEVICE_OTHER)
//                    searchDevices()
//                }
//            }
//            REQUEST_BLUETOOTH_DEVICELIST ->{
//                if(resultCode == Activity.RESULT_OK){
//                    Log.d("Connect", data?.extras?.getString(BluetoothState.EXTRA_DEVICE_ADDRESS))
//                    bt.connect(data)
//                    bt.startService(BluetoothState.DEVICE_OTHER)
//                    setupBluetooth()
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        bt.stopService()
//        super.onDestroy()
//    }
}
