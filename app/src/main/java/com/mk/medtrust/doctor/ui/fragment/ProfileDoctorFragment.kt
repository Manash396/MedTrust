package com.mk.medtrust.doctor.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mk.medtrust.R
import com.mk.medtrust.auth.LoginActivity
import com.mk.medtrust.databinding.FragmentProfileDoctorBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.ui.viewmodel.DoctorViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.AvailabilityPrefs
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileDoctorFragment : Fragment() {

    // Backing property to avoid memory leaks
    private var _binding: FragmentProfileDoctorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoctorViewModel by activityViewModels()

    private var doctorUpdatedC: Doctor? = null
    private var availabilityUpdatedC: Doctor.Availability? = null
    private var isDoctorPDetailEditing = false
    private var isDoctorAvailEditing = false
    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireContext()

        setUpOnClick()
        bindDoctorProfile()
        observeResponse()
    }


    private fun observeResponse() {

        viewModel.logoutState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.mk.medtrust.util.Result.Error<*> -> {
                    showOkDialog(result.message)
                }

                com.mk.medtrust.util.Result.Loading -> {

                }

                is Result.Success -> {
                    requireActivity().finish()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                }
            }
        }

        viewModel.profileUpdateState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    binding.progressBarProf.visibility = View.GONE

                    binding.tvExperience.visibility = View.VISIBLE
                    binding.btnEditProfessional.visibility = View.VISIBLE
                    binding.tvConsultationFee.visibility = View.VISIBLE

                    doctorUpdatedC?.let {
                        AppPreferences.setString(AppConstant.EXP, it.experience)
                        AppPreferences.setString(AppConstant.FEE, it.consultationFee)
                        bindDoctorProfile()
                    }
                    doctorUpdatedC = null
                }

                is Result.Loading -> {
                    binding.progressBarProf.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    binding.progressBarProf.visibility = View.GONE

                    binding.tvExperience.visibility = View.VISIBLE
                    binding.btnEditProfessional.visibility = View.VISIBLE
                    binding.tvConsultationFee.visibility = View.VISIBLE

                    showOkDialog(result.message)
                    doctorUpdatedC = null
                }
            }
        }

        viewModel.availabilityUpdateState.observe(viewLifecycleOwner){result ->
            when(result){
                is Result.Success ->{
                    binding.progressBarAvail.visibility = View.GONE
                    binding.availableDaysTv.visibility = View.VISIBLE
                    binding.btnEditAvailability.visibility = View.VISIBLE

                    // saving in preferences
                    availabilityUpdatedC?.let {
                        AvailabilityPrefs.saveDoctorAvailability(it)
                        bindDoctorProfile()
                    }
                    availabilityUpdatedC = null
                }
                is Result.Loading -> {
                    binding.progressBarAvail.visibility = View.VISIBLE
                }
                is Result.Error -> {
                    binding.progressBarAvail.visibility = View.GONE

                    binding.availableDaysTv.visibility = View.VISIBLE
                    binding.btnEditAvailability.visibility = View.VISIBLE
                    availabilityUpdatedC = null
                }
            }
        }


    }

    private fun setUpOnClick() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnLogout.setOnClickListener {
            logOut()
        }
        binding.layoutSaveProfess.setOnClickListener {
            updatingProfessionalDetail()
        }
        binding.layoutSaveAvailability.setOnClickListener {
            updatingAvailabilityDetail()
        }
        binding.btnCancelProfessional.setOnClickListener {
            isDoctorPDetailEditing = false
            it.visibility = View.GONE

            binding.tvExperience.visibility = View.VISIBLE
            binding.btnEditProfessional.visibility = View.VISIBLE
            binding.tvConsultationFee.visibility = View.VISIBLE
            binding.btnSaveProfessional.visibility = View.GONE
            binding.etExperience.visibility = View.GONE
            binding.etConsultationFee.visibility = View.GONE
        }
        binding.btnCancelAvailability.setOnClickListener {
            with(binding) {

                availableDaysTv.visibility = View.VISIBLE
                btnCancelAvailability.visibility = View.GONE
                btnEditAvailability.visibility = View.VISIBLE

                chipDayGroup.visibility = View.GONE
                btnSaveAvailability.visibility = View.GONE

                tvStartTime.apply {
                    background = null
                    isClickable = false
                    isFocusable = false
                }

                tvEndTime.apply {
                    background = null
                    isClickable = false
                    isFocusable = false
                }
            }
            isDoctorAvailEditing = false
        }

        // time picker will be called from here
        binding.tvStartTime.setOnClickListener {
            if (!isDoctorAvailEditing) return@setOnClickListener
            showGreenTimePicker { time ->
                binding.tvStartTime.text = time
            }
        }
        binding.tvEndTime.setOnClickListener {
            if (!isDoctorAvailEditing) return@setOnClickListener
            showGreenTimePicker { time ->
                binding.tvEndTime.text = time
            }
        }
    }

    private fun updatingAvailabilityDetail() {
        if (!isDoctorAvailEditing) {
            with(binding) {
                btnEditAvailability.visibility = View.GONE
                availableDaysTv.visibility = View.GONE

                // check the chips for available days
                editModeChipDays(binding.availableDaysTv.text.toString())

                chipDayGroup.visibility = View.VISIBLE
                btnCancelAvailability.visibility = View.VISIBLE
                btnSaveAvailability.visibility = View.VISIBLE

                tvStartTime.apply {
                    background = ContextCompat.getDrawable(context, R.drawable.green_round_stroke)
                    isClickable = true
                    isFocusable = true
                }

                tvEndTime.apply {
                    background = ContextCompat.getDrawable(context, R.drawable.green_round_stroke)
                    isClickable = true
                    isFocusable = true
                }

            }

            isDoctorAvailEditing = true
        } else {
            with(binding) {
          //      saving will be done here

                val list = getListOfDays(binding.chipDayGroup)
                val startTime = binding.tvStartTime.text.toString().trim()
                val endTime  = binding.tvEndTime.text.toString().trim()

                if (startTime.isEmpty() || endTime.isEmpty()) {
                    showOkDialog("Please select both start and end time")
                    return
                }

                val startMinutes = UtilObject.timeToMinutes(startTime)
                val endMinutes = UtilObject.timeToMinutes(endTime)

                if (endMinutes <= startMinutes) {
                    showOkDialog("End time must be after start time")
                    return
                }

                if (endMinutes - startMinutes < 30) {
                    showOkDialog("Time difference must be at least 30 minutes")
                    return
                }


                val availability = Doctor.Availability(
                    list ,
                    startTime,
                    endTime
                )
                availabilityUpdatedC = availability
                updateDoctorAvailDetails(availability)

                btnCancelAvailability.visibility = View.GONE
                chipDayGroup.visibility = View.GONE
                btnSaveAvailability.visibility = View.GONE

                tvStartTime.apply {
                    background = null
                    isClickable = false
                    isFocusable = false
                }

                tvEndTime.apply {
                    background = null
                    isClickable = false
                    isFocusable = false
                }
            }
            isDoctorAvailEditing = false
        }
    }

    private fun updateDoctorAvailDetails(av : Doctor.Availability){
        viewModel.updateDoctorAvailability(av)
    }

    private fun editModeChipDays(list : String){
        val selectedDays = parseDays(list)

        for(i in 0 until binding.chipDayGroup.childCount){
            val chip  = binding.chipDayGroup.getChildAt(i) as Chip
            chip.isChecked = selectedDays.contains(chip.text.toString())
        }
    }

    fun parseDays(daysText: String): List<String> {
        return daysText
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim() }
    }

    private fun getListOfDays(chipGroup: ChipGroup): List<String> {
        val selectedDays = mutableListOf<String>()

        for (chipId in chipGroup.checkedChipIds) {
            val chip = chipGroup.findViewById<Chip>(chipId)
            selectedDays.add(chip.text.toString())
        }

        return selectedDays
    }


    private fun updatingProfessionalDetail() {
        if (!isDoctorPDetailEditing) {
            binding.btnEditProfessional.visibility = View.GONE
            binding.btnSaveProfessional.visibility = View.VISIBLE

            binding.tvExperience.visibility = View.GONE
            binding.etExperience.visibility = View.VISIBLE

            binding.tvConsultationFee.visibility = View.GONE
            binding.etConsultationFee.visibility = View.VISIBLE
            binding.btnCancelProfessional.visibility = View.VISIBLE

            with(binding) {
                etExperience.setText(tvExperience.text.toString().replace(
                    Regex("[^0-9]") ,""
                ))
                etConsultationFee.setText(
                    tvConsultationFee.text.toString().replace(Regex("[^0-9]"), "")
                )
            }

            isDoctorPDetailEditing = true
        } else {
            // editing i s going on clicking to save the edit
            val exp = binding.etExperience.text.toString().trim() + " yrs"
            val consultation = binding.etConsultationFee.text.toString().trim()

            updateDoctorProfDetails(exp, consultation)


            binding.btnSaveProfessional.visibility = View.GONE
            binding.btnCancelProfessional.visibility = View.GONE

            binding.etExperience.visibility = View.GONE

            binding.etConsultationFee.visibility = View.GONE


            isDoctorPDetailEditing = false
        }
    }


    private fun updateDoctorProfDetails(exp: String, consFee: String) {
        val doctor = Doctor(
            experience = exp,
            consultationFee = "₹$consFee"
        )
        doctorUpdatedC = doctor
        viewModel.updateDoctorProfDetails(doctor)
    }

    private fun bindDoctorProfile() {

        // Header
        binding.apply {
            // Name
            tvName.text =
                "Dr. ${AppPreferences.getString(AppConstant.USERNAME)}"

            // Email
            tvEmail.text =
                AppPreferences.getString(AppConstant.EMAIL)

            // Phone
            tvPhone.text =
                AppPreferences.getString(AppConstant.MOBILE)

            // Specialization
            tvSpecialization.text =
                AppPreferences.getString(AppConstant.SPEC)
            specId.text = AppPreferences.getString(AppConstant.SPEC)

            // Experience
            tvExperience.text =
                AppPreferences.getString(AppConstant.EXP)

            // License
            tvLicense.text =
                AppPreferences.getString(AppConstant.LISC)

            // Consultation Fee
            binding.tvConsultationFee.text = AppPreferences.getString(AppConstant.FEE)

            // hospital name
            binding.tvHospital.text = AppPreferences.getString(AppConstant.HOSPITAL)

            val av = AvailabilityPrefs.getDoctorAvailability()
            binding.availableDaysTv.text = av.days.toString()
            binding.tvStartTime.text = av.startTime
            binding.tvEndTime.text = av.endTime
        }
    }

    private fun logOut() {
        viewModel.logOut()
    }

    private fun showOkDialog(message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
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

    private fun showGreenTimePicker(onTimeSelected: (String) -> Unit) {

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTheme(R.style.GreenTimePickerTheme)
            .setHour(10)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val time = formatTime(picker.hour, picker.minute)
            onTimeSelected(time)
        }

        picker.show(parentFragmentManager, "GREEN_TIME_PICKER")
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return String.format("%02d:%02d %s", hour12, minute, amPm)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


