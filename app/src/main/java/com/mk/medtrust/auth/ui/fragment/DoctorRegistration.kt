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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.auth.ui.viewmodel.AuthViewModel
import com.mk.medtrust.databinding.FragmentDoctorRegistrationBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.ui.DoctorActivity
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.sheet.BottomSheetDialog
import com.mk.medtrust.sheet.OptionCLickListener
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.mk.medtrust.util.UtilObject.clearErrorOnType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorRegistration : Fragment(), OptionCLickListener {

    private var _binding: FragmentDoctorRegistrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel : AuthViewModel by viewModels()
    private lateinit var specialisationDialog : BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        specialisationDialog = BottomSheetDialog(this@DoctorRegistration, AppConstant.specialisationList)
        showLoader()


        Handler(Looper.getMainLooper()).postDelayed({
            hideLoader()
        }, 1000)

        setUpOnclick()
        addListener()

        observeResponse()
    }

    private fun observeResponse(){
        viewModel.doctorRegistrationState.observe(viewLifecycleOwner){result ->
            when(result){
                is Result.Error<*> -> {
                    showOkDialog(result.message)
                    with(binding){
                        etName.setText("")
                        etEmail.setText("")
                        etPhone.setText("")
                        etLicense.setText("")
                        etSpecialization.setText("")
                        etPassword.setText("")
                        etConfirmPassword.setText("")
                    }
                    hideLoader()
                }
                Result.Loading -> showLoader()
                is Result.Success<*> -> {
                    hideLoader()
                    val activity = requireActivity()
                    startActivity(Intent(activity, DoctorActivity::class.java))
                    activity.finish()
                }
            }
        }
    }

    private fun addListener(){
        with(binding){
            etNameLayout.clearErrorOnType()
            etEmailLayout.clearErrorOnType()
            etPhoneLayout.clearErrorOnType()
            etLicenseLayout.clearErrorOnType()
            etSpecializationLayout.clearErrorOnType()
            etPasswordLayout.clearErrorOnType()
            etConfirmPasswordLayout.clearErrorOnType()
            etHospitalLayout.clearErrorOnType()
        }
    }
    private fun setUpOnclick() {
        with(binding) {

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            etSpecialization.setOnClickListener {
               if (!specialisationDialog.isAdded) specialisationDialog.show(parentFragmentManager,"specDialog")
            }
            btnRegister.setOnClickListener {
                register()
            }

        }


    }

    private fun register() {
        val isValidate = validate()

        if (!isValidate) return
        val doctor = Doctor(
            name = binding.etName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            contact = binding.etPhone.text.toString().trim(),
            medicalLicenseNo = binding.etLicense.text.toString().trim(),
            specialisation = binding.etSpecialization.text.toString().trim(),
            hospital = binding.etHospital.text.toString().trim()
        )

        viewModel.registerDoctor(doctor,binding.etPassword.text.toString().trim())
    }

    private fun hideLoader() {
        var dots = listOf<View>()
        with(binding.progressAnim) {
            dots = listOf(dot1, dot2, dot3)
        }
        UtilObject.stopDotsAnimation(dots)
        binding.registerContent.alpha = 1f
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }

    private fun showLoader() {
        binding.registerContent.alpha = 0.3f
        binding.progressAnim.mainProgress.visibility = View.VISIBLE

        var dots = listOf<View>()
        with(binding.progressAnim) {
            dots = listOf(dot1, dot2, dot3)
        }

        UtilObject.startDotsAnimation(dots)

    }

    private fun validate() : Boolean {

        with(binding) {
            val fullName = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val license = etLicense.text.toString().trim()
            val specialise = etSpecialization.text.toString().trim()
            val hospital = etHospital.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            // Full Name
            if (fullName.isEmpty()) {
                etNameLayout.error = "Field required"
                return false
            } else etNameLayout.error = null

            // Email
            if (email.isEmpty()) {
                etEmailLayout.error = "Field required"
                return false
            } else etEmailLayout.error = null

            // Mobile number
            if (phone.isEmpty()) {
                etPhoneLayout.error = "Field required"
                return false
            } else if (phone.length != 10) {
                etPhoneLayout.error = "Enter valid 10-digit number"
                return false
            } else etPhoneLayout.error = null

            // Medical License
            if (license.isEmpty()) {
                etLicenseLayout.error = "Field Required"
                return false
            } else etLicenseLayout.error = null

            // Specialisation
            if (specialise.isEmpty()) {
                etSpecializationLayout.error = "Select specialisation"
                return false
            } else etSpecializationLayout.error = null

            // Hospital
            if (hospital.isEmpty()) {
                etSpecializationLayout.error = "Field Required"
                return false
            } else etSpecializationLayout.error = null

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

        }

        return true
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
        binding.etSpecialization.setText(name)
        specialisationDialog.dismiss()
    }
}
