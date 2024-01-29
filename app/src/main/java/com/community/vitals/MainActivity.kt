package com.community.vitals

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.community.vitals.databinding.ActivityMainBinding
import com.community.vitals.singleton.SingletonInstance

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedPrefsKey = "connectedDevice"

    private val updateUIReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val receivedData = intent?.getStringExtra("receivedData")
            updateUI(receivedData)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        Log.d("MainActivity", "onCreate")

        val filter = IntentFilter("com.community.vitals.UPDATE_UI")
        registerReceiver(updateUIReceiver, filter)

        binding.txtDiscover.setOnClickListener {
            val intent = Intent(this, DiscoverActivity::class.java)
            startActivity(intent)
        }

        binding.txtReading.setOnClickListener {
            Log.d("MainActivity", "Connected device: ${getConnectedDeviceId()}")
                val instructionsSent = sendInstructions("m")
                if (instructionsSent) {
                    runOnUiThread {
                        registerReceiver(updateUIReceiver, filter)
                    }
                }
        }
    }

    private fun getConnectedDeviceId(): String? {
        val sharedPreferences = getSharedPreferences("YourPrefsName", Context.MODE_PRIVATE)
        return sharedPreferences.getString(sharedPrefsKey, null)
    }

    private fun sendInstructions(instructions: String): Boolean {
        return try {
            SingletonInstance.connectedThreadInstance?.write(instructions.toByteArray())
            true
        } catch (e: Exception) {
            Log.e("MainActivity", "Error sending instructions", e)
            false
        }
    }

    private fun updateUI(receivedData: String?) {
        Log.e("DataR", "$receivedData")
        if (receivedData != null) {
            val tempArray = receivedData.split("_")
            if (tempArray.size >= 2) {
                val updatedTemperature = "Temperature\n ${tempArray[1]} F - ${tempArray[2]} C"
                Handler(mainLooper).post {
                    binding.txtReading.text = updatedTemperature
                    Log.e("Temp", "$updatedTemperature")
                }
            } else {
                Log.e("Data", "Invalid data format: $receivedData")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateUIReceiver)
    }
}