package com.mk.medtrust.patient.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.patient.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.mk.medtrust.util.Result
import kotlinx.coroutines.launch

@HiltViewModel
class DoctorDetailsViewModel @Inject constructor(
    private val repo : PatientRepository
) : ViewModel(){

    private val _bookingState = MutableLiveData<Result<String>>()
    val bookingState: LiveData<Result<String>> = _bookingState

    private val _doctorBookings = MutableLiveData<Result<List<Appointment>>>()
    val doctorBookings  : LiveData<Result<List<Appointment>>> = _doctorBookings


    fun getAllAppointmentsByDoctor(doctorId: String){
        viewModelScope.launch {
            _doctorBookings.value = Result.Loading
            _doctorBookings.value = repo.getAllAppointmentsByDoctor(doctorId)
        }
    }
    fun appointmentBooking(appointment: Appointment) {
        viewModelScope.launch {
            _bookingState.value = Result.Loading
            _bookingState.value = repo.appointmentBooking(appointment)
        }
    }

}