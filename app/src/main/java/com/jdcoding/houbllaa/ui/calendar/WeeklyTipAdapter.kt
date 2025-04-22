package com.jdcoding.houbllaa.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jdcoding.houbllaa.R

/**
 * Adapter for displaying weekly pregnancy tips
 */
class WeeklyTipAdapter(private var tips: List<String> = emptyList()) : 
    RecyclerView.Adapter<WeeklyTipAdapter.TipViewHolder>() {

    class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipText: TextView = itemView.findViewById(R.id.tv_tip_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weekly_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.tipText.text = "- $tip"
    }

    override fun getItemCount(): Int = tips.size

    fun updateTips(newTips: List<String>) {
        tips = newTips
        notifyDataSetChanged()
    }
}
