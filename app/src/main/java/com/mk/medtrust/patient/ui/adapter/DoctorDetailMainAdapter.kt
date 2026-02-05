package com.mk.medtrust.patient.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.databinding.ItemDateListDBinding
import com.mk.medtrust.databinding.ItemDoctorDetailTitleBinding
import com.mk.medtrust.databinding.ItemSlotGridDBinding

class DoctorDetailMainAdapter(
    private val slotAdapter: SlotAdapter,
    private val dateAdapter: DateAdapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    companion object{
        private const val TYPE_DATE_TITLE = 0
        private const val TYPE_DATE_LIST = 1
        private const val TYPE_SLOT_TITLE = 2
        private const val TYPE_SLOT_LIST = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> TYPE_DATE_TITLE
            1 -> TYPE_DATE_LIST
            2 -> TYPE_SLOT_TITLE
            else -> TYPE_SLOT_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater  = LayoutInflater.from(parent.context)

       return when(viewType){
            TYPE_DATE_TITLE, TYPE_SLOT_TITLE -> {
                val binding = ItemDoctorDetailTitleBinding.inflate(inflater,parent,false)
                TitleViewHolder(binding)
            }
            TYPE_DATE_LIST -> {
                val binding = ItemDateListDBinding.inflate(inflater,parent,false)
                DateListViewHolder(binding)
            }
           else -> {
               val binding = ItemSlotGridDBinding.inflate(inflater,parent,false)
               SlotListViewHolder(binding)
           }
       }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is DateListViewHolder -> {
                holder.itemBinding.dateRecyclerView.adapter = dateAdapter
            }
            is SlotListViewHolder -> {
                holder.itemBinding.slotRecyclerView.adapter = slotAdapter
            }
            is TitleViewHolder -> {
                holder.itemBinding.title.text = if (position == 0) "Select Date" else "Select Slot"
            }
        }
    }

    override fun getItemCount(): Int = 4

    class  DateListViewHolder(val itemBinding : ItemDateListDBinding): RecyclerView.ViewHolder(itemBinding.root){
        init {
            itemBinding.dateRecyclerView.layoutManager = LinearLayoutManager(
                itemBinding.root.context, LinearLayoutManager.HORIZONTAL, false
            )
            itemBinding.dateRecyclerView.isNestedScrollingEnabled = false
        }
    }

    class SlotListViewHolder(val itemBinding : ItemSlotGridDBinding): RecyclerView.ViewHolder(itemBinding.root){
        init {
            itemBinding.slotRecyclerView.layoutManager = GridLayoutManager(
                itemBinding.root.context , 3
            )
            itemBinding.slotRecyclerView.isNestedScrollingEnabled = false
        }
    }

    class TitleViewHolder(val itemBinding : ItemDoctorDetailTitleBinding): RecyclerView.ViewHolder(itemBinding.root){

    }

}