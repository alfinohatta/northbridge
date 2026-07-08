package com.example.northbridge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.northbridge.model.UserRole
import com.example.northbridge.ui.RiskAdapter
import com.example.northbridge.viewmodel.RiskViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: RiskViewModel by viewModels {
        RiskViewModel.Factory((application as NorthbridgeApplication).riskRepository)
    }

    private fun showResolveDialog(eventId: Long) {
        val options = arrayOf("Resolved YES", "Resolved NO", "Cancel")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Resolve Event")
            .setItems(options) { _, which ->
                val status = when (which) {
                    0 -> com.example.northbridge.model.EventStatus.RESOLVED_YES
                    1 -> com.example.northbridge.model.EventStatus.RESOLVED_NO
                    else -> com.example.northbridge.model.EventStatus.CANCELLED
                }
                viewModel.resolveEventSim(eventId, status)
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val userNameText: TextView = findViewById(R.id.userNameText)
        val roleChip: Chip = findViewById(R.id.roleChip)
        val emptyState: View = findViewById(R.id.emptyState)
        val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.swipeRefresh)
        val refreshFab: FloatingActionButton = findViewById(R.id.refreshSignalsButton)
        val centerLoading: View = findViewById(R.id.centerLoading)

        val adapter = RiskAdapter(
            onForecastClick = { eventId ->
                val intent = Intent(this, ForecastActivity::class.java).apply {
                    putExtra(ForecastActivity.EXTRA_EVENT_ID, eventId)
                }
                startActivity(intent)
            },
            onHedgeClick = { flagId ->
                val intent = Intent(this, HedgeActivity::class.java).apply {
                    putExtra(HedgeActivity.EXTRA_FLAG_ID, flagId)
                }
                startActivity(intent)
            },
            onEventClick = { eventId ->
                val intent = Intent(this, EventTrendActivity::class.java).apply {
                    putExtra(EventTrendActivity.EXTRA_EVENT_ID, eventId)
                }
                startActivity(intent)
            },
            onLongClick = { eventId ->
                showResolveDialog(eventId)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_report -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                    true
                }
                R.id.action_audit -> {
                    startActivity(Intent(this, AuditLogActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<View>(R.id.performanceButton).setOnClickListener {
            startActivity(Intent(this, CalibrationActivity::class.java))
        }

        findViewById<View>(R.id.notificationsButton).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        refreshFab.setOnClickListener {
            viewModel.simulateMarketSignals()
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.simulateMarketSignals()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { session ->
                    if (session == null) return@collect
                    adapter.currentRole = session.role
                    userNameText.text = session.name
                    roleChip.text = session.role.name
                    
                    val isExec = session.role == UserRole.CFO || session.role == UserRole.CRO
                    toolbar.menu.findItem(R.id.action_report)?.isVisible = isExec
                    toolbar.menu.findItem(R.id.action_audit)?.isVisible = isExec
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.flaggedEvents.collect { events ->
                    adapter.submitList(events)
                    emptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isRefreshing.collect { isRefreshing ->
                    swipeRefresh.isRefreshing = isRefreshing
                    centerLoading.visibility = if (isRefreshing && adapter.itemCount == 0) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
