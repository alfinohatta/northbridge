package com.example.northbridge

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.ui.CalibrationAdapter
import com.example.northbridge.viewmodel.CalibrationViewModel
import kotlinx.coroutines.launch

class CalibrationActivity : AppCompatActivity() {

    private val viewModel: CalibrationViewModel by viewModels {
        CalibrationViewModel.Factory(
            (application as NorthbridgeApplication).riskRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val scoreView: TextView = findViewById(R.id.calibrationScore)
        val skillView: TextView = findViewById(R.id.skillScore)
        val recyclerView: RecyclerView = findViewById(R.id.historyRecyclerView)
        val adapter = CalibrationAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                    scoreView.text = if (user?.calibrationScore != null) "%.4f".format(user.calibrationScore) else "--"
                    toolbar.title = "Performance: ${user?.fullName ?: ""}"
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.brierSkillScore.collect { skillScore ->
                    skillView.text = if (skillScore != null) "%.2f".format(skillScore) else "--"
                    // Change color based on skill
                    if (skillScore != null) {
                        skillView.setTextColor(if (skillScore > 0) getColor(R.color.divergence_low) else getColor(R.color.divergence_high))
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forecastHistory.collect { history ->
                    adapter.submitList(history)
                }
            }
        }
    }
}
