package com.example.northbridge

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.northbridge.viewmodel.EventTrendViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class EventTrendActivity : AppCompatActivity() {

    private val viewModel: EventTrendViewModel by viewModels {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        EventTrendViewModel.Factory(
            (application as NorthbridgeApplication).riskRepository,
            eventId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_trend)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val titleView: TextView = findViewById(R.id.eventTitle)
        val chart: LineChart = findViewById(R.id.trendChart)

        // Configure Chart for Premium look
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDrawGridBackground(false)
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = true

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    titleView.text = event?.title ?: "Event Trend"
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.internalForecasts, viewModel.externalMarketData) { internal, external ->
                    Pair(internal, external)
                }.collect { (internal, external) ->
                    val internalEntries = internal.mapIndexed { index, forecast ->
                        Entry(index.toFloat(), forecast.probability.toFloat())
                    }
                    val externalEntries = external.mapIndexed { index, market ->
                        Entry(index.toFloat(), market.probability.toFloat())
                    }

                    val internalSet = LineDataSet(internalEntries, "Internal (ICP)").apply {
                        color = Color.parseColor("#002D72") // Primary
                        setCircleColor(Color.parseColor("#002D72"))
                        lineWidth = 3f
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }
                    val externalSet = LineDataSet(externalEntries, "External (EMP)").apply {
                        color = Color.parseColor("#008291") // Accent
                        setCircleColor(Color.parseColor("#008291"))
                        lineWidth = 3f
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    chart.data = LineData(internalSet, externalSet)
                    chart.animateX(500)
                    chart.invalidate()
                }
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
