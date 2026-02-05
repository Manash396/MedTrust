package com.mk.medtrust.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.auth.data.model.toLocalDateTime
import com.mk.medtrust.patient.repository.PatientRepository
import com.mk.medtrust.util.AppointmentStatus
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PatientSharedViewModel @Inject constructor(
    private val repo :  PatientRepository
) : ViewModel(){


    private val _appointments = MutableLiveData<com.mk.medtrust.util.Result<List<Appointment>>>()
    val appointments: LiveData<com.mk.medtrust.util.Result<List<Appointment>>> = _appointments

    private var appointmentList = mutableListOf<Appointment>()


    val ongoingAppointmentList : List<Appointment> get() =  ongoingAppointments(appointmentList)
    val historyAppointmentList : List<Appointment> get() = historyAppointment(appointmentList)

    var currentAppointment : Appointment? = null

    private fun ongoingAppointments(list : List<Appointment>) : List<Appointment> {
        val now = LocalDateTime.now() .withSecond(0)
            .withNano(0)
        Log.d("mse",now.toString())

        return list.filter {
            it.toLocalDateTime().isAfter(now.minusMinutes(25)) && AppointmentStatus.COMPLETED != it.status
        }
    }

    private fun historyAppointment(list: List<Appointment>): List<Appointment>{
        val now = LocalDateTime.now()
        return list.filter {
            AppointmentStatus.COMPLETED == it.status
        }
    }

    fun loadAppointments(patientId: String) {
        viewModelScope.launch {
            _appointments.value = Result.Loading
            _appointments.value = repo.getAllAppointments(patientId)
        }
    }

    fun updateList(list: List<Appointment>){
        appointmentList.clear()
        appointmentList.addAll(list)
    }

}