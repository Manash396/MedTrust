package com.mk.medtrust.patient.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mk.medtrust.R
import com.mk.medtrust.databinding.FragmentDoctorListBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.ui.adapter.DoctorListAdapter
import com.mk.medtrust.patient.ui.viewmodel.DoctorListViewModel
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorListFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentDoctorListBinding? = null
    private val binding get() = _binding!!

    private val viewModel : DoctorListViewModel by viewModels()
    private lateinit var doctorListAdapter : DoctorListAdapter
    private var mapCategorisedDoctor  = mutableMapOf<String,MutableList<Doctor>>()


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

        setDoctorListAdapter()
        observeResponse()

        // map need to be empty because coming back to this fragment , views are recreated and data is fetch but map exist
        // since fragment exist
        viewModel.fetchDoctors()
    }

    private fun observeResponse() {
        viewModel.doctorListState.observe(viewLifecycleOwner){result ->
            when(result){
                is Result.Error -> {
                    hideLoader()
                    showOkDialog(result.message)
                }
                Result.Loading -> {
                    showLoader()
                }
                is Result.Success -> {
                    hideLoader()
                    val doctors = result.data
                    // categorizing the list
                    categoriseDoctorList(doctors)
                    if (doctors.isEmpty()) binding.emptyListDoctor.visibility = View.VISIBLE
                    doctorListAdapter.updateList(doctors)
                }
            }
        }
    }

    private fun categoriseDoctorList(doctors: List<Doctor>) {
        mapCategorisedDoctor.clear()
        doctors.forEach { doctor ->
            mapCategorisedDoctor.getOrPut(doctor.specialisation){ mutableListOf() }
                .add(doctor)
        }
        mapCategorisedDoctor["All"] = doctors as MutableList<Doctor>
    }

    private fun setDoctorListAdapter() {
        binding.rvDoctors.layoutManager = LinearLayoutManager(requireContext())
        doctorListAdapter = DoctorListAdapter(){ doctor ->
            val action  = DoctorListFragmentDirections.doctorListFragmentToDoctorDetailFragment(doctor)
            findNavController().navigate(action)
        }
        binding.rvDoctors.adapter = doctorListAdapter
    }

    private fun setUpOnclick() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()){
                val chip  = group.findViewById<Chip>(checkedIds[0])
                val category = chip.text.toString()

                mapCategorisedDoctor[category]?.let { list ->
                    doctorListAdapter.updateList(list)
                    binding.emptyListDoctor.visibility = View.INVISIBLE
                } ?: run {
                    doctorListAdapter.updateList(emptyList())
                    binding.emptyListDoctor.visibility = View.VISIBLE
                }
            }
        }

        binding.searchView.doOnTextChanged {text, _, _, _ ->
            applyFilter(text?.trim())
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

    private fun applyFilter(text: CharSequence?) {
        binding.chipAll.isChecked = true
        // if text is empty
        binding.emptyListDoctor.visibility = View.INVISIBLE
        if (mapCategorisedDoctor.isEmpty()) return
        val fullList  = mapCategorisedDoctor["All"] 
        if (text?.isEmpty() == true){
            doctorListAdapter.updateList(fullList as List<Doctor>)
            return
        }
        
        val filteredList = fullList?.filter { doctor ->
            doctor.name.contains(text ?: "99" , true)
        } ?: emptyList()

        if (filteredList.isEmpty()){
            binding.emptyListDoctor.visibility = View.VISIBLE
        }
        doctorListAdapter.updateList(filteredList)
    }

    private fun showLoader() {
        binding.progressAnim.mainProgress.visibility = View.VISIBLE
        binding.rvDoctors.visibility = View.INVISIBLE

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
        binding.rvDoctors.visibility = View.VISIBLE
        binding.progressAnim.mainProgress.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks
        _binding = null
    }
}




