package com.mk.medtrust.doctor.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.auth.data.model.toLocalDateTime
import com.mk.medtrust.doctor.repository.DoctorRepository
import com.mk.medtrust.util.AppointmentStatus
import com.mk.medtrust.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DoctorSharedViewModel @Inject constructor(
    private val repo : DoctorRepository
): ViewModel(){

    private val _appointments = MutableLiveData<Result<List<Appointment>>>()
    val appointments: LiveData<Result<List<Appointment>>> = _appointments

    private var appointmentList = mutableListOf<Appointment>()


    val ongoingAppointmentList : List<Appointment> get() =  ongoingAppointments(appointmentList)
    val historyAppointmentList : List<Appointment> get() = historyAppointment(appointmentList)

    private fun ongoingAppointments(list : List<Appointment>) : List<Appointment> {
        val now = LocalDateTime.now() .withSecond(0)
            .withNano(0)
        Log.d("mse",now.toString())

        return list.filter {
            it.toLocalDateTime().isAfter(now.minusMinutes(25))
        }
    }

    private fun historyAppointment(list: List<Appointment>): List<Appointment>{
        val now = LocalDateTime.now()
        return list.filter {
            it.toLocalDateTime().isBefore(now.minusMinutes(25)) && AppointmentStatus.COMPLETED == it.status
        }
    }

    fun loadAppointments(doctorId: String) {
        viewModelScope.launch {
            _appointments.value = Result.Loading
            _appointments.value = repo.getAllAppointments(doctorId)
        }
    }

    fun updateList(list: List<Appointment>){
        appointmentList.clear()
        appointmentList.addAll(list)
    }
}