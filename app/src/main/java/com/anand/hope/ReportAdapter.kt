package com.anand.hope

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter : ListAdapter<EmergencyReport, ReportAdapter.ReportViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emergency_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = getItem(position)
        holder.bind(report)
    }

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)

        fun bind(report: EmergencyReport) {
            locationText.text = "📍 ${report.latitude}, ${report.longitude}"
            descriptionText.text = report.description
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<EmergencyReport>() {
        override fun areItemsTheSame(oldItem: EmergencyReport, newItem: EmergencyReport): Boolean {
            return oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(oldItem: EmergencyReport, newItem: EmergencyReport): Boolean {
            return oldItem == newItem
        }
    }
}