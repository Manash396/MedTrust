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
import com.mk.medtrust.databinding.FragmentAppointmentListPBinding
import com.mk.medtrust.patient.ui.adapter.AppointmentAdapterP
import com.mk.medtrust.patient.ui.viewmodel.PatientSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class AppointmentListPFragment : Fragment() {

    // Backing property
    private var _binding: FragmentAppointmentListPBinding? = null

    private val patientSharedViewModel: PatientSharedViewModel by activityViewModels()

    // This is safe to use between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private lateinit var appointmentListAdapter: AppointmentAdapterP
    private  var appointmentListSorted = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentListPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnclick()
        bindData()
        sortAppointmentList()
    }

    private fun sortAppointmentList() {
        var list = patientSharedViewModel.ongoingAppointmentList

        list = list.sortedBy { appointment ->
            val dateParts = appointment.dateId.split("_")

            val date =
                LocalDate.of(dateParts[2].toInt(), dateParts[1].toInt(), dateParts[0].toInt())

            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
            val time = LocalTime.parse(appointment.slotTime, formatter)

             LocalDateTime.of(date, time)
        }

//        appointmentListSorted = list as MutableList<Appointment>   unsafe cast
        appointmentListSorted.clear() // it was alive since fragment instance was alive this does not belong to view's
        appointmentListSorted.addAll(list)
        appointmentListAdapter.updateNewList(appointmentListSorted)
    }

    private fun bindData() {
        appointmentListAdapter = AppointmentAdapterP { appointment ->
            val appointmentTime = appointment.toLocalDateTime()
            val now  = LocalDateTime.now()
            val isWithin26minCurr =  now.isAfter(appointmentTime.minusMinutes(1)) && now.isBefore(appointmentTime.plusMinutes(25))
            if (!isWithin26minCurr){
                sortAppointmentList()
                return@AppointmentAdapterP
            }

            val action = AppointmentListPFragmentDirections.appointmentListPFragmentToAppointmentDetailPFragment(appointment.appointmentId)
            findNavController().navigate(action)
        }
        binding.appointmentRecyclerView.adapter = appointmentListAdapter
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
        _binding = null
    }
}
