package com.mk.medtrust.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mk.medtrust.databinding.BottomSheetDialogBinding
import com.mk.medtrust.util.AppConstant

class BottomSheetDialog(
    private val callback: OptionCLickListener,
    private val options :List<String>
) : BottomSheetDialogFragment() {

    private var _binding : BottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // for custom bottom sheet
        _binding = BottomSheetDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val optionList = options.map { spe ->
            OptionItem(spe)
        }

        with(binding.recyclerOptions){
            layoutManager  = LinearLayoutManager(requireContext())
            adapter = BottomSheetAdapter(optionList , callback)
        }

    }

}