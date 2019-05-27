package com.example.arduinobluetoothapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val bt: BluetoothSPP by lazy{
        BluetoothSPP(this).apply {
            setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener{
                override fun onDeviceDisconnected() {
                    main_log.append("Device disconnected!\n")
                    disableInput()
                }

                override fun onDeviceConnected(name: String?, address: String?) {
                    main_log.append("Device $name connected ($address)!\n")
                    main_device_selection_text.text = name
                }

                override fun onDeviceConnectionFailed() {
                    main_log.append("Device connection failed!\n")
                    disableInput()
                }
            })
            setOnDataReceivedListener{_, message ->
                main_log.append("$message \n")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        disableInput()
        main_log.append("Welcome to ArduinoBluetoothApp!\n")
        main_device_selection_container.setOnClickListener{
            searchDevices()
        }
        if(bt.isBluetoothAvailable) {
            if(!bt.isBluetoothEnabled) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_ENABLE)
            } else {
                searchDevices()
            }
        } else{
            main_log.append("Bluetooth unavailable!\n")
        }
    }

    private fun disableInput(){
        main_button.isEnabled = false
        main_input.isEnabled = false
    }

    private fun setupBluetooth(){
        main_button.isEnabled = true
        main_input.isEnabled = true
        main_button.setOnClickListener{
            bt.send(main_input.text.toString(), true)
            main_input.setText("")
        }

    }

    private fun searchDevices(){
        startActivityForResult(Intent(applicationContext, DeviceList::class.java), REQUEST_BLUETOOTH_DEVICELIST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_BLUETOOTH_ENABLE ->{
                if(resultCode == Activity.RESULT_OK){
                    bt.setupService()
                    bt.startService(BluetoothState.DEVICE_ANDROID)
                    searchDevices()
                }
            }
            REQUEST_BLUETOOTH_DEVICELIST ->{
                if(resultCode == Activity.RESULT_OK){
                    bt.connect(data)
                    setupBluetooth()
                }
            }
        }
    }
}
