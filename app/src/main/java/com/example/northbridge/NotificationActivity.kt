package com.example.northbridge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.ui.NotificationAdapter
import com.example.northbridge.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModel.Factory((application as NorthbridgeApplication).riskRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.notificationRecyclerView)
        val simulateButton: Button = findViewById(R.id.simulatePromptButton)

        val adapter = NotificationAdapter { notification ->
            viewModel.markAsRead(notification.id)
            if (notification.eventId != null) {
                val intent = Intent(this, ForecastActivity::class.java).apply {
                    putExtra(ForecastActivity.EXTRA_EVENT_ID, notification.eventId)
                }
                startActivity(intent)
            }
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        simulateButton.setOnClickListener {
            viewModel.simulateNewPrompt()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notifications.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
    }
}
