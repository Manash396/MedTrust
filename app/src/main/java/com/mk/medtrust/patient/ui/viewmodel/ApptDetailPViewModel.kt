package com.mk.medtrust.patient.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.repository.PatientRepository
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApptDetailPViewModel @Inject constructor(
    private val repo  : PatientRepository
) : ViewModel(){

    private val  _appointmentDetail = MutableLiveData<Result<Appointment>>()
    val appointmentDetail : LiveData<Result<Appointment>> = _appointmentDetail

    private val  _doctorDetail = MutableLiveData<Result<Doctor>>()
    val doctorDetail : LiveData<Result<Doctor>> = _doctorDetail

    private val  _apptUpdateStatus = MutableLiveData<Result<String>>()
    val apptUpdateStatus : LiveData<Result<String>> = _apptUpdateStatus

    fun getAppointmentDetail(apptId : String){
        viewModelScope.launch {
            _appointmentDetail.value  = Result.Loading
            _appointmentDetail.value  = repo.getAppointmentDetail(apptId)
        }
    }

    fun getDoctorDetail(doctorId : String){
        viewModelScope.launch {
            _doctorDetail.value  = Result.Loading
            _doctorDetail.value  = repo.getDoctorDetail(doctorId)
        }
    }

    fun markCompleteAppointment(doctorId: String ,dateId: String, apptId: String){
        viewModelScope.launch {
            _apptUpdateStatus.value = Result.Loading
            _apptUpdateStatus.value = repo.markCompleteAppointment(doctorId , dateId, apptId)
        }
    }


}