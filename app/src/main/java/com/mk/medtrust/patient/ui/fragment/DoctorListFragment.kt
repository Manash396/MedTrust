package com.mk.medtrust.patient.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mk.medtrust.R
import com.mk.medtrust.databinding.FragmentDoctorListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorListFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentDoctorListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate using ViewBinding
        _binding = FragmentDoctorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnclick()

    }

    private fun setUpOnclick() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.cardDoctor.setOnClickListener {
            findNavController().navigate(R.id.doctorListFragment_to_doctorDetailFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks
        _binding = null
    }
}
