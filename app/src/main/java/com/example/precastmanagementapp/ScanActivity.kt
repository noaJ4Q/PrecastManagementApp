package com.example.precastmanagementapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.precastmanagementapp.databinding.ActivityScanBinding

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
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
            // SEND "ENABLE SCAN" MESSAGE TO SCANNER
            // SWITCH TO STOP SCAN BUTTON
            // SHOW LOADING
        }

        binding.btnBackScanActivity.setOnClickListener { finish() }
    }

    private fun startRfidScan(){
        isScanningRfid = true
        Log.d("TEST", "STARTING RFID...")
    }

    private fun stopRfidScan(){
        isScanningRfid = false
        Log.d("TEST", "STOPING RFID...")
    }
}