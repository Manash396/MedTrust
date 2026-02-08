package com.mk.medtrust.doctor.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.databinding.FragmentHistoryDBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.ui.adapter.AppointmentAdapter
import com.mk.medtrust.doctor.ui.adapter.HistoryDAdapter
import com.mk.medtrust.doctor.ui.viewmodel.DoctorSharedViewModel
import com.mk.medtrust.patient.model.DateItem
import com.mk.medtrust.patient.ui.adapter.DateAdapter
import com.mk.medtrust.util.AvailabilityPrefs
import com.mk.medtrust.util.Result
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.getValue

class HistoryDFragment : Fragment() {

    private var _binding: FragmentHistoryDBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: DoctorSharedViewModel by activityViewModels()
    private val appointmentMapByDate: MutableMap<String, MutableList<Appointment>> = mutableMapOf()
    private lateinit var appointmentAdapter: HistoryDAdapter
    private lateinit var dateAdapter: DateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpOnclick()
        observeResponse()
    }

    private fun observeResponse() {
        sharedViewModel.appointments.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.mk.medtrust.util.Result.Error<*> -> {

                }

                com.mk.medtrust.util.Result.Loading -> {

                }

                is Result.Success -> {
//                    Log.d("KrishnaMK", result.data.toString())
                    sharedViewModel.updateList(result.data)
//                    Log.d("KrishnaMK", sharedViewModel.historyAppointmentList.toString())
                    bindData()
                }
            }
        }
    }

    private fun bindData() {
        appointmentMapByDate.clear()
        appointmentMapByDate.putAll(appointmentMapFunction(sharedViewModel.historyAppointmentList))
        binding.apply {

            val datesAvailable = generateDates()
            dateAdapter = DateAdapter() { dateItem ->

                val dateId = String.format(Locale.ENGLISH,"%02d_%02d_%04d", dateItem.dayOfMonth, dateItem.monthOfYear, dateItem.year)
                val newList = appointmentMapByDate[dateId] ?: emptyList()

//                Log.d("KrishnaMK", "$appointmentMapByDate \n$dateId \n$newList")

                noAppointmentFound.visibility = if (newList.isEmpty()) View.VISIBLE else View.GONE
                appointmentAdapter.updateNewList(newList)
//                findNavController().navigate(R.id.appointmentListCurFragment_to_appointmentDetailOnFragment)
            }

            dateRecyclerView.adapter = dateAdapter.apply {
                updateNewList(datesAvailable)
            }

            appointmentAdapter = HistoryDAdapter() { appointment ->
                val action =
                    HistoryDFragmentDirections.historyDFragmentToAppointmentDetailOnFragment(
                        appointment
                    )
                findNavController().navigate(action)
            }
            appointmentRecyclerView.adapter = appointmentAdapter
        }
    }

    // since this fragment about history so no need for availability check
    private fun generateDates(): List<DateItem> {
        val list = mutableListOf<DateItem>()
        val formatter  = DateTimeFormatter.ofPattern("dd_MM_yyyy")

        appointmentMapByDate.keys.forEach { id ->
            val date = LocalDate.parse(id,formatter)
            list.add(DateItem(
               dayOfWeek = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
               dayOfMonth = date.dayOfMonth,
                monthOfYear = date.monthValue,
                year = date.year,
                isAvailable = true
            ))
        }

        Log.d("Krishna",list.toString())
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

//        Log.d("KrishnaMK", sharedViewModel.historyAppointmentList.toString())
        return map
    }

    private fun setUpOnclick() {
        binding.apply {
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // VERY important to avoid memory leaks
    }
}
