package com.example.precastmanagementapp.ble

import android.bluetooth.BluetoothDevice
import android.content.Context

/** Abstract sealed class representing a type of BLE operation. */
sealed class BleOperationType {
    abstract val device: BluetoothDevice
}

/** Connect to [device] and perform service discovery. */
data class Connect(override val device: BluetoothDevice, val context: Context) : BleOperationType()