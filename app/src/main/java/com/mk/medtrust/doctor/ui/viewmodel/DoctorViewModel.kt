package com.mk.medtrust.doctor.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.repository.DoctorRepository
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val repo : DoctorRepository
) : ViewModel(){

    private val _doctorDetailState  = MutableLiveData<Result<Doctor>>()
    val doctorDetailState : LiveData<Result<Doctor>> = _doctorDetailState

    private val _logoutState = MutableLiveData<com.mk.medtrust.util.Result<String>>()
    val logoutState : LiveData<Result<String>> get() = _logoutState

    private val _profileUpdateState = MutableLiveData<com.mk.medtrust.util.Result<String>>()
    val profileUpdateState : LiveData<Result<String>> get() = _profileUpdateState

    private val _availabilityUpdateState = MutableLiveData<com.mk.medtrust.util.Result<String>>()
    val availabilityUpdateState : LiveData<Result<String>> get() = _availabilityUpdateState
    fun updateDoctorProfDetails(doctor: Doctor){
        viewModelScope.launch {
            _profileUpdateState.value = Result.Loading
            _profileUpdateState.value = repo.updateDoctorProfDetails(doctor)
        }
    }
    fun getDoctorDetails(uid : String){
        viewModelScope.launch {
            _doctorDetailState.value = Result.Loading
            _doctorDetailState.value = repo.getDoctorDetails(uid)
        }
    }

    fun updateDoctorAvailability(av : Doctor.Availability){
        viewModelScope.launch {
            _availabilityUpdateState.value = Result.Loading
            _availabilityUpdateState.value = repo.updateDoctorAvailability(av)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _logoutState.postValue(Result.Loading)
            _logoutState.postValue(repo.logout())
        }
    }

}