package com.example.precastmanagementapp.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

/** A listener containing callback methods to be registered with [ConnectionManager].*/
class ConnectionEventListener {

    var onConnectionSetupComplete: ((gatt: BluetoothGatt) -> Unit)? = null

    var onDisconnect: ((device: BluetoothDevice) -> Unit)? = null

    var onCharacteristicRead: (
        (
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) -> Unit
    )? = null

    var onCharacteristicChanged: (
        (
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) -> Unit
    )? = null
}