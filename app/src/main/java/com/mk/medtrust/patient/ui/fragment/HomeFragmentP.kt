package com.mk.medtrust.patient.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mk.medtrust.R
import com.mk.medtrust.databinding.FragmentHomeBinding
import com.mk.medtrust.patient.ui.viewmodel.PatientViewModel
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class HomeFragmentP : Fragment() {

    // Backing property
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!
    private val viewModelP : PatientViewModel by activityViewModels()

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

    }

    private fun setUpOnclick() {
        binding.bookConsultation.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_doctorListFragment)
        }
        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_patientProfile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // prevent memory leaks
    }
}
