package com.example.northbridge.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.R
import com.example.northbridge.model.ResolvedForecast

class CalibrationAdapter : ListAdapter<ResolvedForecast, CalibrationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resolved_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        private val yourForecast: TextView = itemView.findViewById(R.id.yourForecast)
        private val outcome: TextView = itemView.findViewById(R.id.outcome)

        fun bind(resolved: ResolvedForecast) {
            eventTitle.text = resolved.event.title
            yourForecast.text = "Forecast: ${resolved.forecast.probability.toInt()}%"
            outcome.text = "Result: ${resolved.event.status.name.replace("RESOLVED_", "")}"
            
            // Visual indicator of accuracy
            val outcomeValue = if (resolved.event.status == com.example.northbridge.model.EventStatus.RESOLVED_YES) 100.0 else 0.0
            val error = Math.abs(resolved.forecast.probability - outcomeValue)
            itemView.findViewById<View>(R.id.accuracyIcon).alpha = if (error < 20) 1.0f else 0.3f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ResolvedForecast>() {
        override fun areItemsTheSame(oldItem: ResolvedForecast, newItem: ResolvedForecast): Boolean {
            return oldItem.forecast.id == newItem.forecast.id
        }

        override fun areContentsTheSame(oldItem: ResolvedForecast, newItem: ResolvedForecast): Boolean {
            return oldItem == newItem
        }
    }
}
