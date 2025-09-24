package com.example.precastmanagementapp.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utilidades para manejo de permisos Bluetooth y ubicación
 */
object PermissionUtils {

    // Códigos de solicitud de permisos
    const val REQUEST_BLUETOOTH_PERMISSIONS = 1001
    const val REQUEST_LOCATION_PERMISSIONS = 1002
    const val REQUEST_ENABLE_BLUETOOTH = 1003
    const val REQUEST_ENABLE_LOCATION = 1004

    /**
     * Permisos requeridos según la versión de Android
     */
    fun getRequiredBluetoothPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    /**
     * Verifica si todos los permisos Bluetooth están concedidos
     */
    fun hasBluetoothPermissions(context: Context): Boolean {
        return getRequiredBluetoothPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    fun hasLocationPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return fineLocation && coarseLocation
    }

    /**
     * Solicita permisos Bluetooth
     */
    fun requestBluetoothPermissions(activity: Activity) {
        val permissions = getRequiredBluetoothPermissions()
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_BLUETOOTH_PERMISSIONS)
    }

    /**
     * Solicita permisos de ubicación
     */
    fun requestLocationPermissions(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_LOCATION_PERMISSIONS)
    }

    /**
     * Verifica si el Bluetooth está habilitado
     */
    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Verifica si los servicios de ubicación están habilitados
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Solicita habilitar Bluetooth
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun requestEnableBluetooth(activity: Activity) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH)
    }

    /**
     * Solicita habilitar servicios de ubicación
     */
    fun requestEnableLocation(activity: Activity) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivityForResult(intent, REQUEST_ENABLE_LOCATION)
    }

    /**
     * Verifica si se debe mostrar una explicación del permiso
     */
    fun shouldShowBluetoothPermissionRationale(activity: Activity): Boolean {
        return getRequiredBluetoothPermissions().any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    /**
     * Verifica si se puede realizar escaneo BLE
     */
    fun canScanBle(context: Context): Boolean {
        return hasBluetoothPermissions(context) &&
                isBluetoothEnabled() &&
                isLocationEnabled(context)
    }

    /**
     * Obtiene una lista de permisos faltantes
     */
    fun getMissingPermissions(context: Context): List<String> {
        return getRequiredBluetoothPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Resultado del análisis de permisos
     */
    data class PermissionStatus(
        val hasAllPermissions: Boolean,
        val bluetoothEnabled: Boolean,
        val locationEnabled: Boolean,
        val missingPermissions: List<String>
    ) {
        fun canUseBle(): Boolean = hasAllPermissions && bluetoothEnabled && locationEnabled

        fun getStatusMessage(): String {
            return when {
                !hasAllPermissions -> "Permisos Bluetooth requeridos"
                !bluetoothEnabled -> "Bluetooth deshabilitado"
                !locationEnabled -> "Ubicación deshabilitada"
                else -> "Listo para usar BLE"
            }
        }
    }

    /**
     * Obtiene el estado completo de permisos
     */
    fun getPermissionStatus(context: Context): PermissionStatus {
        return PermissionStatus(
            hasAllPermissions = hasBluetoothPermissions(context),
            bluetoothEnabled = isBluetoothEnabled(),
            locationEnabled = isLocationEnabled(context),
            missingPermissions = getMissingPermissions(context)
        )
    }
}