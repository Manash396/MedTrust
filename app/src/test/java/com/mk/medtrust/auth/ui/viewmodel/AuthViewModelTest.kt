package com.mk.medtrust.auth.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.ExpectFailure.assertThat
import com.google.common.truth.Truth.assertThat
import com.mk.medtrust.auth.data.repository.AuthRepository
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.util.MainDispatcherRule
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.getOrAwaitValues
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel : AuthViewModel
    private val repo : AuthRepository = mockk()

    @get:Rule
    val mainDispatcher = MainDispatcherRule(StandardTestDispatcher()) // for coroutine
    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()    // for live data

    @Before
    fun setUp(){
        viewModel = AuthViewModel(repo)
    }

//  boundary test
//  text cases for login function
//  In this test case I will use extension function that uses latch

//  success path
    @Test
    fun login_success_emitsLoadingThenSuccess() = runTest {
        coEvery { repo.login(any() , any() , any() ) } returns Result.Success("Successful")

        viewModel.login("mk@gmail.com","sdsds",false)

        val states = viewModel.loginState.getOrAwaitValues(2)

        advanceUntilIdle()

        assertThat(states[0]).isEqualTo(Result.Loading)
        assertThat(states[1]).isEqualTo(Result.Success("Successful"))

        coVerify {
            repo.login("mk@gmail.com","sdsds",false)
        }
    }

//  error path
    @Test
    fun login_fail_emitsLoadingThenFailure() = runTest {
        coEvery { repo.login(any() , any() , any() ) } returns Result.Error("failed")

        viewModel.login("mk@gmail.com","sdsds",false)

        val states = viewModel.loginState.getOrAwaitValues(2)

        advanceUntilIdle()

        assertThat(states[0]).isEqualTo(Result.Loading)
        assertThat(states[1]).isEqualTo(Result.Error("failed",null))

        coVerify {
            repo.login("mk@gmail.com","sdsds",false)
        }

    }

//  no boundary test cases for login  due to its handle inside the ui itself but it is better to handle here

//  register doctor fun
//  error path
    @Test
fun register_doctor_emitsLoadingThenSuccess() = runTest {
    coEvery { repo.registerDoctor(any() , any()) } returns Result.Success("Successful")

    viewModel.registerDoctor(Doctor() , "key")

    val states = viewModel.doctorRegistrationState.getOrAwaitValues(2)

    advanceUntilIdle()

    assertThat(states[0]).isEqualTo(Result.Loading)
    assertThat(states[1]).isEqualTo(Result.Success("Successful"))

    coVerify {
        repo.registerDoctor(Doctor() , "key")
    }

}




}
