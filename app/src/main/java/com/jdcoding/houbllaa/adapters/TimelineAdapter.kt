package com.jdcoding.houbllaa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jdcoding.houbllaa.R
import com.jdcoding.houbllaa.models.PregnancyWeekInfo

class TimelineAdapter(private var weeks: List<PregnancyWeekInfo>) : 
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {
    
    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekTitle: TextView = itemView.findViewById(R.id.tvWeekTitle)
        val weekDescription: TextView = itemView.findViewById(R.id.tvWeekDescription)
        val timelineDot: View = itemView.findViewById(R.id.viewTimelineDot)
        val timelineLine: View = itemView.findViewById(R.id.viewTimelineLine)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_week, parent, false)
        return TimelineViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val weekInfo = weeks[position]
        
        // Set title and description
        holder.weekTitle.text = weekInfo.title
        holder.weekDescription.text = weekInfo.description
        
        // Highlight current week
        holder.timelineDot.isSelected = weekInfo.isCurrentWeek
        
        // If this is the current week, make the text color more prominent
        if (weekInfo.isCurrentWeek) {
            holder.weekTitle.setTextColor(holder.itemView.context.getColor(R.color.colorPrimary))
            holder.weekDescription.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        } else {
            holder.weekTitle.setTextColor(holder.itemView.context.getColor(android.R.color.black))
            holder.weekDescription.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }
        
        // Hide the connecting line for the last item
        if (position == weeks.size - 1) {
            holder.timelineLine.visibility = View.GONE
        } else {
            holder.timelineLine.visibility = View.VISIBLE
        }
    }
    
    override fun getItemCount() = weeks.size
    
    fun updateData(newWeeks: List<PregnancyWeekInfo>) {
        weeks = newWeeks
        notifyDataSetChanged()
    }
}
