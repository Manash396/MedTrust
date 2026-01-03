package com.mk.medtrust.patient.ui.fragment

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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.auth.LoginActivity
import com.mk.medtrust.databinding.FragmentProfilePatientBinding
import com.mk.medtrust.doctor.ui.DoctorActivity
import com.mk.medtrust.patient.ui.PatientActivity
import com.mk.medtrust.patient.ui.viewmodel.PatientViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfilePatientFragment : Fragment() {

    private var _binding: FragmentProfilePatientBinding? = null
    private val binding get() = _binding!!

    private val viewModelP : PatientViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "profile_update_result",
            viewLifecycleOwner
        ){_ , bundle ->
            val success = bundle.getBoolean("success")
            if (success){
                Toast.makeText(requireContext(),"Profile Updated successfully", Toast.LENGTH_LONG)
                    .show()
            }
        }

        setUpOnclick()
        observeResponse()
        updateUi()
    }

    private fun updateUi() {
        with(binding){
            tvName.text = AppPreferences.getString(AppConstant.USERNAME)
            tvEmail.text = AppPreferences.getString(AppConstant.EMAIL)
            tvMobile.text = AppPreferences.getString(AppConstant.MOBILE)
            tvGender.text = AppPreferences.getString(AppConstant.GENDER)
            tvDob.text = AppPreferences.getString(AppConstant.DOB)
            tvStatus.text = AppPreferences.getString(AppConstant.ROLE)
        }
    }

    private fun observeResponse() {
        viewModelP.logoutState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.mk.medtrust.util.Result.Error<*> -> {
                    showOkDialog(result.message)
                }

                com.mk.medtrust.util.Result.Loading -> {
                    showLoader()
                }

                is Result.Success<*> -> {
                    hideLoader()
                    requireActivity().finish()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                }
            }
        }
    }

    private fun showLoader() {
        binding.progressAnim.mainProgress.visibility = View.VISIBLE

        var dots = listOf<View>()
        with(binding.progressAnim) {
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
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }

    private fun hideLoader(dots1: List<View>) {
        UtilObject.stopDotsAnimation(dots1)
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }
    private fun setUpOnclick() {
        binding.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnLogout.setOnClickListener {
            viewModelP.logOut()
        }
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.patientProfile_to_editFragment)
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
        _binding = null   // avoid memory leaks
    }
}
