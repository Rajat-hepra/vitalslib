package com.community.vitals.singleton

import com.community.vitals.models.MyBluetoothService

object SingletonInstance{
    var connectedThreadInstance: MyBluetoothService.ConnectedThread? = null
}