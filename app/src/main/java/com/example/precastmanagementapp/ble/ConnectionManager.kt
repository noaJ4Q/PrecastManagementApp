package com.example.precastmanagementapp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.widget.WithHint
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sign

@SuppressLint("MissingPermission")
object ConnectionManager {

    private var listeners: MutableSet<WeakReference<ConnectionEventListener>> = mutableSetOf()
    private val listenersAsSet
        get() = listeners.toSet()
    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private var pendingOperation: BleOperationType? = null

    fun servicesOnDevice(device: BluetoothDevice): List<BluetoothGattService>? =
        deviceGattMap[device]?.services

    fun registerListener(listener: ConnectionEventListener) {
        if (listeners.map { it.get() }.contains(listener)) { return }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
        Log.d("Listener", "Added listener $listener, ${listeners.size} listeners total")
    }

    fun unregisterListener(listener: ConnectionEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<ConnectionEventListener>? = null
        listenersAsSet.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
            Log.d("Listener", "Removed listener ${it.get()}, ${listeners.size} listeners total")
        }
    }

    fun connect(device: BluetoothDevice, context: Context) {
        if (device.isConnected()){
            Log.e("Connect", "Already connected to ${device.address}!")
        } else {
            enqueueOperation(Connect(device, context.applicationContext))
        }
    }

    fun teardownConnection(device: BluetoothDevice) {
        if (device.isConnected()) {
            enqueueOperation(Disconnect(device))
        } else {
            Log.e("TeardownConnection", "Not connected to ${device.address}, cannot teardown connection!")
        }
    }

    fun readCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() && characteristic.isReadable()) {
            enqueueOperation(CharacteristicRead(device, characteristic.uuid))
        } else if (!characteristic.isReadable()) {
            Log.e("ReadCharacteristic", "Attempting to read ${characteristic.uuid} that isn't readable!")
        } else if (!device.isConnected()) {
            Log.e("ReadCharacteristic", "Not connected to ${device.address}, cannot perform characteristic read")
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
    private fun signalEndOfOperation() {
        Log.d("OperationManagement", "End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()){
            doNextOperation()
        }
    }

    /**
     * Perform a given [BleOperationType]. All permission checks are performed before an operation
     * can be enqueued by [enqueueOperation].
     */
    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Log.e("OperationManagement", "doNextOperation() called when an operation is pending! Aborting.")
            return
        }

        val operation = operationQueue.poll() ?: run {
            Log.v("OperationManagement", "Operation queue empty, returning")
            return
        }

        pendingOperation = operation

        // Handle Connect separately from other operations that require device to be connected
        if (operation is Connect) {
            with(operation) {
                Log.w("Connect", "Connecting to ${device.address}")
                device.connectGatt(context, false, callback)
            }
            return
        }

        // Check BluetoothGatt availability for other operations
        val gatt = deviceGattMap[operation.device]
            ?: this@ConnectionManager.run {
                Log.e("Connect", "Not connected to ${operation.device.address}! Aborting $operation operation.")
                signalEndOfOperation()
                return
            }

        when (operation) {
            is Disconnect -> with(operation) {
                Log.w("Disconnect", "Disconnecting from ${device.address}")
                gatt.close()
                deviceGattMap.remove(device)
                listenersAsSet.forEach { it.get()?.onDisconnect?.invoke(device) }
                signalEndOfOperation()
            }
            is CharacteristicRead -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    gatt.readCharacteristic(characteristic)
                } ?: this@ConnectionManager.run {
                    Log.e("CharacteristicRead", "Cannot find $characteristicUuid to read from ")
                    signalEndOfOperation()
                }
            }
            else -> error("Unsupported operation: $operation")
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "onConnectionStateChanged: connected to $deviceAddress")
                    deviceGattMap[gatt.device] = gatt
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e("BluetoothGattCallback", "onConnectionStateChange: disconnected from $deviceAddress")
                    teardownConnection(gatt.device)
                }
            } else {
                Log.e("BluetoothGattCallback", "onConnectionStateChange: status $status encountered for $deviceAddress")
                if (pendingOperation is Connect) {
                    signalEndOfOperation()
                }
                teardownConnection(gatt.device)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                    printGattTable()
//                    requestMtu(device, GATT_MAX_MTU_SIZE)
                    listenersAsSet.forEach { it.get()?.onConnectionSetupComplete?.invoke(this)}
                } else {
                    Log.e("BluetoothGattCallback", "Service discovery failed due to status $status")
                    teardownConnection(gatt.device)
                }
            }

            if (pendingOperation is Connect) {
                signalEndOfOperation()
            }
        }

        // TODO: Complete onCharacteristicRead deprecated version

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            val uuid = characteristic.uuid
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i("BluetoothGattCallback", "Read characteristic $uuid | value: ${value.toHexString()}")
                    listenersAsSet.forEach {
                        it.get()?.onCharacteristicRead?.invoke(gatt.device, characteristic, value)
                    }
                }
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                }
                else -> {
                    Log.e("BluetoothGattCallback", "Characteristic read failed for $uuid, error: $status")
                }
            }

            if (pendingOperation is CharacteristicRead) {
                signalEndOfOperation()
            }
        }
    }

    private fun BluetoothDevice.isConnected() = deviceGattMap.contains(this)

    /**
     * A backwards compatible approach of obtaining a parcelable extra from an [Intent] object.
     *
     * NOTE: Despite the docs stating that [Intent.getParcelableExtra] is deprecated in Android 13,
     * Google has confirmed in https://issuetracker.google.com/issues/240585930#comment6 that the
     * replacement API is buggy for Android 13, and they suggested that developers continue to use the
     * deprecated API for Android 13. The issue will be fixed for Android 14 (U).
     */
    internal inline fun <reified T : Parcelable> Intent.parcelableExtraCompat(key: String): T? = when {
        Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}