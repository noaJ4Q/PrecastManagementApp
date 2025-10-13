package com.example.precastmanagementapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.precastmanagementapp.BuildConfig
import com.example.precastmanagementapp.R
import com.example.precastmanagementapp.ScanResultAdapter
import com.example.precastmanagementapp.ble.ConnectionEventListener
import com.example.precastmanagementapp.ble.ConnectionManager
import com.example.precastmanagementapp.ble.isReadable
import com.example.precastmanagementapp.databinding.ActivityPairBinding
import com.example.precastmanagementapp.hasPermission
import com.example.precastmanagementapp.hasRequiredBluetoothPermissions
import com.example.precastmanagementapp.requestRelevantRuntimePermissions
import timber.log.Timber
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
                Timber.w("Connecting to $address")
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
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
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
        ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        } else {
            startBleScan()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isScanning) {
            stopBleScan()
        }
        ConnectionManager.unregisterListener(connectionEventListener)
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
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with (result.device) {
                    Timber.i("Found BLE device! Name ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                Timber.tag("TEST").d("PRE-INIT ACTIVITY SCAN_ACTIVITY")
                Intent(this@PairActivity, ScanActivity::class.java).also {
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
                    startActivity(it)
                }
            }

            onDisconnect = {
                val deviceName = if (hasRequiredBluetoothPermissions()) {
                    it.name
                } else {
                    "device"
                }
                runOnUiThread {
                    AlertDialog.Builder(this@PairActivity)
                        .setTitle("Disconnected")
                        .setMessage("Disconnected or unable to connect to device $deviceName")
                        .setPositiveButton("Ok", null)
                        .show()
                }
            }
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