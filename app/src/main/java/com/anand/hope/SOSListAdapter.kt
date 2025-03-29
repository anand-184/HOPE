package com.anand.hope

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SOSListAdapter : ListAdapter<SOSModel, SOSListAdapter.SOSViewHolder>(SOSDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SOSViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sos, parent, false)
        return SOSViewHolder(view)
    }

    override fun onBindViewHolder(holder: SOSViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SOSViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationText: TextView = itemView.findViewById(R.id.tv_sos_location)
        private val needsText: TextView = itemView.findViewById(R.id.tv_sos_needs)

        fun bind(sos: SOSModel) {
            locationText.text = "Location: ${sos.latitude}, ${sos.longitude}"
            needsText.text = "Needs: ${sos.needs.joinToString(", ")}"

        }
    }


}
class SOSDiffCallback : DiffUtil.ItemCallback<SOSModel>() {
    override fun areItemsTheSame(oldItem: SOSModel, newItem: SOSModel): Boolean {
        return oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude
    }

    override fun areContentsTheSame(oldItem: SOSModel, newItem: SOSModel): Boolean {
        return oldItem == newItem
    }
}