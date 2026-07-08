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
import com.example.northbridge.db.entity.HedgeRecommendationEntity
import com.example.northbridge.model.SessionManager
import com.example.northbridge.viewmodel.HedgeViewModel
import kotlinx.coroutines.launch

class ExecuteHedgeActivity : AppCompatActivity() {

    private val viewModel: HedgeViewModel by viewModels {
        val flagId = intent.getLongExtra(EXTRA_FLAG_ID, -1L)
        val userId = SessionManager.currentUser.value?.id ?: -1L
        HedgeViewModel.Factory(
            (application as NorthbridgeApplication).riskRepository,
            flagId,
            userId
        )
    }

    private var targetRecommendation: HedgeRecommendationEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execute_hedge)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val hedgeId = intent.getLongExtra(EXTRA_HEDGE_ID, -1L)
        val actionText: TextView = findViewById(R.id.hedgeActionSummary)
        val detailsText: TextView = findViewById(R.id.hedgeDetailsSummary)
        val partnerInput: EditText = findViewById(R.id.partnerInput)
        val referenceInput: EditText = findViewById(R.id.referenceInput)
        val confirmButton: Button = findViewById(R.id.confirmExecuteButton)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recommendations.collect { list ->
                    targetRecommendation = list.find { it.id == hedgeId }
                    targetRecommendation?.let {
                        actionText.text = it.recommendedAction
                        detailsText.text = "Instrument: ${it.instrumentType.name} | Amount: ${it.notionalAmount} ${it.currency}"
                    }
                }
            }
        }

        confirmButton.setOnClickListener {
            val partner = partnerInput.text.toString()
            val reference = referenceInput.text.toString()

            if (partner.isEmpty()) {
                partnerInput.error = "Required"
                return@setOnClickListener
            }

            targetRecommendation?.let {
                viewModel.executeDetailed(it, partner, reference)
                Toast.makeText(this, "Mitigation executed and audit trail updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_FLAG_ID = "extra_flag_id"
        const val EXTRA_HEDGE_ID = "extra_hedge_id"
    }
}
