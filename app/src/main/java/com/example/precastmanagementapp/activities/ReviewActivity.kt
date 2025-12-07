package com.example.precastmanagementapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.precastmanagementapp.BuildConfig
import com.example.precastmanagementapp.ElementResultAdapter
import com.example.precastmanagementapp.PrecastElement
import com.example.precastmanagementapp.R
import com.example.precastmanagementapp.databinding.ActivityReviewBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import timber.log.Timber

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var elementResultAdapter: ElementResultAdapter
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBackReviewActivity.setOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        elementResultAdapter = ElementResultAdapter(
            onClickListener = { element -> onElementClicked(element)}
        )
        binding.rvElementResults.apply {
            adapter = elementResultAdapter
            layoutManager = LinearLayoutManager(
                this@ReviewActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        fetchElementFromFirebase()
    }

    private fun fetchElementFromFirebase(){
        db.collection("projects")
            .document("racbasicsampleproject.rvt")
            .collection("elements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error fetching elements")
                    return@addSnapshotListener
                }

                val elements = snapshot?.documents?.mapNotNull {
                    it.toObject(PrecastElement::class.java)
                }
                elementResultAdapter.updateData(elements ?: emptyList())
                Timber.d("Elements fetched: $elements")
            }
    }

    private fun onElementClicked(element: PrecastElement){
        val intent = Intent(this, EditElementActivity::class.java)
        intent.putExtra("element", element)
        startActivity(intent)
    }
}