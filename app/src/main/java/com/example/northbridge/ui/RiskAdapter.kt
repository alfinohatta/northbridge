package com.example.northbridge.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.R
import com.example.northbridge.model.FlaggedEvent
import com.example.northbridge.model.UserRole

class RiskAdapter(
    private val onForecastClick: (Long) -> Unit,
    private val onHedgeClick: (Long) -> Unit,
    private val onEventClick: (Long) -> Unit,
    private val onLongClick: (Long) -> Unit = {}
) : ListAdapter<FlaggedEvent, RiskAdapter.ViewHolder>(DiffCallback()) {

    var currentRole: UserRole = UserRole.CFO
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view, onForecastClick, onHedgeClick, onEventClick, onLongClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, currentRole)
    }

    class ViewHolder(
        itemView: View,
        private val onForecastClick: (Long) -> Unit,
        private val onHedgeClick: (Long) -> Unit,
        private val onEventClick: (Long) -> Unit,
        private val onLongClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val exposure: TextView = itemView.findViewById(R.id.exposure)
        private val internalProb: TextView = itemView.findViewById(R.id.internalProb)
        private val externalProb: TextView = itemView.findViewById(R.id.externalProb)
        private val divergenceScore: TextView = itemView.findViewById(R.id.divergenceScore)
        private val divergenceProgress: com.google.android.material.progressindicator.LinearProgressIndicator = itemView.findViewById(R.id.divergenceProgress)
        private val forecastButton: Button = itemView.findViewById(R.id.forecastButton)
        private val hedgeButton: Button = itemView.findViewById(R.id.hedgeButton)

        fun bind(item: FlaggedEvent, role: UserRole) {
            val event = item.event
            val flag = item.flag

            title.text = event.title
            category.text = event.category.name
            exposure.text = "${event.exposureAmount ?: "N/A"} ${event.currency ?: ""}"

            if (flag != null) {
                internalProb.text = "${flag.internalConsensusProbability.toInt()}%"
                externalProb.text = "${flag.externalConsensusProbability.toInt()}%"
                divergenceScore.text = "%.1f GAP".format(flag.divergenceScore)
                divergenceScore.visibility = View.VISIBLE
                
                divergenceProgress.progress = flag.divergenceScore.toInt().coerceIn(0, 100)
                divergenceProgress.visibility = View.VISIBLE
                
                // Color coding based on divergence severity
                val color = if (flag.divergenceScore > 15) {
                    itemView.context.getColor(R.color.divergence_high)
                } else {
                    itemView.context.getColor(R.color.divergence_low)
                }
                divergenceProgress.setIndicatorColor(color)
                divergenceScore.backgroundTintList = android.content.res.ColorStateList.valueOf(color)

                // Only show Hedges to CFO/CRO
                hedgeButton.visibility = if (role == UserRole.CFO || role == UserRole.CRO) View.VISIBLE else View.GONE
            } else {
                internalProb.text = "--"
                externalProb.text = "--"
                divergenceScore.visibility = View.GONE
                divergenceProgress.visibility = View.GONE
                hedgeButton.visibility = View.GONE
            }

            forecastButton.setOnClickListener {
                onForecastClick(event.id)
            }

            hedgeButton.setOnClickListener {
                flag?.let { onHedgeClick(it.id) }
            }

            itemView.setOnClickListener {
                onEventClick(event.id)
            }

            itemView.setOnLongClickListener {
                if (role == UserRole.CFO || role == UserRole.CRO) {
                    onLongClick(event.id)
                    true
                } else {
                    false
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FlaggedEvent>() {
        override fun areItemsTheSame(oldItem: FlaggedEvent, newItem: FlaggedEvent): Boolean {
            return oldItem.event.id == newItem.event.id
        }

        override fun areContentsTheSame(oldItem: FlaggedEvent, newItem: FlaggedEvent): Boolean {
            return oldItem == newItem
        }
    }
}
