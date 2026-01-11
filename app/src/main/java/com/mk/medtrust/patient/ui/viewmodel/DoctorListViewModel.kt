package com.mk.medtrust.patient.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.repository.PatientRepository
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorListViewModel @Inject constructor(
    private val repo: PatientRepository
) : ViewModel() {

    private val _doctorListState = MutableLiveData<Result<List<Doctor>>>()
    val doctorListState: LiveData<Result<List<Doctor>>> get() = _doctorListState

    fun fetchDoctors() {
        viewModelScope.launch {
            _doctorListState.postValue(Result.Loading)
            _doctorListState.postValue(repo.getDoctorList())
        }
    }
}
