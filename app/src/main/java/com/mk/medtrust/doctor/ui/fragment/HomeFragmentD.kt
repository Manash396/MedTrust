package com.mk.medtrust.doctor.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.databinding.FragmentHomeDBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.ui.viewmodel.DoctorSharedViewModel
import com.mk.medtrust.doctor.ui.viewmodel.DoctorViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.AvailabilityPrefs
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@AndroidEntryPoint
class HomeFragmentD : Fragment() {

    private var  _binding  : FragmentHomeDBinding? = null
    private val binding get() = _binding!!

    private val viewModel : DoctorViewModel by viewModels()
    private val appointmentMapByDate: MutableMap<String, MutableList<Appointment>> = mutableMapOf()

    private val sharedViewModel : DoctorSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvHello.text = "Dr. ${AppPreferences.getString(AppConstant.USERNAME)}"

        setUpOnClick()
        observeResponse()

        viewModel.getDoctorDetails(AppPreferences.getString(AppConstant.UID))
        sharedViewModel.loadAppointments(AppPreferences.getString(AppConstant.UID))
    }

    private fun bindData() {
        appointmentMapByDate.clear()
        appointmentMapByDate.putAll(appointmentMapFunction(sharedViewModel.ongoingAppointmentList))
        binding.apply {
            val todayDateId = with(LocalDate.now()) {
                "${dayOfMonth}_${monthValue}_${year}"
            }
            val list = appointmentMapByDate[todayDateId] ?: emptyList()
            pendingApp.text = "${list.size} pending"

        }
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

    private fun observeResponse() {
        viewModel.doctorDetailState.observe(viewLifecycleOwner){result ->
            when(result){
                is Result.Error<*> -> {
                    showOkDialog(result.message)
                }
                Result.Loading -> {

                }
                is Result.Success -> {
                   saveToPreferences(result.data)
                }
            }
        }
        sharedViewModel.appointments.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error<*> -> {
                    showOkDialog(result.message)
                }

                Result.Loading -> {

                }

                is Result.Success -> {
                    Log.d("KrishnaMK",result.data.toString())
                    sharedViewModel.updateList(result.data)
                    binding.appointmentListCurBtn.isEnabled =  true
                    bindData()
                }
            }
          }
        }



    private fun setUpOnClick() {
        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_doctorProfileFragment)
        }
        binding.profileSecondBtn.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_doctorProfileFragment)
        }
        binding.appointmentListCurBtn.setOnClickListener {
            if (!binding.appointmentListCurBtn.isEnabled) return@setOnClickListener
            findNavController().navigate(R.id.homeFragment_to_appointmentListCurFragment)
        }

        binding.appointmentListCurBtn.isEnabled = false
    }

    private fun saveToPreferences(doctor: Doctor){
        AppPreferences.setString(AppConstant.EXP,doctor.experience)
        AppPreferences.setString(AppConstant.RATE,doctor.rating)
        AppPreferences.setString(AppConstant.RATEN,doctor.rateNo)
        AppPreferences.setString(AppConstant.LISC,doctor.medicalLicenseNo)
        AppPreferences.setString(AppConstant.FEE,doctor.consultationFee)
        AppPreferences.setString(AppConstant.HOSPITAL,doctor.hospital)

        AvailabilityPrefs.saveDoctorAvailability(doctor.availability)
    }
    private fun showOkDialog(message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
                refetchDoctorDetails()
            }
            .create()

        dialog.show()

        // Center the OK button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER
            layoutParams = params
        }
    }

    private fun refetchDoctorDetails(){
        viewModel.getDoctorDetails(AppPreferences.getString(AppConstant.UID))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}