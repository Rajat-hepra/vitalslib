package com.community.vitals

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.community.vitals.databinding.ActivityDiscoverBinding
import com.community.vitals.models.BluetoothDeviceModel
import com.community.vitals.models.MyBluetoothService
import com.community.vitals.singleton.SingletonInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.Serializable
import java.util.UUID

class DiscoverActivity : AppCompatActivity(), DevicesAdapter.OnDeviceClickListener, Serializable {

    private lateinit var binding: ActivityDiscoverBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var discoveredDevicesAdapter: DevicesAdapter
    private val list = ArrayList<BluetoothDeviceModel>()
    private val sharedPrefsKey = "connectedDevice"
    private var myBluetoothService = MyBluetoothService()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_discover)
        Log.d("DiscoverActivity", "onCreate")

        sharedPreferences = getSharedPreferences("YourPrefsName", Context.MODE_PRIVATE)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        discoveredDevicesAdapter = DevicesAdapter(list, this)
        binding.recyDevices.adapter = discoveredDevicesAdapter

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!bluetoothAdapter.isEnabled) {
                Log.d("DiscoverActivity", "Bluetooth not enabled. Requesting to enable.")
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, 1)
                return
            }
            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val address = device.address
                    list.add(BluetoothDeviceModel(deviceName, address, device))
                }
                discoveredDevicesAdapter.notifyDataSetChanged()
            }
        }

        val serviceIntent = Intent(this, MyBluetoothService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onDeviceClicked(position: Int) {
        val device = list[position]
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val serviceUUID = device.device.uuids[0].uuid
            connectToDevice(device, serviceUUID.toString())
            saveConnectedDeviceId(device.device.address)
        }
    }

    private fun connectToDevice(device: BluetoothDeviceModel, serviceUUID: String) {
        GlobalScope.launch(Dispatchers.IO) {
            if (ActivityCompat.checkSelfPermission(
                    this@DiscoverActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val socket: BluetoothSocket =
                        device.device.createRfcommSocketToServiceRecord(UUID.fromString(serviceUUID))
                    socket.connect()
                    runOnUiThread {
                        SingletonInstance.connectedThreadInstance =
                            myBluetoothService.ConnectedThread(socket, device.device)
                        SingletonInstance.connectedThreadInstance?.start()
                        Toast.makeText(
                            this@DiscoverActivity,
                            "Connected to ${device.device.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        Log.e("", "$e.message")
                        Toast.makeText(
                            this@DiscoverActivity,
                            "Connection failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun saveConnectedDeviceId(deviceId: String) {
        val editor = sharedPreferences.edit()
        editor.putString(sharedPrefsKey, deviceId)
        editor.apply()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyBluetoothService.LocalBinder
            myBluetoothService = binder.getService()
            if (SingletonInstance.connectedThreadInstance == null) {
                SingletonInstance.connectedThreadInstance = myBluetoothService.getConnectedThread()
            }
            Log.d("DiscoverActivity", "onServiceConnected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            SingletonInstance.connectedThreadInstance = null
            Log.d("DiscoverActivity", "onServiceDisconnected")
        }
    }

    override fun onBackPressed() {
        SingletonInstance.connectedThreadInstance?.let {
            saveConnectedDeviceId(it.connectedDevice.address)
        }
        super.onBackPressed()
    }

}
