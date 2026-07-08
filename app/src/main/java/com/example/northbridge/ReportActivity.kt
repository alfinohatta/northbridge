package com.example.northbridge

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.print.PrintHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.ui.RiskAdapter
import com.example.northbridge.viewmodel.ReportViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private val viewModel: ReportViewModel by viewModels {
        ReportViewModel.Factory((application as NorthbridgeApplication).riskRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val dateText: TextView = findViewById(R.id.reportDate)
        val scrollView: NestedScrollView = findViewById(R.id.reportScrollView)
        val recyclerView: RecyclerView = findViewById(R.id.reportRecyclerView)
        val coverageText: TextView = findViewById(R.id.hedgeCoverageText)
        val flagsCountText: TextView = findViewById(R.id.riskFlagsCountText)
        val valueProtectedText: TextView = findViewById(R.id.valueProtectedText)
        val printButton: FloatingActionButton = findViewById(R.id.printButton)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.reportToolbar)

        toolbar.setNavigationOnClickListener { finish() }

        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        dateText.text = "Risk Committee Summary - ${sdf.format(Date())}"

        printButton.setOnClickListener {
            exportReportAsPdf(scrollView)
        }

        // Reuse RiskAdapter for consistency, but we could customize it for "Report Mode"
        val adapter = RiskAdapter(
            onForecastClick = {},
            onHedgeClick = {},
            onEventClick = {}
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reportMetrics.collect { metrics ->
                    metrics?.let {
                        flagsCountText.text = it.activeFlagsCount.toString()
                        coverageText.text = "${it.hedgeCoverage}%"
                        
                        val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(Locale.US)
                        valueProtectedText.text = currencyFormatter.format(it.valueProtected)
                        
                        adapter.submitList(it.topRisks)
                    }
                }
            }
        }
    }

    private fun exportReportAsPdf(scrollView: NestedScrollView) {
        val printHelper = PrintHelper(this)
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT

        // Capture the entire content of the ScrollView
        val child = scrollView.getChildAt(0) ?: return
        val bitmap = Bitmap.createBitmap(child.width, child.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw background white explicitly for PDF clarity
        canvas.drawColor(android.graphics.Color.WHITE)
        child.draw(canvas)

        printHelper.printBitmap("Northbridge_Risk_Report_${System.currentTimeMillis()}", bitmap)
    }
}
