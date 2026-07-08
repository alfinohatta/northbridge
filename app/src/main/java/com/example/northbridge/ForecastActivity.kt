package com.example.northbridge

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.northbridge.model.SessionManager
import com.example.northbridge.viewmodel.ForecastViewModel
import kotlinx.coroutines.launch

class ForecastActivity : AppCompatActivity() {

    private val viewModel: ForecastViewModel by viewModels {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val userId = SessionManager.currentUser.value?.id ?: -1L
        ForecastViewModel.Factory(
            (application as NorthbridgeApplication).riskRepository,
            eventId,
            userId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val titleView: TextView = findViewById(R.id.eventTitle)
        val descView: TextView = findViewById(R.id.eventDescription)
        val probInput: EditText = findViewById(R.id.probabilityInput)
        val rationaleInput: EditText = findViewById(R.id.rationaleInput)
        val submitButton: Button = findViewById(R.id.submitButton)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    event?.let {
                        titleView.text = it.title
                        descView.text = it.description
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submissionStatus.collect { success ->
                    if (success == true) {
                        Toast.makeText(this@ForecastActivity, "Quantification submitted for audit", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (success == false) {
                        Toast.makeText(this@ForecastActivity, "Submission failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        submitButton.setOnClickListener {
            val probText = probInput.text.toString()
            if (probText.isNotEmpty()) {
                val probability = probText.toDoubleOrNull()
                if (probability != null && probability in 0.0..100.0) {
                    viewModel.submitForecast(probability, rationaleInput.text.toString())
                } else {
                    probInput.error = "Please enter a valid percentage (0-100)"
                }
            } else {
                probInput.error = "Required"
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
