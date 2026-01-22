package com.mk.medtrust.doctor.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.auth.data.model.toLocalDateTime
import com.mk.medtrust.databinding.ItemAppointmentBinding
import com.mk.medtrust.patient.model.DateItem
import java.time.LocalDateTime

class AppointmentAdapter(
    private val onAppointmentStart : (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>(){

    private val appointments: MutableList<Appointment> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val itemBinding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AppointmentViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val ap  = appointments[position]

        holder.itemBinding.apply {
            tvName.text = ap.patientName
            tvTime.text = ap.slotTime

            val now = LocalDateTime.now()
            val appointmentTime = ap.toLocalDateTime()

            val isWithin30Minutes =
                now.isAfter(appointmentTime.minusMinutes(5)) &&
                        now.isBefore(appointmentTime.plusMinutes(25))

            tvStatus.visibility = if (isWithin30Minutes) View.VISIBLE else View.GONE
            val isWithin26min =  now.isAfter(appointmentTime.minusMinutes(1)) && now.isBefore(appointmentTime.plusMinutes(25))
            btnStart.apply {
                isEnabled = isWithin26min
                alpha  = if (isWithin26min) 1f else 0.3f
            }
        }
    }

    override fun getItemCount(): Int  = appointments.size

    fun updateNewList(newList : List<Appointment>){
        appointments.clear()
        appointments.addAll(newList)
        notifyDataSetChanged()
    }

    inner class AppointmentViewHolder(val itemBinding : ItemAppointmentBinding) : RecyclerView.ViewHolder(itemBinding.root)

}