package com.example.precastmanagementapp.models

import android.bluetooth.BluetoothDevice
import java.util.Date

/**
 * Modelo para el estado de conexión BLE con el ESP32
 */
data class ConnectionStatus(
    val state: BleConnectionState,
    val device: BluetoothDevice? = null,
    val errorMessage: String? = null,
    val lastConnected: Long? = null,
    val connectionAttempts: Int = 0,
    val signalStrength: Int? = null,    // RSSI en dBm
    val batteryLevel: Int? = null       // Nivel de batería del ESP32 (si está disponible)
) {

    /**
     * Verifica si la conexión está activa
     */
    fun isConnected(): Boolean = state == BleConnectionState.CONNECTED

    /**
     * Verifica si está en proceso de conexión
     */
    fun isConnecting(): Boolean = state == BleConnectionState.CONNECTING

    /**
     * Verifica si la conexión falló
     */
    fun hasError(): Boolean = state == BleConnectionState.ERROR && errorMessage != null

    /**
     * Obtiene la fecha de última conexión
     */
    fun getLastConnectedDate(): Date? {
        return lastConnected?.let { Date(it) }
    }

    /**
     * Obtiene la información básica del dispositivo
     */
    fun getDeviceInfo(): String? {
        return device?.let { "${it.name ?: "Desconocido"} (${it.address})" }
    }

    /**
     * Obtiene el nivel de señal formateado
     */
    fun getFormattedSignalStrength(): String {
        return signalStrength?.let { "${it} dBm" } ?: "N/A"
    }

    /**
     * Determina la calidad de la señal
     */
    fun getSignalQuality(): SignalQuality {
        return when (signalStrength) {
            null -> SignalQuality.UNKNOWN
            in -50..0 -> SignalQuality.EXCELLENT
            in -70..-51 -> SignalQuality.GOOD
            in -80..-71 -> SignalQuality.FAIR
            in -90..-81 -> SignalQuality.WEAK
            else -> SignalQuality.VERY_WEAK
        }
    }

    /**
     * Obtiene el tiempo desde la última conexión en texto legible
     */
    fun getTimeSinceLastConnection(): String {
        if (lastConnected == null) return "Nunca conectado"

        val diff = System.currentTimeMillis() - lastConnected
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

}

/**
 * Estados posibles de conexión BLE
 */
enum class BleConnectionState(val displayName: String) {
    DISCONNECTED("Desconectado"),
    CONNECTING("Conectando..."),
    CONNECTED("Conectado"),
    DISCONNECTING("Desconectando..."),
    ERROR("Error de conexión"),
    BLUETOOTH_OFF("Bluetooth deshabilitado"),
    DEVICE_NOT_FOUND("Dispositivo no encontrado"),
    PERMISSION_DENIED("Permisos denegados"),
    SCANNING("Buscando dispositivo..."),
    READY("Listo para conectar");

    /**
     * Verifica si el estado indica que se puede intentar conectar
     */
    fun canConnect(): Boolean {
        return this in listOf(DISCONNECTED, ERROR, DEVICE_NOT_FOUND, READY)
    }

    /**
     * Verifica si el estado requiere intervención del usuario
     */
    fun requiresUserAction(): Boolean {
        return this in listOf(BLUETOOTH_OFF, PERMISSION_DENIED, ERROR)
    }
}

/**
 * Calidad de señal BLE
 */
enum class SignalQuality(val displayName: String, val colorResource: String) {
    EXCELLENT("Excelente", "#4CAF50"),      // Verde
    GOOD("Buena", "#8BC34A"),               // Verde claro
    FAIR("Regular", "#FFC107"),             // Amarillo
    WEAK("Débil", "#FF9800"),               // Naranja
    VERY_WEAK("Muy débil", "#F44336"),      // Rojo
    UNKNOWN("Desconocida", "#9E9E9E")       // Gris
}