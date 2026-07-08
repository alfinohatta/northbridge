package com.example.northbridge.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.northbridge.R
import com.example.northbridge.db.entity.NotificationEntity

class NotificationAdapter(private val onNotificationClick: (NotificationEntity) -> Unit) :
    ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view, onNotificationClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View, private val onNotificationClick: (NotificationEntity) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.notifTitle)
        private val message: TextView = itemView.findViewById(R.id.notifMessage)
        private val time: TextView = itemView.findViewById(R.id.notifTime)
        private val container: View = itemView.findViewById(R.id.notificationContainer)

        fun bind(notification: NotificationEntity) {
            title.text = notification.title
            message.text = notification.message
            time.text = notification.createdAt
            
            if (notification.isRead) {
                itemView.alpha = 0.5f
            } else {
                itemView.alpha = 1.0f
            }

            container.setOnClickListener {
                onNotificationClick(notification)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem == newItem
        }
    }
}
