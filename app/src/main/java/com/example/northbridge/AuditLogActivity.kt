package com.example.northbridge

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.ui.LogAdapter
import com.example.northbridge.viewmodel.RiskViewModel
import kotlinx.coroutines.launch

class AuditLogActivity : AppCompatActivity() {

    private val viewModel: RiskViewModel by viewModels {
        RiskViewModel.Factory((application as NorthbridgeApplication).riskRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audit_log)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.logRecyclerView)
        val adapter = LogAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAuditLogs.collect { logs ->
                    adapter.submitList(logs)
                }
            }
        }
    }
}
