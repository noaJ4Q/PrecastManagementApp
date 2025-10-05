package com.example.precastmanagementapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.precastmanagementapp.R
import com.example.precastmanagementapp.ScanResultAdapter
import com.example.precastmanagementapp.ble.ConnectionManager
import com.example.precastmanagementapp.ble.isReadable
import com.example.precastmanagementapp.ble.printGattTable
import com.example.precastmanagementapp.ble.toHexString
import com.example.precastmanagementapp.databinding.ActivityPairBinding
import com.example.precastmanagementapp.hasPermission
import com.example.precastmanagementapp.hasRequiredBluetoothPermissions
import com.example.precastmanagementapp.requestRelevantRuntimePermissions
import java.util.UUID

private const val PERMISSION_REQUEST_CODE = 1

@SuppressLint("MissingPermission")
class PairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPairBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothAdapter = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setScanMode(ScanSettings.MATCH_MODE_STICKY)
        .build()

    private var isScanning = false
    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
//                connectGatt(this@PairActivity, false, gattCallback)
                ConnectionManager.connect(this, this@PairActivity)
            }
        }
    }

    private lateinit var bluetoothGatt: BluetoothGatt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPairBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBackPairActivity.setOnClickListener { finish() }

        binding.btnRealoadScanDevices.setOnClickListener {
            stopBleScan()
            startBleScan()
        }

        binding.imageView4.setOnClickListener {
            readRfidId()
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
        startBleScan()
    }

    override fun onPause() {
        super.onPause()
        stopBleScan()
    }

    private fun startBleScan() {
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions(PERMISSION_REQUEST_CODE)
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                printGattTable()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            val uuid = characteristic.uuid
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val rfidId = value.first().toInt() and 0xFF
                    Log.i(
                        "BluetoothGattCallback",
                        "Read characteristic $uuid:n${value.toHexString()} | $rfidId"
                    )
                }

                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                }

                else -> {
                    Log.e(
                        "BluetoothGattCallback",
                        "Characteristic read failed for $uuid, error: $status"
                    )
                }
            }
        }

    }

    private fun readRfidId() {
//        val rfidIdServiceUuid = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
//        val rfidIdCharUuid = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val rfidIdServiceUuid = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val rfidIdCharUuid = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        val rfidIdChar = bluetoothGatt
            .getService(rfidIdServiceUuid)?.getCharacteristic(rfidIdCharUuid)

        if (rfidIdChar?.isReadable() == true) {
            bluetoothGatt.readCharacteristic(rfidIdChar)
        }
    }

    private fun setupRecyclerView() {
        binding.rvScanResults.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@PairActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = binding.rvScanResults.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            with(result.device) {
                Log.i("ScanCallback", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
            }

            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with (result.device) {
                    Log.i("ScanCallback", "Found BLE device! Name ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }

    }

    private fun promptEnableBluetooth() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                bluetoothEnablingResult.launch(this)
            }
        }
    }

    private val bluetoothEnablingResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("TEST", "BLUETOOTH ENABLED")
        } else {
            Log.d("TEST", "BLUETOOTH IS NOT ENABLED")
            promptEnableBluetooth()
        }
    }

}