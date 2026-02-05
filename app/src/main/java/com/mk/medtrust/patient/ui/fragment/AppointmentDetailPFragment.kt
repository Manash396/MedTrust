package com.mk.medtrust.patient.ui.fragment

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.auth.data.model.toLocalDateTime
import com.mk.medtrust.databinding.FragmentAppointmentDetailPBinding
import com.mk.medtrust.patient.ui.PatientCallActivity
import com.mk.medtrust.patient.ui.viewmodel.ApptDetailPViewModel
import com.mk.medtrust.patient.ui.viewmodel.PatientSharedViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.AppointmentStatus
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppointmentDetailPFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailPBinding? = null
    private val binding get() = _binding!!

    private val args: AppointmentDetailPFragmentArgs by navArgs()
    private val viewModel: ApptDetailPViewModel by viewModels()
    private val patientSharedViewModel: PatientSharedViewModel by activityViewModels()


    // this run first then onCreateView
    private val patientCallActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode == RESULT_OK){
                val callEnded  =  result.data?.getBooleanExtra("Call_Ended",false)
                val appt = viewModel.appointmentDetail.value
                if (callEnded == true){
                   if (appt is Result.Success){
                       val data  =  appt.data
                       viewModel.markCompleteAppointment(data.doctorId,data.dateId,data.appointmentId)
                       // this hold the appointment so load all current updated list
                       patientSharedViewModel.loadAppointments(AppPreferences.getString(AppConstant.UID))
                   }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentDetailPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setOnClick()
        observeResponse()
        val apptId = args.appointmentId
       viewModel.getAppointmentDetail(args.appointmentId)
    }

    private fun observeResponse() {
        viewModel.appointmentDetail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    showOkDialog(result.message)
                }

                Result.Loading -> {
                    binding.apply {
                        contentView.visibility = View.GONE
                        shimmerLoadingSkeleton.visibility = View.VISIBLE
                    }
                }

                is Result.Success -> {
                    val data = result.data
                    restoreUi(result.data)
                    viewModel.getDoctorDetail(data.doctorId)
                }
            }
        }

        viewModel.doctorDetail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    showOkDialog(result.message)
                }

                Result.Loading -> {
                    binding.apply {
                        contentView.visibility = View.GONE
                        shimmerLoadingSkeleton.visibility = View.VISIBLE
                    }
                }

                is Result.Success -> {
                    val doctor = result.data
                    binding.apply {
                        nameDoctor.text = doctor.name
                        specialist.text = doctor.specialisation
                        hospitalName.text = doctor.hospital
                        contentView.visibility = View.VISIBLE
                        shimmerLoadingSkeleton.visibility = View.GONE
                    }

                }
            }
        }
        viewModel.apptUpdateStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    showOkDialog(result.message)
                }

                Result.Loading -> {

                }

                is Result.Success -> {
                   viewModel.getAppointmentDetail(args.appointmentId)
                }
            }
        }

        // for shared appointment list
        patientSharedViewModel.appointments.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {

                    showOkDialog(result.message)
                }

                is Result.Loading -> {

                }

                is Result.Success -> {
                    patientSharedViewModel.updateList(result.data)
                }
            }
        }

    }

    private fun restoreUi(data: Appointment) {
        binding.apply {
            status.text = data.status.toString()
            if (data.status == AppointmentStatus.COMPLETED) {
                incompleteIcon.visibility = View.INVISIBLE
                completeIcon.visibility = View.VISIBLE

                greenDotCompletedAppt.visibility = View.VISIBLE
                orangeDotUpcomingAppt.visibility = View.INVISIBLE

                btnPrescriptionDownload.visibility = View.VISIBLE
                btnVideoCall.visibility = View.GONE
            } else {
                incompleteIcon.visibility = View.VISIBLE
                completeIcon.visibility = View.INVISIBLE

                greenDotCompletedAppt.visibility = View.INVISIBLE
                orangeDotUpcomingAppt.visibility = View.VISIBLE

                btnPrescriptionDownload.visibility = View.GONE
                btnVideoCall.visibility = View.VISIBLE
            }

            timeSlot.text = data.slotTime
            noteDoctor.text = data.prescription?.notes ?: "No notes provided yet."

            btnPrescriptionDownload.alpha = if(data.prescription == null) 0.3f else 1f

            val date = data.toLocalDateTime()
            val dateString = "${date.month} ${date.dayOfMonth}, ${date.year}"
            apptDate.text = dateString
        }
    }

    private fun setOnClick() {
        binding.apply {
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
            btnVideoCall.setOnClickListener {
                val intent = Intent(requireContext(), PatientCallActivity::class.java).apply {
                    putExtra("callId", args.appointmentId)
                }
                patientCallActivityLauncher.launch(intent)
            }

        }
    }

    private fun showOkDialog(message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
                viewModel.getAppointmentDetail(args.appointmentId)
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
        _binding = null // 🔥 prevents memory leak
    }
}


