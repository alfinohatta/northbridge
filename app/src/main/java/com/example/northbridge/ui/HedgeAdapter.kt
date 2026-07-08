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
import com.example.northbridge.db.entity.HedgeRecommendationEntity
import com.example.northbridge.model.HedgeStatus

class HedgeAdapter(
    private val onApprove: (HedgeRecommendationEntity) -> Unit,
    private val onReject: (HedgeRecommendationEntity) -> Unit,
    private val onExecute: (HedgeRecommendationEntity) -> Unit
) : ListAdapter<HedgeRecommendationEntity, HedgeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hedge, parent, false)
        return ViewHolder(view, onApprove, onReject, onExecute)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onApprove: (HedgeRecommendationEntity) -> Unit,
        private val onReject: (HedgeRecommendationEntity) -> Unit,
        private val onExecute: (HedgeRecommendationEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val actionText: TextView = itemView.findViewById(R.id.actionText)
        private val instrumentType: TextView = itemView.findViewById(R.id.instrumentType)
        private val amount: TextView = itemView.findViewById(R.id.amount)
        private val status: TextView = itemView.findViewById(R.id.status)
        private val approveButton: Button = itemView.findViewById(R.id.approveButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
        private val executeButton: Button = itemView.findViewById(R.id.executeButton)

        fun bind(recommendation: HedgeRecommendationEntity) {
            actionText.text = recommendation.recommendedAction
            instrumentType.text = recommendation.instrumentType.name.replace("_", " ")
            amount.text = "%.2f %s".format(recommendation.notionalAmount, recommendation.currency)
            status.text = recommendation.status.name
            
            val statusColor = when (recommendation.status) {
                HedgeStatus.EXECUTED -> itemView.context.getColor(R.color.success)
                HedgeStatus.REJECTED -> itemView.context.getColor(R.color.error)
                HedgeStatus.APPROVED -> itemView.context.getColor(R.color.info)
                else -> itemView.context.getColor(R.color.secondary)
            }
            status.backgroundTintList = android.content.res.ColorStateList.valueOf(statusColor)

            when (recommendation.status) {
                HedgeStatus.PROPOSED -> {
                    approveButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                    executeButton.visibility = View.GONE
                }
                HedgeStatus.APPROVED -> {
                    approveButton.visibility = View.GONE
                    rejectButton.visibility = View.GONE
                    executeButton.visibility = View.VISIBLE
                }
                else -> {
                    approveButton.visibility = View.GONE
                    rejectButton.visibility = View.GONE
                    executeButton.visibility = View.GONE
                }
            }

            approveButton.setOnClickListener { onApprove(recommendation) }
            rejectButton.setOnClickListener { onReject(recommendation) }
            executeButton.setOnClickListener { onExecute(recommendation) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HedgeRecommendationEntity>() {
        override fun areItemsTheSame(oldItem: HedgeRecommendationEntity, newItem: HedgeRecommendationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HedgeRecommendationEntity, newItem: HedgeRecommendationEntity): Boolean {
            return oldItem == newItem
        }
    }
}
