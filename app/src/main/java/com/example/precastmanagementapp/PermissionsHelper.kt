package com.example.precastmanagementapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Determine whether the current [Context] has been granted the relevant [Manifest.permission].
 */
fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) ==
            PackageManager.PERMISSION_GRANTED
}

fun Context.hasRequiredBluetoothPermissions(): Boolean {
    // For Android 12 (API 31) or higher: BLUETOOTH_SCAN and BLUETOOTH_CONNECT
    // For lower than Android 12: ACCESS_FINE_LOCATION
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

fun Activity.requestRelevantRuntimePermissions(requestCode: Int) {
    if (hasRequiredBluetoothPermissions()) {
        Log.d("PermissionHelper", "Required permission(s) for Bluetooth already granted")
        return
    }
    when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
            requestLocationPermission(requestCode)
        }
        else -> {
            requestBluetoothPermission(requestCode)
        }
    }
}

private fun Activity.requestLocationPermission(requestCode: Int) {
    runOnUiThread {
        AlertDialog.Builder(this)
            .setTitle("Location permission required")
            .setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
            )
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
            }
            .show()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun Activity.requestBluetoothPermission(requestCode: Int) {
    runOnUiThread {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth permission required")
            .setMessage(
                "Starting from Android 12, the system requires apps to be granted " +
                        "Bluetooth access in order to scan for and connect to BLE devices."
            )
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    requestCode
                )
            }
            .show()
    }
}