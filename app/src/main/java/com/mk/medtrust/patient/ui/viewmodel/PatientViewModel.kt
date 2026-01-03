package com.mk.medtrust.patient.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.patient.repository.PatientRepository
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repo: PatientRepository
) : ViewModel(){

    private val _logoutState = MutableLiveData<com.mk.medtrust.util.Result<String>>()
    val logoutState : LiveData<Result<String>> get() = _logoutState

    private val _updateState  = MutableLiveData<Result<String>>()
    val updateState : LiveData<Result<String>>  = _updateState

    fun logOut() {
        viewModelScope.launch {
            _logoutState.postValue(Result.Loading)
            _logoutState.postValue(repo.logout())
        }
    }

    fun updatePatientProfile(patient : Patient){
        viewModelScope.launch {
            _updateState.postValue(Result.Loading)
            _updateState.postValue(repo.updatePatientProfile(patient))
        }
    }



}