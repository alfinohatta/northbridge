package com.example.northbridge.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.R
import com.example.northbridge.db.entity.AuditLogEntity

class LogAdapter : ListAdapter<AuditLogEntity, LogAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val actionTitle: TextView = itemView.findViewById(R.id.logActionTitle)
        private val details: TextView = itemView.findViewById(R.id.logDetails)
        private val time: TextView = itemView.findViewById(R.id.logTime)

        fun bind(log: AuditLogEntity) {
            actionTitle.text = "${log.action.name} - ${log.entityType}"
            details.text = "Entity ID: ${log.entityId} | User: ${log.performedBy ?: "System"}"
            time.text = log.createdAt // Ideally format this timestamp
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AuditLogEntity>() {
        override fun areItemsTheSame(oldItem: AuditLogEntity, newItem: AuditLogEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AuditLogEntity, newItem: AuditLogEntity): Boolean {
            return oldItem == newItem
        }
    }
}
