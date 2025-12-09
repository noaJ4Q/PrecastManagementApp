package com.example.precastmanagementapp.activities

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.precastmanagementapp.R
import com.example.precastmanagementapp.ble.ConnectionEventListener
import com.example.precastmanagementapp.ble.ConnectionManager
import com.example.precastmanagementapp.ble.ConnectionManager.parcelableExtraCompat
import com.example.precastmanagementapp.ble.isReadable
import com.example.precastmanagementapp.ble.isWritable
import com.example.precastmanagementapp.ble.toHexString
import com.example.precastmanagementapp.databinding.ActivityScanBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private val device: BluetoothDevice by lazy {
        intent.parcelableExtraCompat(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")
    }
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)

    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    private val characteristicProperties by lazy {
        characteristics.associateWith { characteristic ->
            mutableListOf<CharacteristicProperty>().apply {
                if (characteristic.isReadable()) add(CharacteristicProperty.Readable)
                if (characteristic.isWritable()) add(CharacteristicProperty.Writable)
            }.toList()
        }
    }

    private var isScanningRfid = false
        set(value) {
            field = value
            binding.btnStartScanTag.text = if (field) "Stop scan" else "Scan again"
            binding.btnStartScanTag.background.setTint(
                if (field) resources.getColor(R.color.white) else resources.getColor(R.color.primary)
            )
            binding.btnStartScanTag.setTextColor(
                if (field) resources.getColor(R.color.primaryText) else resources.getColor(R.color.white)
            )
            binding.liScanTag.visibility = if (field) View.VISIBLE else View.INVISIBLE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConnectionManager.registerListener(connectionEventListener)

        binding = ActivityScanBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnStartScanTag.setOnClickListener {
            if (isScanningRfid){
                stopRfidScan()
            } else {
                startRfidScan()
            }
        }

//        beb5483e-36e1-4688-b7f5-ea07361b26a8 RFID scanner
//        00002a38-0000-1000-8000-00805f9b34fb Body sensor location (Heart Rate)
        val rfidIdCharUuid = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        val characteristic: BluetoothGattCharacteristic = characteristics.find { characteristic -> characteristic.uuid == rfidIdCharUuid } as BluetoothGattCharacteristic

        binding.btnBackScanActivity.setOnClickListener { finish() }
        binding.btnStartScanTag.setOnClickListener {
            ConnectionManager.readCharacteristic(device, characteristic)
        }
    }

    override fun onDestroy() {
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        super.onDestroy()
    }

    private fun startRfidScan(){
        isScanningRfid = true
    }

    private fun stopRfidScan(){
        isScanningRfid = false
    }

    private fun log(message: String) {
        val formattedMessage = "${dateFormatter.format(Date())}: $message"
        runOnUiThread {
            val uiText = binding.logTextView.text
            val currentLogText = uiText.ifEmpty { "Beginning of log." }
            binding.liScanTag.visibility = View.INVISIBLE
            binding.logTextView.visibility = View.VISIBLE
//            binding.logTextView.text = "$currentLogText\n$formattedMessage"
            // get rid of 9 last characters of message
            val message = message.substring(0, message.length - 9)
            binding.logTextView.text = "Detected tag:\n$message"
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {
                    AlertDialog.Builder(this@ScanActivity)
                        .setTitle("Disconnected")
                        .setMessage("Disconnected from device.")
                        .setPositiveButton("OK") { _, _ -> onBackPressed() }
                        .show()
                }
            }

            onCharacteristicRead = { _, characteristic, value ->
//                log("Read from ${characteristic.uuid}: ${value.toHexString()}")
//                log("Read from ${characteristic.uuid}: ${value.decodeToString()}")
                log(value.decodeToString())
            }

            onCharacteristicChanged = { _, characteristic, value ->
                val hexString = value.toHexString()
                val textString = String(value, Charsets.UTF_8)
                log("Value changed on ${characteristic.uuid}: ${textString}")
            }
        }
    }

    private enum class CharacteristicProperty {
        Readable,
        Writable,
        WritableWithoutResponse,
        Notifiable,
        Indicatable;

        val action
            get() = when (this) {
                Readable -> "Read"
                Writable -> "Write"
                WritableWithoutResponse -> "Write Without Response"
                Notifiable -> "Toggle Notifications"
                Indicatable -> "Toggle Indications"
            }
    }
}