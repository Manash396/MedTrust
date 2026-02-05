package com.mk.medtrust.patient.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.databinding.FragmentHomeBinding
import com.mk.medtrust.patient.ui.viewmodel.PatientSharedViewModel
import com.mk.medtrust.patient.ui.viewmodel.PatientViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class HomeFragmentP : Fragment() {

    // Backing property
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!
    private val viewModelP: PatientViewModel by viewModels()
    private val patientSharedViewModel: PatientSharedViewModel by activityViewModels()
    private var isAppointmentFetched: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvHello.text = "Hello, ${AppPreferences.getString("userName")}"

        setUpOnclick()
        observeResponse()
        patientSharedViewModel.loadAppointments(AppPreferences.getString(AppConstant.UID))
    }

    private fun observeResponse() {
        patientSharedViewModel.appointments.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    isAppointmentFetched = false
                    showOkDialog(result.message)
                }

                is Result.Loading -> {
                    isAppointmentFetched = false
                }

                is Result.Success -> {
                    isAppointmentFetched = true
                    patientSharedViewModel.updateList(result.data)
                    checkForOngoingAppointments()
                }
            }
        }
    }

    private fun setUpOnclick() {
        binding.bookConsultation.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_doctorListFragment)
        }
        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_patientProfile)
        }
        binding.ongoingAppointmentBtn.setOnClickListener {
            if (!isAppointmentFetched) return@setOnClickListener
            findNavController().navigate(R.id.homeFragment_to_appointmentListPFragment)
        }
        binding.historyApptBtn.setOnClickListener {
            if (!isAppointmentFetched) return@setOnClickListener
            findNavController().navigate(R.id.homeFragment_to_historyListPFragment)
        }
    }

    private fun checkForOngoingAppointments(){
        val ongoing = patientSharedViewModel.ongoingAppointmentList
        if (ongoing.isEmpty()) return
        binding.greenDotOngoingApmt.visibility = View.VISIBLE
    }

    private fun showOkDialog(message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
                patientSharedViewModel.loadAppointments(AppPreferences.getString(AppConstant.UID))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // prevent memory leaks
    }
}
