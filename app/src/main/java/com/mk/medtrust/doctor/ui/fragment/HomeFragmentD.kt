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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.auth.LoginActivity
import com.mk.medtrust.databinding.FragmentHomeDBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.ui.viewmodel.DoctorViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel


@AndroidEntryPoint
class HomeFragmentD : Fragment() {

    private var  _binding  : FragmentHomeDBinding? = null
    private val binding get() = _binding!!

    private val viewModel : DoctorViewModel by activityViewModels()

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



    }

    private fun setUpOnClick() {
        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_doctorProfileFragment)
        }
        binding.profileSecondBtn.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_doctorProfileFragment)
        }
    }

    private fun saveToPreferences(doctor: Doctor){
        AppPreferences.setString(AppConstant.EXP,doctor.experience)
        AppPreferences.setString(AppConstant.RATE,doctor.rating)
        AppPreferences.setString(AppConstant.RATEN,doctor.rateNo)
        AppPreferences.setString(AppConstant.LISC,doctor.medicalLicenseNo)
        AppPreferences.setString(AppConstant.FEE,doctor.consultationFee)
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