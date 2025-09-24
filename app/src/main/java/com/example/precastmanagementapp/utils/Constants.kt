package com.example.precastmanagementapp.utils

import java.util.UUID

/**
 * Constantes de la aplicación para configuración BLE y RFID
 */
object Constants {

    // ==================== CONFIGURACIÓN ESP32 ====================

    /**
     * Información conocida del ESP32 RFID Scanner
     */
    const val ESP32_MAC_ADDRESS = "24:6F:28:XX:XX:XX"  // Reemplazar con MAC real
    const val ESP32_DEVICE_NAME = "ESP32-RFID-Scanner"
    const val ESP32_FALLBACK_NAME = "ESP32"

    // ==================== UUIDs BLE ====================

    /**
     * UUID del servicio principal del ESP32
     * Generar uno único para tu proyecto en: https://www.uuidgenerator.net/
     */
    const val SERVICE_UUID_STRING = "12345678-1234-5678-9012-123456789abc"
    val SERVICE_UUID: UUID = UUID.fromString(SERVICE_UUID_STRING)

    /**
     * UUID para característica de datos RFID (ESP32 -> App)
     */
    const val RFID_DATA_CHARACTERISTIC_UUID_STRING = "87654321-4321-8765-4321-cba987654321"
    val RFID_DATA_CHARACTERISTIC_UUID: UUID = UUID.fromString(RFID_DATA_CHARACTERISTIC_UUID_STRING)

    /**
     * UUID para característica de comandos (App -> ESP32)
     */
    const val COMMAND_CHARACTERISTIC_UUID_STRING = "11111111-2222-3333-4444-555555555555"
    val COMMAND_CHARACTERISTIC_UUID: UUID = UUID.fromString(COMMAND_CHARACTERISTIC_UUID_STRING)

    /**
     * UUID para característica de estado/configuración
     */
    const val STATUS_CHARACTERISTIC_UUID_STRING = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
    val STATUS_CHARACTERISTIC_UUID: UUID = UUID.fromString(STATUS_CHARACTERISTIC_UUID_STRING)

    // ==================== CONFIGURACIÓN BLE ====================

    /**
     * Timeout para operaciones BLE en milisegundos
     */
    const val BLE_CONNECTION_TIMEOUT = 10000L          // 10 segundos
    const val BLE_OPERATION_TIMEOUT = 5000L            // 5 segundos
    const val BLE_SCAN_TIMEOUT = 8000L                 // 8 segundos
    const val BLE_RETRY_DELAY = 2000L                  // 2 segundos entre reintentos

    /**
     * Configuración de reconexión automática
     */
    const val MAX_CONNECTION_RETRIES = 3
    const val AUTO_RECONNECT_ENABLED = true
    const val RECONNECT_INTERVAL = 5000L               // 5 segundos

    /**
     * MTU (Maximum Transmission Unit) para BLE
     */
    const val BLE_MTU_SIZE = 512                       // Tamaño preferido
    const val BLE_MIN_MTU_SIZE = 23                    // Tamaño mínimo estándar

    // ==================== COMANDOS ESP32 ====================

    /**
     * Comandos que se pueden enviar al ESP32
     */
    object Commands {
        const val START_SCAN = "START_SCAN"
        const val STOP_SCAN = "STOP_SCAN"
        const val GET_STATUS = "GET_STATUS"
        const val RESET_DEVICE = "RESET"
        const val GET_BATTERY = "GET_BATTERY"
        const val SET_LED_ON = "LED_ON"
        const val SET_LED_OFF = "LED_OFF"
        const val BEEP_SUCCESS = "BEEP_OK"
        const val BEEP_ERROR = "BEEP_ERR"
    }

    /**
     * Respuestas esperadas del ESP32
     */
    object Responses {
        const val ACK = "ACK"
        const val NACK = "NACK"
        const val ERROR = "ERROR"
        const val READY = "READY"
        const val SCANNING = "SCANNING"
        const val IDLE = "IDLE"
    }

    // ==================== CONFIGURACIÓN RFID ====================

    /**
     * Configuración para lectura RFID
     */
    const val RFID_READ_TIMEOUT = 3000L                // 3 segundos timeout de lectura
    const val MIN_UID_LENGTH = 8                       // Mínimo 4 bytes UID
    const val MAX_UID_LENGTH = 20                      // Máximo 10 bytes UID

    /**
     * Separadores para formateo de datos
     */
    const val UID_SEPARATOR = ":"
    const val DATA_SEPARATOR = "|"
    const val BLOCK_SEPARATOR = ";"

    // ==================== CONFIGURACIÓN UI ====================

    /**
     * Timeouts para UI y notificaciones
     */
    const val TOAST_DURATION_SHORT = 2000L
    const val TOAST_DURATION_LONG = 4000L
    const val PROGRESS_UPDATE_INTERVAL = 100L
    const val CONNECTION_STATUS_UPDATE_INTERVAL = 1000L

    /**
     * Colores para estados de conexión (formato hex)
     */
    object StatusColors {
        const val CONNECTED = "#4CAF50"        // Verde
        const val CONNECTING = "#2196F3"       // Azul
        const val DISCONNECTED = "#9E9E9E"     // Gris
        const val ERROR = "#F44336"            // Rojo
        const val WARNING = "#FF9800"          // Naranja
    }

    // ==================== CONFIGURACIÓN DE LOG ====================

    /**
     * Tags para logging
     */
    object LogTags {
        const val BLE_MANAGER = "BLE_Manager"
        const val PAIR_ACTIVITY = "PairActivity"
        const val RFID_DATA = "RfidData"
        const val PERMISSIONS = "Permissions"
        const val ESP32_COMM = "ESP32_Communication"
    }

    /**
     * Configuración de debug
     */
    const val DEBUG_MODE = true
    const val VERBOSE_LOGGING = true

    // ==================== SHARED PREFERENCES ====================

    /**
     * Keys para SharedPreferences
     */
    object PrefsKeys {
        const val PREFS_NAME = "ble_rfid_prefs"
        const val LAST_CONNECTED_DEVICE = "last_connected_device"
        const val AUTO_CONNECT_ENABLED = "auto_connect_enabled"
        const val CONNECTION_RETRIES = "connection_retries"
        const val LAST_CONNECTION_TIME = "last_connection_time"
        const val DEVICE_BATTERY_LEVEL = "device_battery_level"
        const val SCAN_SETTINGS = "scan_settings"
    }

    // ==================== CONFIGURACIÓN DE NOTIFICACIONES ====================

    /**
     * IDs y configuración para notificaciones
     */
    object Notifications {
        const val CHANNEL_ID = "ble_rfid_channel"
        const val CHANNEL_NAME = "BLE RFID Scanner"
        const val CONNECTION_NOTIFICATION_ID = 1001
        const val RFID_SCAN_NOTIFICATION_ID = 1002
    }

    // ==================== VALIDACIÓN ====================

    /**
     * Valida si una dirección MAC es válida
     */
    fun isValidMacAddress(address: String): Boolean {
        val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return address.matches(macRegex)
    }

    /**
     * Valida si un UID RFID es válido
     */
    fun isValidRfidUid(uid: String): Boolean {
        val cleanUid = uid.replace(UID_SEPARATOR, "")
        return cleanUid.matches("^[0-9A-Fa-f]{8,20}$".toRegex())
    }

    /**
     * Obtiene la configuración completa del ESP32
     */
    data class Esp32Config(
        val macAddress: String = ESP32_MAC_ADDRESS,
        val deviceName: String = ESP32_DEVICE_NAME,
        val serviceUuid: UUID = SERVICE_UUID,
        val rfidCharacteristic: UUID = RFID_DATA_CHARACTERISTIC_UUID,
        val commandCharacteristic: UUID = COMMAND_CHARACTERISTIC_UUID,
        val statusCharacteristic: UUID = STATUS_CHARACTERISTIC_UUID
    ) {
        fun isValid(): Boolean {
            return isValidMacAddress(macAddress) && deviceName.isNotBlank()
        }
    }
}