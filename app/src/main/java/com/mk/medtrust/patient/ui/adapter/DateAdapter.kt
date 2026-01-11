package com.mk.medtrust.patient.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.databinding.ItemDateBinding
import com.mk.medtrust.patient.model.DateItem

class DateAdapter(
    private val onDateSelected: (DateItem) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val dates: MutableList<DateItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val itemDateBinding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(itemDateBinding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]

        with(holder.itemDateBinding) {
            day.text = dateItem.dayOfWeek
            date.text = dateItem.dayOfMonth.toString()

            container.isSelected = dateItem.isSelected && dateItem.isAvailable
            container.alpha = if (dateItem.isAvailable) 1f else 0.3f

            root.setOnClickListener {
                if (!dateItem.isAvailable) return@setOnClickListener
                // deselect all first
                dates.forEach { it.isSelected = false }
                dateItem.isSelected = true
                notifyDataSetChanged()

                onDateSelected(dateItem)
            }
        }


    }

    override fun getItemCount(): Int = dates.size

    inner class DateViewHolder(val itemDateBinding: ItemDateBinding) :
        RecyclerView.ViewHolder(itemDateBinding.root) {

    }

    fun updateNewList(newList: List<DateItem>) {
        dates.clear()
        dates.addAll(newList)
        notifyDataSetChanged()
    }
}






