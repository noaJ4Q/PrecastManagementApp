package com.example.precastmanagementapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.precastmanagementapp.R
import com.example.precastmanagementapp.databinding.ActivityEditElementBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EditElementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditElementBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditElementBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSaveElement.setOnClickListener {
            // Save the element data to Firebase
            val elementRef = db.collection("projects")
                .document("racbasicsampleproject.rvt")
                .collection("elements")
                .document()
        }
    }
}