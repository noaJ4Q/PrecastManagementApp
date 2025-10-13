package com.example.precastmanagementapp.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import java.util.UUID

fun BluetoothGatt.printGattTable() {
    if (services.isEmpty()) {
        Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
        return
    }
    services.forEach { service ->
        val characteristicsTable = service.characteristics.joinToString(
            separator = "n|--",
            prefix = "|--"
        ) { it.uuid.toString() }
        Log.i("printGattTable", "nService ${service.uuid}nCharacteristics:n$characteristicsTable")
    }
}

fun BluetoothGatt.findCharacteristic(
    characteristicUuid: UUID,
    serviceUuid: UUID? = null
): BluetoothGattCharacteristic? {
    return if (serviceUuid != null) {
        // If serviceUuid is available, use it to disambiguate cases where multiple services have
        // distinct characteristics that happen to use the same UUID
        services
            ?.firstOrNull { it.uuid == serviceUuid }
            ?.characteristics?.firstOrNull { it.uuid == characteristicUuid }
    } else {
        // Iterate through services and find the first one with a match for the characteristic UUID
        services?.forEach { service ->
            service.characteristics?.firstOrNull { characteristic ->
                characteristic.uuid == characteristicUuid
            }?.let { matchingCharacteristic ->
                return matchingCharacteristic
            }
        }
        return null
    }
}

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
    return properties and property != 0
}

fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }
