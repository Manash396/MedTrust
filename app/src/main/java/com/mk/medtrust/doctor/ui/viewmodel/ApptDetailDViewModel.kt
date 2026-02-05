package com.mk.medtrust.doctor.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.doctor.repository.DoctorRepository
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApptDetailDViewModel @Inject constructor(
    private val repo : DoctorRepository
) : ViewModel(){

    private val _appointmentDetailStatus = MutableLiveData<Result<Appointment>>()
    // since this reference is type LiveData does no about the methods present in MutableLiveData
    val  appointmentDetailStatus : LiveData<Result<Appointment>> = _appointmentDetailStatus

    private val _patientDetailStatus = MutableLiveData<Result<Patient>>()
    // since this reference is type LiveData does no about the methods present in MutableLiveData
    val  patientDetailStatus : LiveData<Result<Patient>> = _patientDetailStatus

    fun getAppointmentDetail(appt : Appointment){
        viewModelScope.launch {
            _appointmentDetailStatus.value = Result.Loading
            _appointmentDetailStatus.value = repo.getAppointmentDetail(appt)
        }
    }

    fun getPatientDetail(patientId : String){
        viewModelScope.launch {
            _patientDetailStatus.value = Result.Loading
            _patientDetailStatus.value = repo.getPatientDetail(patientId)
        }
    }

}