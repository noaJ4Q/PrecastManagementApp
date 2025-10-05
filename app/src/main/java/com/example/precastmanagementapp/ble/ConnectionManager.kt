package com.example.precastmanagementapp.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object ConnectionManager {

    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private val pendingOperation: BleOperationType? = null

    fun connect(device: BluetoothDevice, context: Context) {
        if (device.isConnected()){
            Log.d("ConnectionManager", "Already connected to ${device.address}")
        } else {
            enqueueOperation(Connect(device, context.applicationContext))
        }
    }

    @Synchronized
    private fun enqueueOperation(operation: BleOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Log.d("ConnectionManager", "doNextOperation() called when an operation id pending! Aborting.")
            return
        }
        val operation = operationQueue.poll() ?: run {
            Log.d("ConnectionManager", "Operation queue empty, returning")
            return
        }
    }

    private fun BluetoothDevice.isConnected() = deviceGattMap.contains(this)

}