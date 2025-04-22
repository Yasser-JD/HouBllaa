package com.jdcoding.houbllaa.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jdcoding.houbllaa.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class DaySelectorAdapter(
    private val context: Context,
    private val days: List<LocalDate>,
    private val selectedDay: LocalDate,
    private val onDaySelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<DaySelectorAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView as CardView
        val dayNameTextView: TextView = itemView.findViewById(R.id.tvDayName)
        val dayNumberTextView: TextView = itemView.findViewById(R.id.tvDayNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_selector, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        
        // Set day name (3-letter abbreviation)
        val dayName = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        holder.dayNameTextView.text = dayName
        
        // Set day number
        holder.dayNumberTextView.text = day.dayOfMonth.toString()
        
        // Check if this is the selected day
        val isSelected = day.isEqual(selectedDay)
        
        // Apply styling based on selection
        if (isSelected) {
            // Selected day styling (pink background)
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            holder.dayNameTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            holder.dayNumberTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } else {
            // Unselected day styling (white background)
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            holder.dayNameTextView.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
            holder.dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
        }
        
        // Set click listener
        holder.cardView.setOnClickListener {
            onDaySelected(day)
        }
    }

    override fun getItemCount(): Int = days.size
}
