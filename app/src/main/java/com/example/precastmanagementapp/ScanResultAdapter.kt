package com.example.precastmanagementapp

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScanResultAdapter(
    private val items: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) :
    RecyclerView.Adapter<ScanResultAdapter.ViewHolder>(){

    // Create new view
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_result, parent, false)
        return ViewHolder(view, onClickListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val view: View,
        private val onclickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(view) {

        fun bind(result: ScanResult) {
            view.findViewById<TextView>(R.id.tvDeviceName).text = result.device.name ?: "Unnamed"
            view.findViewById<TextView>(R.id.tvDeviceMacAddress).text = result.device.address
            view.findViewById<TextView>(R.id.tvDeviceSignal).text = "${result.rssi} dBm"
            view.setOnClickListener { onclickListener.invoke(result) }
        }


    }
}