package com.community.vitals

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Vitals {
    fun sendReceiveData(type: String?, socket: BluetoothSocket?): String {
        Log.e("VitalSocket", "$socket")
        var result = ""
        var tempf = ""
        var tempc = ""
        var spo2 = ""
        var pulse = ""
        var bloodPressureValue = ""
        var bloodGlucose = ""
        val outputStream: OutputStream = socket!!.outputStream
        val inputStream: InputStream = socket!!.inputStream
        val buffer = ByteArray(1024)
        val bytesRead: Int = inputStream.read(buffer)
        val reading = String(buffer, 0, bytesRead)
        val list1 = reading.split("_")
        outputStream.write(type!!.toByteArray())

        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (type != null && type.isNotEmpty()) {
                    when (type) {
                        "m" -> {
                            if (reading.contains("_")) {
                                if (list1[0] == "T") {
                                    tempf = list1[1]
                                    tempc = list1[2]
                                    result = "$tempf-$tempc"
                                    Log.d("Vital", "$tempf-------------$tempc ")
                                    // socket!!.close()
                                }
                            }
                        }

                        "p" -> {
                            if (reading.contains("_")) {
                                if (list1[0] == "O") {
                                    spo2 = list1[1]
                                    pulse = list1[2]
                                    result = "$spo2-$pulse"
                                    Log.d("Vital", "$spo2-------------$pulse ")
                                    //socket!!.close()
                                }
                            }
                        }

                        "b" -> {
                            if (reading.contains("_")) {
                                if (list1[0] == "B") {
                                    bloodPressureValue = "${list1[1]}/${list1[2]}"
                                    result = "$bloodPressureValue"
                                    Log.d("Vital", "$bloodPressureValue==========>>")
                                    //socket!!.close()
                                }
                            }
                        }

                        "g" -> {
                            if (reading.contains("_")) {
                                if (list1[0] == "G") {
                                    bloodGlucose = "${list1[1]}"
                                    result = "$bloodGlucose"
                                    Log.d("Vital", "$bloodGlucose==========>>")
                                    //socket!!.close()
                                }
                            }
                        }
                    }
                } else if (type == "" || type == null || type.isEmpty()) {
                    if (reading.startsWith("T") && list1.size >= 3) {
                        tempf = list1[1]
                        tempc = list1[2]
                        result = "$tempf-$tempc"
                        Log.d("Vital", "$tempf-------------$tempc")
                        //socket!!.close()
                    } else if (reading.startsWith("B") && list1.size >= 3) {
                        bloodPressureValue = "${list1[1]}/${list1[2]}"
                        result = "$bloodPressureValue"
                        Log.d("Vital", "$bloodPressureValue==========>>")
                        // socket!!.close()
                    } else if (reading.startsWith("O") && list1.size >= 3) {
                        spo2 = list1[1]
                        pulse = list1[2]
                        result = "$spo2-$pulse"
                        Log.d("Vital", "$spo2-------------$pulse ")
                        //socket!!.close()
                    } else if (reading.startsWith("G") && list1.size >= 2) {
                        bloodGlucose = "${list1[1]}"
                        result = "$bloodGlucose"
                        Log.d("Vital", "$bloodGlucose==========>>")
                        //socket!!.close()
                    }
                }
                //socket!!.close()
            } catch (e: IOException) {
                Log.e("Vitals", "Error in data transmission", e)
            }
        }
        return result
    }
}