package com.mk.medtrust.doctor.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mk.medtrust.R
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.databinding.FragmentAppointmentListCurBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.ui.adapter.AppointmentAdapter
import com.mk.medtrust.doctor.ui.viewmodel.DoctorSharedViewModel
import com.mk.medtrust.patient.model.DateItem
import com.mk.medtrust.patient.ui.adapter.DateAdapter
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.AvailabilityPrefs
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.map


@AndroidEntryPoint
class AppointmentListCurFragment : Fragment() {

    private var _binding: FragmentAppointmentListCurBinding? = null
    private val binding get() = _binding!!
    private lateinit var dateAdapter: DateAdapter
    private lateinit var appointmentAdapter: AppointmentAdapter
    private val appointmentMapByDate: MutableMap<String, MutableList<Appointment>> = mutableMapOf()
    private val sharedViewModel: DoctorSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentListCurBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindData()
        setUpOnclick()
    }

    private fun setUpOnclick() {
        binding.apply {
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }


    }

    private fun bindData() {
        appointmentMapByDate.clear()
        appointmentMapByDate.putAll(appointmentMapFunction(sharedViewModel.ongoingAppointmentList))
        binding.apply {
            val todayDateId = with(LocalDate.now()) {
                "${dayOfMonth}_${monthValue}_${year}"
            }

            todaysApp.text = appointmentMapByDate[todayDateId]?.size?.toString() ?: "0"

            val datesAvailable = generateDates(AvailabilityPrefs.getDoctorAvailability())
            dateAdapter = DateAdapter() { dateItem ->

                val dateId = "${dateItem.dayOfMonth}_${dateItem.monthOfYear}_${dateItem.year}"
                val newList = appointmentMapByDate[dateId] ?: emptyList()
                appointmentAdapter.updateNewList(newList)
            }
            dateRecyclerView.adapter = dateAdapter.apply {
                updateNewList(datesAvailable as MutableList<DateItem>)
            }

            appointmentAdapter = AppointmentAdapter() {}
            appointmentRecyclerView.adapter = appointmentAdapter

        }

    }

    private fun generateDates(
        availability: Doctor.Availability,
        daysAhead: Int = 10
    ): List<DateItem> {
        val today = LocalDate.now()
        val list = mutableListOf<DateItem>()
        for (i in 0 until daysAhead) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.name.take(3) // "MON", "TUE"
            val monthOfYear = date.monthValue
            val year = date.year
            val isAvailable = availability.days.any {
                it.equals(dayOfWeek, ignoreCase = true)
            }
            list.add(DateItem(dayOfWeek, date.dayOfMonth, monthOfYear, year, isAvailable))
        }
//        Log.d("Krishna",list.toString())
        return list
    }

    private fun appointmentMapFunction(list: List<Appointment>): MutableMap<String, MutableList<Appointment>> {
        val map = mutableMapOf<String, MutableList<Appointment>>()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

        list
            .sortedBy {
                LocalTime.parse(it.slotTime, formatter)
            }
            .forEach {
                val appointmentsForDate = map.getOrPut(it.dateId) { mutableListOf() }
                appointmentsForDate.add(it)
            }

        return map
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}