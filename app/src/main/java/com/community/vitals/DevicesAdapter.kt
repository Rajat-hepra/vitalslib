package com.community.vitals

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.community.vitals.models.BluetoothDeviceModel

class DevicesAdapter(private val deviceList: ArrayList<BluetoothDeviceModel>, val listener: OnDeviceClickListener) :
    RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    interface OnDeviceClickListener{
        fun onDeviceClicked(position: Int)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.txtDeviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.txtDeviceAddress)
        val deviceImage: ImageView = itemView.findViewById(R.id.imgDevice)
        val deviceConnect: ImageView = itemView.findViewById(R.id.imgConnect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.child_recy_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.deviceName.text = device.name
        holder.deviceAddress.text = device.address
        holder.itemView.setOnClickListener{
            holder.deviceName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.deviceAddress.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.deviceConnect.setColorFilter(R.color.black)
            holder.deviceImage.setColorFilter(R.color.black)
            listener.onDeviceClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }
}
