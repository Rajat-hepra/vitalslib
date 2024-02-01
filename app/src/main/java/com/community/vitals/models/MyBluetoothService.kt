package com.community.vitals.models

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MyBluetoothService : Service() {

    private var connectedThread: ConnectedThread? = null
    private val handler = Handler(Looper.getMainLooper())
    private val MESSAGE_READ: Int = 0
    private val MESSAGE_TOAST: Int = 2
    private val binder = LocalBinder()
    private var receivedData: String? = null

    inner class LocalBinder : Binder() {
        fun getService(): MyBluetoothService = this@MyBluetoothService
    }

    inner class ConnectedThread(private val mmSocket: BluetoothSocket, val connectedDevice: BluetoothDevice) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)
        private val uiHandler = Handler(Looper.getMainLooper())

        override fun run() {
            var numBytes: Int

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer)
                    receivedData = String(mmBuffer, 0, numBytes)
                    val readMsg = handler.obtainMessage(
                        MESSAGE_READ, numBytes, -1,
                        mmBuffer.copyOf(numBytes)
                    )
                    readMsg.sendToTarget()
                    Log.d("Received data", "$receivedData")
                    uiHandler.post {
                        handleReceivedData(receivedData)
                    }
                } catch (e: IOException) {
                    Log.d("", "Input stream was disconnected", e)
                    break
                }
            }
            connectedThread = this
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("", "Error occurred when sending data", e)

                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
            }
        }

        fun cancel() {
            try {
                //mmSocket.close()
            } catch (e: IOException) {
                Log.e("", "Could not close the connect socket", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun handleReceivedData(data: String?) {
        Log.d("MyBluetoothService", "Received data: $data")
        receivedData = data
        val intent = Intent("com.community.vitals.UPDATE_UI")
        intent.putExtra("receivedData", receivedData)
        sendBroadcast(intent)
    }

    fun getConnectedThread(): ConnectedThread? {
        return connectedThread
    }

    fun disconnect() {
        connectedThread?.cancel()
        stopSelf()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

}
