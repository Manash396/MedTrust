package com.mk.medtrust.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.auth.data.model.AppUser
import com.mk.medtrust.auth.data.repository.AuthRepository
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mk.medtrust.util.Result

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private var _patientRegisterState = MutableLiveData<Result<String>>()
    val patientRegistrationState: LiveData<Result<String>> get() = _patientRegisterState

    private var _doctorRegisterState = MutableLiveData<Result<String>>()
    val doctorRegistrationState: LiveData<Result<String>> get() = _doctorRegisterState

    private var _loginState = MutableLiveData<Result<String>>()
    val loginState : LiveData<Result<String>> get() = _loginState



    fun registerDoctor(doctor: Doctor, key: String) {
        viewModelScope.launch {
            _doctorRegisterState.postValue(Result.Loading)
            val result = repo.registerDoctor(doctor, key)
            _doctorRegisterState.value = result
        }
    }

    fun registerPatient(patient: Patient, key: String) {
        viewModelScope.launch {
            _patientRegisterState.value = Result.Loading
            val result = repo.registerPatient(patient, key)
            _patientRegisterState.value = result

        }
    }

    fun login(email : String , key : String, isDoctor : Boolean) {
        viewModelScope.launch {
            _loginState.value = Result.Loading
            _loginState.value = repo.login(email, key, isDoctor)
        }
    }


}