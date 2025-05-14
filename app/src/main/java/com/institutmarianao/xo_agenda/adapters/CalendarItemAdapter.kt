package com.institutmarianao.xo_agenda.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.institutmarianao.xo_agenda.R
import com.institutmarianao.xo_agenda.models.CalendarItem



class CalendarItemAdapter(
    private val items: List<CalendarItem>,
    private val listener: OnItemActionListener
) :
    RecyclerView.Adapter<CalendarItemAdapter.CalendarItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_calendar_item, parent, false)
        return CalendarItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarItemViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.dateTime.text = item.dateTimeText
        holder.type.text = item.tipo
        holder.itemView.setOnClickListener { view: View ->
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.menu_item_options)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                val id = menuItem.itemId
                if (id == R.id.action_edit) {
                    listener.onEdit(item)
                    return@setOnMenuItemClickListener true
                } else if (id == R.id.action_delete) {
                    listener.onDelete(item)
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CalendarItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var description: TextView
        var dateTime: TextView
        var type: TextView

        init {
            title = itemView.findViewById(R.id.textViewItemTitle)
            description = itemView.findViewById(R.id.textViewItemDescription)
            dateTime = itemView.findViewById(R.id.textViewItemDateTime)
            type = itemView.findViewById(R.id.textViewItemType)
        }
    }
}
// Interfaz para manejar acciones de editar y borrar
interface OnItemActionListener {
    fun onEdit(item: CalendarItem)
    fun onDelete(item: CalendarItem)
}