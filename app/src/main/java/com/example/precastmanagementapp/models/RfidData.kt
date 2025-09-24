package com.example.precastmanagementapp.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class RfidData(
    val uid: String, // RFID tag unique ID
    val cardType: RfidCardType, // Tipo de tarjeta RFID
    val rawData: ByteArray? = null, // Datos crudos del tag (opcional)
    val timestamp: Long = System.currentTimeMillis(), // Timestamp de lectura
    val signalStrength: Int? = null, // Intensidad de señal RSSI (opcional)
    val blockData: Map<Int, String>? = null, // Datos de bloques específicos (opcional)
    val isValid: Boolean = true // Validez de los datos recibidos
) : Parcelable {

    fun getReadDate(): Date = Date(timestamp)
    fun getFormattedUid(): String {
        return uid.chunked(2).joinToString(":")
    }
    fun isRecentRead(): Boolean {
        return (System.currentTimeMillis() - timestamp) < 5000
    }
    fun getRawDataHex(): String? {
        return rawData?.joinToString("") { "%02x".format(it) }?.uppercase()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RfidData

        if (uid != other.uid) return false
        if (cardType != other.cardType) return false
        if (rawData != null) {
            if (other.rawData == null) return false
            if (!rawData.contentEquals(other.rawData)) return false
        } else if (other.rawData != null) return false
        if (timestamp != other.timestamp) return false
        if (signalStrength != other.signalStrength) return false
        if (blockData != other.blockData) return false
        if (isValid != other.isValid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + cardType.hashCode()
        result = 31 * result + (rawData?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (signalStrength ?: 0)
        result = 31 * result + (blockData?.hashCode() ?: 0)
        result = 31 * result + isValid.hashCode()
        return result
    }

}

/**
 * Enum para tipos de tarjetas RFID soportadas
 */
enum class RfidCardType(val typeName: String, val frequency: String) {
    MIFARE_CLASSIC_1K("Mifare Classic 1K", "13.56 MHz"),
    MIFARE_CLASSIC_4K("Mifare Classic 4K", "13.56 MHz"),
    MIFARE_ULTRALIGHT("Mifare Ultralight", "13.56 MHz"),
    NTAG213("NTAG213", "13.56 MHz"),
    NTAG215("NTAG215", "13.56 MHz"),
    NTAG216("NTAG216", "13.56 MHz"),
    ISO14443A("ISO14443 Type A", "13.56 MHz"),
    ISO14443B("ISO14443 Type B", "13.56 MHz"),
    EM4100("EM4100", "125 kHz"),
    UNKNOWN("Desconocido", "N/A");

    companion object {
        /**
         * Obtiene el tipo de tarjeta basado en el UID o código de identificación
         */
        fun fromString(typeString: String?): RfidCardType {
            return when (typeString?.uppercase()) {
                "MIFARE_1K", "CLASSIC_1K" -> MIFARE_CLASSIC_1K
                "MIFARE_4K", "CLASSIC_4K" -> MIFARE_CLASSIC_4K
                "ULTRALIGHT" -> MIFARE_ULTRALIGHT
                "NTAG213" -> NTAG213
                "NTAG215" -> NTAG215
                "NTAG216" -> NTAG216
                "ISO14443A" -> ISO14443A
                "ISO14443B" -> ISO14443B
                "EM4100" -> EM4100
                else -> UNKNOWN
            }
        }
    }
}