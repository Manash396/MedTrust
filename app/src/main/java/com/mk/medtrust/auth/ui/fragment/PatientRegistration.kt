package com.mk.medtrust.auth.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.auth.ui.viewmodel.AuthViewModel
import com.mk.medtrust.databinding.FragmentPatientRegistrationBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.patient.ui.PatientActivity
import com.mk.medtrust.sheet.BottomSheetDialog
import com.mk.medtrust.sheet.OptionCLickListener
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.mk.medtrust.util.UtilObject.clearErrorOnType
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PatientRegistration : Fragment() , OptionCLickListener{

    private var _binding: FragmentPatientRegistrationBinding? = null
    private val binding get() = _binding!!
    private val  viewModel : AuthViewModel by viewModels()

    private lateinit var specialisationDialog : BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0,systemBars.top,0,systemBars.bottom)
            insets
        }

        specialisationDialog = BottomSheetDialog(this@PatientRegistration, AppConstant.gender)

        setUpOnclick()

        addListener()
        showLoader()

        Handler(Looper.getMainLooper()).postDelayed({
            hideLoader()
        }, 1000)

        observeResponse()
    }

    private fun observeResponse() {
        viewModel.patientRegistrationState.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Error<*> -> {
                    showOkDialog(result.message)
                    with(binding){
                        etPassword.setText("")
                        etFullName.setText("")
                        etGender.setText("")
                        etPhone.setText("")
                        etEmail.setText("")
                        etDob.setText("")
                        etConfirmPassword.setText("")
                    }
                    hideLoader()
                }
                Result.Loading -> showLoader()
                is Result.Success<*> -> {
                    hideLoader()
                    startActivity(Intent(requireActivity(), PatientActivity::class.java))
                    requireActivity().finish()
                }
            }

        }
    }

    private fun setUpOnclick() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.etDob.setOnClickListener {
            openDatePicker()
        }
        binding.etGender.setOnClickListener {
            if (!specialisationDialog.isAdded)specialisationDialog.show(parentFragmentManager,"genderDialog")
        }
        binding.btnCreateAccount.setOnClickListener {
            register()
        }

    }

    private fun addListener(){
        binding.etEmailLayout.clearErrorOnType()
        binding.etPasswordLayout.clearErrorOnType()
        binding.etFullNameLayout.clearErrorOnType()
        binding.etGenderLayout.clearErrorOnType()
        binding.etPhoneLayout.clearErrorOnType()
        binding.etDobLayout.clearErrorOnType()
        binding.etConfirmPasswordLayout.clearErrorOnType()

    }
    private fun register() {
        val isValidate = validate()

        if (!isValidate) return
        val patient = Patient(
            uid = "",
            email = binding.etEmail.text.toString().trim(),
            dob = binding.etDob.text.toString().trim(),
            mobile = binding.etPhone.text.toString().trim(),
            gender = binding.etGender.text.toString().trim(),
            name =  binding.etFullName.text.toString().trim()
        )

        viewModel.registerPatient(patient,binding.etPassword.text.toString().trim())
    }

    private fun validate() : Boolean {

        with(binding) {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val dob = etDob.text.toString().trim()
            val gender = etGender.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            // Full Name
            if (fullName.isEmpty()) {
                etFullNameLayout.error = "Field required"
                return false
            } else etFullNameLayout.error = null

            // Email
            if (email.isEmpty()) {
                etEmailLayout.error = "Field required"
                return false
            } else etEmailLayout.error = null

            // Password
            if (password.isEmpty()) {
                etPasswordLayout.error = "Field required"
                return false
            } else etPasswordLayout.error = null

            // Confirm Password
            if (confirmPassword.isEmpty()) {
                etConfirmPasswordLayout.error = "Field required"
                return false
            } else etConfirmPasswordLayout.error = null

            // Both passwords match?
            if (password != confirmPassword) {
                etConfirmPasswordLayout.error = "Passwords do not match"
                return false
            }

            // Date of birth
            if (dob.isEmpty()) {
                etDobLayout.error = "Select your date of birth"
                return false
            } else etDobLayout.error = null

            // Gender
            if (gender.isEmpty()) {
                etGenderLayout.error = "Select gender"
                return false
            } else etGenderLayout.error = null

            // Mobile number
            if (phone.isEmpty()) {
                etPhoneLayout.error = "Field required"
                return false
            } else if (phone.length != 10) {
                etPhoneLayout.error = "Enter valid 10-digit number"
                return false
            } else etPhoneLayout.error = null

        }

        return true
    }

    private fun hideLoader() {
        var dots : List<View> = emptyList()
        with(binding.progressAnim){
            dots = listOf(dot1,dot2,dot3)
        }
        UtilObject.stopDotsAnimation(dots)
        binding.registerContent.alpha = 1f
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }

    private fun showLoader(){
        binding.registerContent.alpha = 0.3f
        binding.progressAnim.mainProgress.visibility = View.VISIBLE

        var dots  = listOf<View>()
        with(binding.progressAnim){
            dots = listOf(dot1,dot2,dot3)
        }

        UtilObject.startDotsAnimation(dots)

    }

    private fun openDatePicker() {

        if(parentFragmentManager.findFragmentByTag("DATE_PICKER") !=null  )return

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

            binding.etDob.setText(date)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionClickDo(name: String) {
        binding.etGender.setText(name)
        specialisationDialog.dismiss()
    }
}
