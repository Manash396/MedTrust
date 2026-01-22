package com.mk.medtrust.patient.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mk.medtrust.databinding.ItemDoctorBinding
import com.mk.medtrust.doctor.model.Doctor
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class DoctorListAdapter(
    private val callback: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorListAdapter.DoctorViewHolder>() {

    private val doctors = mutableListOf<Doctor>()

    // ---------- VIEW HOLDER ----------
    inner class DoctorViewHolder(
        private val binding: ItemDoctorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(doctor: Doctor) {
            binding.apply {
                tvDoctorName.text ="Dr ${doctor.name}"
                tvSpecialization.text = doctor.specialisation

                tvRating.text = doctor.rating

                val todayDay = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
                tvAvailability.text = if (doctor.availability.days.contains(todayDay)) "Available Today"
                else "Not Available Today"

                root.setOnClickListener {
                    callback(doctor)
                }
            }
        }
    }

    // ---------- ADAPTER METHODS ----------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount(): Int = doctors.size

    // ---------- UPDATE LIST ----------
    fun updateList(newList: List<Doctor>) {
        doctors.clear()
        doctors.addAll(newList)
        notifyDataSetChanged()
    }
}
