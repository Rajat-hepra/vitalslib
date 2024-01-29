package com.community.vitals.models

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceModel(
    val name: String? = null,
    val address: String? = null,
    val device: BluetoothDevice
)