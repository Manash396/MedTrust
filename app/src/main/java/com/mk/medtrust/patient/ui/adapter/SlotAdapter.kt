package com.mk.medtrust.patient.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.R
import com.mk.medtrust.databinding.ItemSlotBinding
import com.mk.medtrust.patient.model.SlotItem

class SlotAdapter(
    private val onSlotSelected: (SlotItem) -> Unit
) : RecyclerView.Adapter<SlotAdapter.SlotViewHolder>(){

    private val slots : MutableList<SlotItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
      val itemViewBinding = ItemSlotBinding.inflate(LayoutInflater.from(parent.context), parent , false)
      return SlotViewHolder(itemViewBinding)
    }

        override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
            val slot  = slots[position]

            with(holder.itemViewBinding){
                time.text = slot.time

                container.isSelected  = slot.isSelected && slot.isAvailable
                container.alpha = if (slot.isAvailable) 1f else 0.3f

                root.setOnClickListener {
                    if (!slot.isAvailable) return@setOnClickListener
                    slots.forEach { it.isSelected = false }
                    slot.isSelected = true
                    notifyDataSetChanged()

                    onSlotSelected(slot)
                }
            }
    }

    override fun getItemCount(): Int  = slots.size

    fun updateNewList(newList :  List<SlotItem>){
        slots.clear()
        slots.addAll(newList)
        notifyDataSetChanged()
    }

    inner class SlotViewHolder(val itemViewBinding : ItemSlotBinding) : RecyclerView.ViewHolder(itemViewBinding.root)


}