package com.mk.medtrust.doctor.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.databinding.ItemAppointmentBinding

class HistoryDAdapter(
    private val onAppointmentStart : (Appointment) -> Unit
) : RecyclerView.Adapter<HistoryDAdapter.HistoryViewHolder>(){

    private val appointments: MutableList<Appointment> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemBinding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return HistoryViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val ap  = appointments[position]

        holder.itemBinding.apply {
            tvStatus.visibility = View.INVISIBLE
            tvName.text = ap.patientName
            tvTime.text = ap.slotTime

            btnStart.text = "View"

            btnStart.setOnClickListener {
                onAppointmentStart(ap)
            }
        }
    }

    override fun getItemCount(): Int  = appointments.size

    fun updateNewList(newList : List<Appointment>){
        appointments.clear()
        appointments.addAll(newList)
        Log.d("KrishnaMK", "$appointments $newList")

        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(val itemBinding: ItemAppointmentBinding): RecyclerView.ViewHolder(itemBinding.root)

}