package com.mk.medtrust.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
     val dispatcher : TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

     override fun starting(description: Description?) {
          super.starting(description)
          Dispatchers.setMain(dispatcher)
     }

     override fun finished(description: Description?) {
          super.finished(description)
          Dispatchers.resetMain()
     }

}

fun <T> LiveData<T>.getOrAwaitValues(count : Int) : List<T> {
     val values  = mutableListOf<T>()
     val latch  = CountDownLatch(count)

     val observer =  object : Observer<T> {
          override fun onChanged(value: T) {
               values.add(value)
               latch.countDown()
               if (latch.count == 0L){
                    removeObserver(this)
               }
          }
     }

     observeForever(observer)
     latch.await(2 , TimeUnit.SECONDS)
     return values
}