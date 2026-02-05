    package com.mk.medtrust.doctor.ui.fragment

    import android.app.AlertDialog
    import android.content.Intent
    import android.os.Bundle
    import android.view.Gravity
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.LinearLayout
    import android.widget.Toast
    import androidx.fragment.app.viewModels
    import androidx.navigation.fragment.findNavController
    import androidx.navigation.fragment.navArgs
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.mk.medtrust.R
    import com.mk.medtrust.auth.data.model.Appointment
    import com.mk.medtrust.auth.data.model.Prescription
    import com.mk.medtrust.databinding.FragmentAppointmentDetailOnBinding
    import com.mk.medtrust.databinding.ItemMedicineBinding
    import com.mk.medtrust.doctor.ui.viewmodel.ApptDetailDViewModel
    import com.mk.medtrust.patient.model.Patient
    import com.mk.medtrust.patient.ui.PatientActivity
    import com.mk.medtrust.patient.ui.PatientCallActivity
    import com.mk.medtrust.util.AppConstant
    import com.mk.medtrust.util.AppointmentStatus
    import com.mk.medtrust.util.Result
    import com.yourpackage.app.AppPreferences
    import dagger.hilt.android.AndroidEntryPoint
    import java.time.LocalDate
    import java.time.LocalDateTime
    import java.time.Period
    import java.time.format.DateTimeFormatter
    import java.util.Locale

    @AndroidEntryPoint
    class AppointmentDetailOnFragment : Fragment() {

        private var _binding : FragmentAppointmentDetailOnBinding? = null
        private val binding get() = _binding!!
        private val args : AppointmentDetailOnFragmentArgs by navArgs()
        private val viewModel : ApptDetailDViewModel by viewModels()
        private lateinit var appointmentCurrent  : Appointment

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentAppointmentDetailOnBinding.inflate(inflater,container,false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            setOnCLick()
            observeResponse()
            viewModel.getAppointmentDetail(args.appointment)
        }

        private fun observeResponse() {
            viewModel.appointmentDetailStatus.observe(viewLifecycleOwner){result ->
                when(result){
                    is Result.Success -> {
                        appointmentCurrent = result.data
                        viewModel.getPatientDetail(appointmentCurrent.patientId)
                    }
                    is Result.Error -> {
                        showOkDialog(result.message)
                    }
                    is Result.Loading -> {
                        binding.apply {
                            shimmerLoadingSkeleton.visibility = View.VISIBLE
                            contentView.visibility = View.INVISIBLE
                        }
                    }
                }
            }

            viewModel.patientDetailStatus.observe(viewLifecycleOwner){result ->
                when(result){
                    is Result.Success -> {
                        binding.apply {
                            shimmerLoadingSkeleton.visibility = View.INVISIBLE
                            contentView.visibility = View.VISIBLE
                        }
                        bindData(result.data)
                    }
                    is Result.Error -> {
                        showOkDialog(result.message)
                    }
                    is Result.Loading -> {

                    }
                }
            }
        }

        private fun bindData(patient : Patient){
            binding.apply {
                namePatient.text = patient.name
                gender.text = patient.gender
                val today  = LocalDate.now()
                val formatter  = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US)
                val dob  = LocalDate.parse(patient.dob,formatter)
                val age = Period.between(dob,today).years.toString()
                Age.text = ", $age years old"

                // btn
                if (appointmentCurrent.status != AppointmentStatus.COMPLETED) {
                    btnVideoCall.visibility = View.VISIBLE
                    btnCompletedAppointment.visibility = View.GONE
                    btnPrescriptionSubmit.apply {
                        isEnabled = false
                        alpha = 0.3f
                    }
                }
                else {
                    btnVideoCall.visibility = View.GONE
                    btnCompletedAppointment.visibility = View.VISIBLE
                    btnPrescriptionSubmit.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                }

                if (appointmentCurrent.prescription != null){
                    btnPrescriptionSubmit.text = "Update Prescription"
                    restorePrescription(appointmentCurrent.prescription)
                }

            }
        }

        private fun restorePrescription(pres: Prescription?) {
            if (pres == null) return

            val medList  = pres.medicines
            binding.apply {
                val note  = pres.notes
                doctorNotesEdit.setText(note)

                medList.forEach { medicine ->
                    val card = ItemMedicineBinding.inflate(layoutInflater,binding.medicineContainer,false)
                    card.apply {
                        btnDelete.setOnClickListener {
                            medicineContainer.removeView(card.root)
                        }
                        tvMedicineName.text = medicine.name
                        tvDose.text  = medicine.dose
                        tvFreg.text =  medicine.frequency
                    }

                    medicineContainer.addView(card.root)
                }
            }
        }

        private  fun setOnCLick(){
            binding.apply {
                backBtn.setOnClickListener {
                    findNavController().popBackStack()
                }
                btnAddMedicine.setOnClickListener {
                    addMedicineToListContainer()
                }
                btnPrescriptionSubmit.setOnClickListener {
                    Toast.makeText(requireContext(),"working",Toast.LENGTH_SHORT).show()
                }
                btnVideoCall.setOnClickListener {
                    val intent  = Intent(requireContext(), PatientCallActivity::class.java).apply {
                        putExtra("callId", args.appointment.appointmentId)
                    }
                    startActivity(intent)
                }
            }
        }

        private fun addMedicineToListContainer(){
            val name  = binding.medicineName.text.toString()
            val does  = binding.medicineDose.text.toString()
            val freq =  binding.frequency.text.toString()

            if (name.isEmpty() || does.isEmpty() || freq.isEmpty()){
                Toast.makeText(requireContext(),"All Field required", Toast.LENGTH_SHORT).show()
                return
            }

            val cardMedicine = ItemMedicineBinding.inflate(layoutInflater, binding.medicineContainer,false)

            cardMedicine.apply {
                tvMedicineName.text = name
                tvDose.text =  does
                tvFreg.text  =  freq

                btnDelete.setOnClickListener {
                    binding.medicineContainer.removeView(cardMedicine.root)
                }
            }

            binding.medicineContainer.addView(cardMedicine.root)

        }
        private fun showOkDialog(message: String) {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                    findNavController().popBackStack()
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
            _binding = null
        }

    }

