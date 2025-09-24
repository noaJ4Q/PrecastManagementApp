package com.example.precastmanagementapp.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import java.util.UUID

/**
 * Utilidades para operaciones Bluetooth y BLE
 */
object BluetoothUtils {

    /**
     * Obtiene el adaptador Bluetooth del sistema
     */
    fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    /**
     * Verifica si BLE está soportado en el dispositivo
     */
    fun isBleSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * Obtiene dispositivo Bluetooth por dirección MAC
     */
    fun getDeviceByAddress(context: Context, address: String): BluetoothDevice? {
        val adapter = getBluetoothAdapter(context)
        return try {
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                adapter?.getRemoteDevice(address)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Valida una dirección MAC Bluetooth
     */
    fun isValidMacAddress(address: String?): Boolean {
        return address?.let { BluetoothAdapter.checkBluetoothAddress(it) } == true
    }

    /**
     * Formatea una dirección MAC con separadores
     */
    fun formatMacAddress(address: String): String {
        return address.replace(":", "").chunked(2).joinToString(":")
    }

    /**
     * Obtiene el nombre del dispositivo o un nombre por defecto
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getDeviceName(device: BluetoothDevice?): String {
        return device?.name ?: "Dispositivo desconocido"
    }

    /**
     * Obtiene información detallada del dispositivo
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getDeviceInfo(device: BluetoothDevice): DeviceInfo {
        return DeviceInfo(
            name = device.name ?: "Desconocido",
            address = device.address,
            bondState = getBondStateString(device.bondState),
            deviceClass = device.bluetoothClass?.majorDeviceClass?.toString() ?: "N/A",
            type = getDeviceTypeString(device.type)
        )
    }

    /**
     * Convierte el estado de emparejamiento a string legible
     */
    private fun getBondStateString(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_NONE -> "No emparejado"
            BluetoothDevice.BOND_BONDING -> "Emparejando..."
            BluetoothDevice.BOND_BONDED -> "Emparejado"
            else -> "Estado desconocido"
        }
    }

    /**
     * Convierte el tipo de dispositivo a string legible
     */
    private fun getDeviceTypeString(type: Int): String {
        return when (type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Clásico"
            BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual (Clásico + BLE)"
            else -> "Desconocido"
        }
    }

    /**
     * Convierte RSSI a nivel de señal
     */
    fun rssiToSignalLevel(rssi: Int): SignalLevel {
        return when {
            rssi >= -50 -> SignalLevel.EXCELLENT
            rssi >= -70 -> SignalLevel.GOOD
            rssi >= -80 -> SignalLevel.FAIR
            rssi >= -90 -> SignalLevel.WEAK
            else -> SignalLevel.VERY_WEAK
        }
    }

    /**
     * Convierte UUID string a objeto UUID
     */
    fun parseUuid(uuidString: String): UUID? {
        return try {
            UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Convierte bytes a string hexadecimal
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }.uppercase()
    }

    /**
     * Convierte string hexadecimal a bytes
     */
    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "").replace(":", "")
        return cleanHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    /**
     * Obtiene el estado de conexión GATT como string
     */
    fun getGattStateString(state: Int): String {
        return when (state) {
            BluetoothGatt.STATE_DISCONNECTED -> "Desconectado"
            BluetoothGatt.STATE_CONNECTING -> "Conectando"
            BluetoothGatt.STATE_CONNECTED -> "Conectado"
            BluetoothGatt.STATE_DISCONNECTING -> "Desconectando"
            else -> "Estado desconocido ($state)"
        }
    }

    /**
     * Información del dispositivo Bluetooth
     */
    data class DeviceInfo(
        val name: String,
        val address: String,
        val bondState: String,
        val deviceClass: String,
        val type: String
    )

    /**
     * Nivel de señal basado en RSSI
     */
    enum class SignalLevel(val displayName: String, val bars: Int) {
        EXCELLENT("Excelente", 4),
        GOOD("Buena", 3),
        FAIR("Regular", 2),
        WEAK("Débil", 1),
        VERY_WEAK("Muy débil", 0)
    }

    /**
     * Verifica si el dispositivo Android soporta las características BLE necesarias
     */
    fun checkBleCapabilities(context: Context): BleCapabilities {
        val adapter = getBluetoothAdapter(context)
        return BleCapabilities(
            isSupported = isBleSupported(context),
            isAvailable = adapter != null,
            isEnabled = adapter?.isEnabled == true,
            supportsPeripheral = adapter?.isMultipleAdvertisementSupported == true,
            maxConnections = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                adapter?.let { 7 } ?: 0  // Típicamente 7 conexiones simultáneas
            } else 4
        )
    }

    /**
     * Capacidades BLE del dispositivo
     */
    data class BleCapabilities(
        val isSupported: Boolean,
        val isAvailable: Boolean,
        val isEnabled: Boolean,
        val supportsPeripheral: Boolean,
        val maxConnections: Int
    ) {
        fun canUseBle(): Boolean = isSupported && isAvailable && isEnabled

        fun getStatusSummary(): String {
            return when {
                !isSupported -> "BLE no soportado"
                !isAvailable -> "Adaptador no disponible"
                !isEnabled -> "Bluetooth deshabilitado"
                else -> "BLE listo (máx. $maxConnections conexiones)"
            }
        }
    }
}