package com.mk.medtrust.patient.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.Grid
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mk.medtrust.databinding.FragmentDoctorDetailsBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.model.DateItem
import com.mk.medtrust.patient.model.SlotItem
import com.mk.medtrust.patient.ui.adapter.DateAdapter
import com.mk.medtrust.patient.ui.adapter.SlotAdapter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.exp

class DoctorDetailsFragment : Fragment() {

    private var _binding: FragmentDoctorDetailsBinding? = null
    private val binding get() = _binding!!

    private val args : DoctorDetailsFragmentArgs by navArgs()
    private lateinit var dateAdapter : DateAdapter
    private lateinit var slotAdapter : SlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnclickListener()

        bindingData(args.doctor)
    }

    private fun bindingData(doctor: Doctor) {
        with(binding){
            name.text = "Dr ${doctor.name}"
            specialist.text = doctor.specialisation
            hospitalName.text = doctor.hospital
            experience.text = "${doctor.experience} \nExperience"
            rating.text = "${doctor.rating}\nRating"
            description.text = "Dr. ${doctor.name} is a highly experienced ${doctor.specialisation} with" +
                    " over ${doctor.experience} of expertise."
            fee.text = doctor.consultationFee
        }
        // for slot
        slotAdapter = SlotAdapter(){}
        binding.slotRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        binding.slotRecyclerView.adapter = slotAdapter

        // for dates
        val datesAvailable = generateDates(doctor.availability)
        dateAdapter = DateAdapter(){ dateItem ->
            // to access on going appointments on that day from firestore
            val date  = "${dateItem.dayOfMonth}_${dateItem.monthOfYear}_${dateItem.year}"
            Log.d("Krishna",date)
            val list = listOf("06:30 PM","07:00 PM")
            val slots = generateSlots(doctor.availability.startTime,doctor.availability.endTime,list)

            slotAdapter.updateNewList(slots)
        }
        binding.dateRecyclerView.adapter = dateAdapter.apply {
            updateNewList(datesAvailable as MutableList<DateItem>)
        }


    }

    private fun setUpOnclickListener() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun generateSlots(startTime: String, endTime: String , bookedSlots: List<String>): List<SlotItem> {
        val slotDuration  = 30
        val timeFormatter24 = DateTimeFormatter.ofPattern("HH:mm")
        val timeFormatter12 = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

        val start = LocalTime.parse(startTime, timeFormatter12)
        val end = LocalTime.parse(endTime, timeFormatter12)

        var current = start
        val slots = mutableListOf<SlotItem>()

        while (current.isBefore(end)) {
            val displayTime = current.format(timeFormatter12)

            val isAvailable = !bookedSlots.contains(displayTime)

            slots.add(
                SlotItem(
                    time = displayTime,
                    isAvailable = isAvailable
                )
            )

            current = current.plusMinutes(slotDuration.toLong())
        }

        return slots
    }

    private fun generateDates(availability: Doctor.Availability, daysAhead: Int = 10): List<DateItem> {
        val today = LocalDate.now()
        val list = mutableListOf<DateItem>()
        for (i in 0 until daysAhead) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.name.take(3) // "MON", "TUE"
            val monthOfYear = date.monthValue
            val year  = date.year
            val isAvailable = availability.days.any {
                it.equals(dayOfWeek, ignoreCase = true)
            }
            list.add(DateItem(dayOfWeek, date.dayOfMonth, monthOfYear,year,isAvailable))
        }
//        Log.d("Krishna",list.toString())
        return list
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leaks
    }
}
