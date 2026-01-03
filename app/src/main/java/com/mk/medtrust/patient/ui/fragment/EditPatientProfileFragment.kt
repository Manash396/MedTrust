package com.mk.medtrust.patient.ui.fragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.databinding.FragmentEditPatientProfileBinding
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.patient.ui.viewmodel.PatientViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditPatientProfileFragment : Fragment() {

    // ViewBinding reference
    private var _binding: FragmentEditPatientProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientViewModel by viewModels()
    private var patientGlobal : Patient? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPatientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnclickListener()
        observeResponse()
        restoreUI()
    }

    private fun observeResponse() {
        viewModel.updateState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    showOkDialog(result.message)
                }

                Result.Loading -> {
                    showLoader()
                }
                is Result.Success -> {
                    parentFragmentManager.setFragmentResult(
                        "profile_update_result",
                        Bundle().apply {
                            putBoolean("success",true)
                        }
                    )
                    saveToPreferences()
                    hideLoader()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun restoreUI() {
        with(binding) {
            nameInput.setText(AppPreferences.getString(AppConstant.USERNAME))
            emailInput.setText(AppPreferences.getString(AppConstant.EMAIL))
            numberInput.setText(AppPreferences.getString(AppConstant.MOBILE))
            when (AppPreferences.getString(AppConstant.ROLE)) {
                "Doctor" -> rbDoctor.isChecked = true
                "Patient" -> rbPatient.isChecked = true
            }
            when (AppPreferences.getString(AppConstant.GENDER)) {
                "Male" -> rbMale.isChecked = true
                "Female" -> rbFemale.isChecked = true
            }
            dobInput.setText(AppPreferences.getString(AppConstant.DOB))
        }
    }

    private fun saveToPreferences(){
        patientGlobal?.let {
            AppPreferences.setString(AppConstant.USERNAME, it.name)
            AppPreferences.setString(AppConstant.EMAIL, it.email)
            AppPreferences.setString(AppConstant.MOBILE, it.mobile)
            AppPreferences.setString(AppConstant.GENDER, it.gender)
            AppPreferences.setString(AppConstant.DOB, it.dob)
        }
    }

    private fun setUpOnclickListener() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.dobInput.setOnClickListener {
            openDatePicker()
        }
        binding.btnSave.setOnClickListener {
            updatePatientProfile()
        }
    }

    private fun updatePatientProfile() {
        val validateMessage = isValidateForm()
        if (validateMessage != "") {
            showOkDialog(validateMessage)
            return
        }

        val patient  = Patient(
            name = binding.nameInput.text.toString().trim(),
            mobile = binding.numberInput.text.toString().trim(),
            dob  = binding.dobInput.text.toString().trim(),
            gender = when(binding.rgGender.checkedRadioButtonId){
                R.id.rbMale -> "Male"
                R.id.rbFemale ->"Female"
                else -> "Other"
            },
            email = binding.emailInput.text.toString().trim()
        )
        patientGlobal = patient
        viewModel.updatePatientProfile(patient)
    }

    private fun isValidateForm(): String {
        with(binding) {
            if (nameInput.text.toString().isEmpty()) return "Name cannot be empty"
            if (numberInput.text.toString().length < 10) return "Mobile number cannot be less  than 10"
        }
        return ""
    }

    private fun openDatePicker() {

        if (parentFragmentManager.findFragmentByTag("DATE_PICKER") != null) return

        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()


        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setCalendarConstraints(constraints)
                .setTheme(R.style.LightCalendarTheme)
                .build()

        datePicker.show(parentFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(selectedDate))

            binding.dobInput.setText(date)
        }
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

    private fun showLoader() {
        binding.progressAnim.mainProgress.visibility = View.VISIBLE
        binding.editDataContent.alpha = 0.3f

        var dots = listOf<View>()
        with(binding.progressAnim) {
            dot1.setBgTint(R.color.lightGreen)
            dot2.setBgTint(R.color.lightGreen)
            dot3.setBgTint(R.color.lightGreen)
            dots = listOf(dot1, dot2, dot3)
        }

        UtilObject.startDotsAnimation(dots)

    }
    private fun hideLoader() {
        var dots : List<View> = emptyList()
        with(binding.progressAnim){
            dots = listOf(dot1,dot2,dot3)
        }
        UtilObject.stopDotsAnimation(dots)
        binding.editDataContent.alpha = 1f
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}


fun View.setBgTint(@ColorRes color : Int){
    backgroundTintList = ContextCompat.getColorStateList(context,color)
    backgroundTintMode = PorterDuff.Mode.SRC_IN
}
