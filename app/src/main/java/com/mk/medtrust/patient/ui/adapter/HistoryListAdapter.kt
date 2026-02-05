package com.mk.medtrust.patient.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.auth.data.model.toLocalDateTime
import com.mk.medtrust.databinding.HistoryAppointmentItemBinding
import com.mk.medtrust.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryListAdapterP(
    private val onAppointmentStart : (Appointment) -> Unit
) : RecyclerView.Adapter<HistoryListAdapterP.AppointmentViewHolder>(){

    private val appointments: MutableList<Appointment> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val itemBinding = HistoryAppointmentItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AppointmentViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val ap  = appointments[position]

        holder.itemBinding.apply {
            tvName.text = "Dr ${ap.doctorName}"
            val date  = ap.toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a",Locale.US)
            val dateText  = date.format(formatter)
            tvDateTime.text = dateText

            btnPrescription.setOnClickListener {
                onAppointmentStart(ap)
            }
        }
    }

    override fun getItemCount(): Int  = appointments.size

    fun updateNewList(newList : List<Appointment>){
        appointments.clear()
        appointments.addAll(newList)
        notifyDataSetChanged()
    }

    inner class AppointmentViewHolder(val itemBinding : HistoryAppointmentItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

}