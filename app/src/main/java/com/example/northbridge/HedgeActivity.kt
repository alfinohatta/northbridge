package com.example.northbridge

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.model.HedgeStatus
import com.example.northbridge.model.SessionManager
import com.example.northbridge.ui.HedgeAdapter
import com.example.northbridge.viewmodel.HedgeViewModel
import kotlinx.coroutines.launch

class HedgeActivity : AppCompatActivity() {

    private val viewModel: HedgeViewModel by viewModels {
        val flagId = intent.getLongExtra(EXTRA_FLAG_ID, -1L)
        val userId = SessionManager.currentUser.value?.id ?: -1L
        HedgeViewModel.Factory(
            (application as NorthbridgeApplication).riskRepository,
            flagId,
            userId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hedge)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val flagId = intent.getLongExtra(EXTRA_FLAG_ID, -1L)
        val recyclerView: RecyclerView = findViewById(R.id.hedgeRecyclerView)
        val adapter = HedgeAdapter(
            onApprove = { viewModel.updateStatus(it, HedgeStatus.APPROVED) },
            onReject = { viewModel.updateStatus(it, HedgeStatus.REJECTED) },
            onExecute = { recommendation ->
                val intent = Intent(this, ExecuteHedgeActivity::class.java).apply {
                    putExtra(ExecuteHedgeActivity.EXTRA_FLAG_ID, flagId)
                    putExtra(ExecuteHedgeActivity.EXTRA_HEDGE_ID, recommendation.id)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recommendations.collect { recommendations ->
                    adapter.submitList(recommendations)
                }
            }
        }
    }

    companion object {
        const val EXTRA_FLAG_ID = "extra_flag_id"
    }
}
