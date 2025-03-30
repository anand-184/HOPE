package com.anand.hope

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SOSListAdapter(private val sosList: ArrayList<SOSModel>) :
    RecyclerView.Adapter<SOSListAdapter.SOSViewHolder>() {

    class SOSViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationText: TextView = view.findViewById(R.id.tv_sos_location)
        val disasterTypeText: TextView = view.findViewById(R.id.tv_sos_dt)
        val needsText: TextView = view.findViewById(R.id.tv_sos_needs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SOSViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos, parent, false)
        return SOSViewHolder(view)
    }

    override fun onBindViewHolder(holder: SOSViewHolder, position: Int) {
        val sos = sosList[position]
        holder.locationText.text = "Lat: ${sos.latitude}, Lng: ${sos.longitude}"
        holder.disasterTypeText.text = "Disaster: ${sos.disasterType}"
        holder.needsText.text = "Needs: ${sos.needs.joinToString(", ")}"
    }

    override fun getItemCount(): Int = sosList.size
}
