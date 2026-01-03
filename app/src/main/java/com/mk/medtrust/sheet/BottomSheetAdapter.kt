package com.mk.medtrust.sheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.databinding.ActivityLoginBinding
import com.mk.medtrust.databinding.ItemOptionBinding

class BottomSheetAdapter(
    private val options : List<OptionItem>,
    private val callback: OptionCLickListener
) : RecyclerView.Adapter<BottomSheetAdapter.ViewHolderClass>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemBinding = ItemOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val option  = options[position]

        with(holder.itemBinding){
            textOption.text = option.name
        }
        holder.itemBinding.root.setOnClickListener {
            callback.onOptionClickDo(option.name)
        }
    }

    override fun getItemCount(): Int  = options.size
    inner class ViewHolderClass(val itemBinding: ItemOptionBinding) : RecyclerView.ViewHolder(itemBinding.root)

}