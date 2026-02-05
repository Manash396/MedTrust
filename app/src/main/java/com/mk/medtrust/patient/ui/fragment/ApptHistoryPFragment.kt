package com.mk.medtrust.patient.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.auth.data.model.toLocalDateTime
import com.mk.medtrust.databinding.FragmentApptHistoryPBinding
import com.mk.medtrust.patient.ui.adapter.AppointmentAdapterP
import com.mk.medtrust.patient.ui.adapter.HistoryListAdapterP
import com.mk.medtrust.patient.ui.viewmodel.PatientSharedViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue

class ApptHistoryPFragment : Fragment() {

    private var _binding: FragmentApptHistoryPBinding? = null
    private val binding get() = _binding!!

    private val patientSharedViewModel: PatientSharedViewModel by activityViewModels()
    private lateinit var historyListSorted: MutableList<Appointment>
    private lateinit var historyListAdapter: HistoryListAdapterP

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApptHistoryPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortAppointmentList()
        setOnCLick()
    }

    private fun setOnCLick() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sortAppointmentList() {
        var list = patientSharedViewModel.historyAppointmentList

        list = list.sortedBy { appointment ->
            val dateParts = appointment.dateId.split("_")

            val date =
                LocalDate.of(dateParts[2].toInt(), dateParts[1].toInt(), dateParts[0].toInt())

            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
            val time = LocalTime.parse(appointment.slotTime, formatter)

            LocalDateTime.of(date, time)
        }.reversed()

        historyListSorted = list as MutableList<Appointment>
        bindData()
    }

    private fun bindData() {
        historyListAdapter = HistoryListAdapterP() { appointment ->
            val action =
                ApptHistoryPFragmentDirections.historyListPFragmentToAppointmentDetailPFragment(
                    appointment.appointmentId
                )
            findNavController().navigate(action)
        }

        binding.appointmentRecyclerView.adapter = historyListAdapter.apply {
            updateNewList(historyListSorted)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // VERY IMPORTANT to avoid memory leaks
    }
}
