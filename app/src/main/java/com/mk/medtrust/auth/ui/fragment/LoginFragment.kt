package com.mk.medtrust.auth.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.auth.ui.viewmodel.AuthViewModel
import com.mk.medtrust.databinding.FragmentLoginBinding
import com.mk.medtrust.doctor.ui.DoctorActivity
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.patient.ui.PatientActivity
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.mk.medtrust.util.UtilObject.clearErrorOnType
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel : AuthViewModel by viewModels()
    private var isDoctor: Boolean? = null

    private var currentId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        Log.d("krishna", "found")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addListeners()
        binding.btnPatient.isChecked = true

        setUpOnclick()
        observeResponse()
    }

    private fun observeResponse() {
        viewModel.loginState.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Error<*> -> {
                    showOkDialog(result.message)
                    hideLoader()
                    binding.etEmail.setText("")
                    binding.etPassword.setText("")
                }
                Result.Loading -> {
                    showLoader()
                }
                is Result.Success<*> -> {
                    hideLoader()
                    if (isDoctor == true){
                        startActivity(Intent(requireActivity() , DoctorActivity::class.java))
                    }else{
                        startActivity(Intent(requireActivity(), PatientActivity::class.java))
                    }
                    requireActivity().finish()
                }
            }
        }
    }

    private fun setUpOnclick() {
        binding.btnSignUp.setOnClickListener {
            isDoctor?.let {
                if (!it) findNavController().navigate(R.id.action_login_to_patient)
                else findNavController().navigate(R.id.action_login_to_doctor)
            }
        }
        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login(){
        with(binding){
            val email  = etEmail.text.toString().trim()
            val key    = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmailLayout.error = "Field required"
                return
            }
            if (key.isEmpty()) {
                etPasswordLayout.error = "Field required"
                return
            }

            isDoctor?.let {  viewModel.login(email,key, it) }
        }
    }
    private fun addListeners() {
        binding.toggleUser.addOnButtonCheckedListener { group, checkedId, isChecked ->
            listOf(binding.btnPatient, binding.btnDoctor).forEach { btn ->
                if (btn.isChecked) {
                    btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.veryLightBlack
                        )
                    )
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightBlack))
                }
            }

            if (checkedId == binding.btnPatient.id && isChecked) {
                binding.btnLogin.setText(R.string.log_in_patient)
                isDoctor = false
            }

            if (checkedId == binding.btnDoctor.id && isChecked) {
                binding.btnLogin.setText(R.string.log_in_doctor)
                isDoctor = true
            }
            if (currentId != checkedId) {
                binding.etEmail.setText("")
                binding.etPassword.setText("")

                showLoader()
                Handler(Looper.getMainLooper()).postDelayed(
                    {hideLoader()},2000
                )
            }

            currentId = checkedId
        }

        binding.etEmailLayout.clearErrorOnType()
        binding.etPasswordLayout.clearErrorOnType()

    }

    private fun hideLoader() {
        var dots : List<View> = emptyList()
        with(binding.progressAnim){
            dots = listOf(dot1,dot2,dot3)
        }
        UtilObject.stopDotsAnimation(dots)
        binding.loginContent.visibility = View.VISIBLE
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }

    private fun showLoader() {
        binding.loginContent.visibility = View.INVISIBLE
        binding.progressAnim.mainProgress.visibility = View.VISIBLE

        var dots = listOf<View>()
        with(binding.progressAnim) {
            dots = listOf(dot1, dot2, dot3)
        }

        UtilObject.startDotsAnimation(dots)
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
}
