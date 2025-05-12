package com.institutmarianao.xo_agenda.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.institutmarianao.xo_agenda.R
import com.institutmarianao.xo_agenda.models.CalendarItem

class CalendarItemAdapter(private val items: List<CalendarItem>) :
    RecyclerView.Adapter<CalendarItemAdapter.CalendarItemViewHolder>() {

    class CalendarItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewItemTitle)
        val description: TextView = itemView.findViewById(R.id.textViewItemDescription)
        val dateTime: TextView = itemView.findViewById(R.id.textViewItemDateTime)
        val type: TextView = itemView.findViewById(R.id.textViewItemType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_calendar_item, parent, false)
        return CalendarItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarItemViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.dateTime.text = item.dateTime
        holder.type.text = item.type
    }

    override fun getItemCount(): Int = items.size
}
