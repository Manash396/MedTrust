package com.mk.medtrust.patient.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.helper.widget.Grid
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mk.medtrust.R
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.databinding.FragmentDoctorDetailsBinding
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.model.DateItem
import com.mk.medtrust.patient.model.SlotItem
import com.mk.medtrust.patient.ui.PaymentActivity
import com.mk.medtrust.patient.ui.adapter.DateAdapter
import com.mk.medtrust.patient.ui.adapter.DoctorDetailMainAdapter
import com.mk.medtrust.patient.ui.adapter.SlotAdapter
import com.mk.medtrust.patient.ui.viewmodel.DoctorDetailsViewModel
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.PaymentConstants
import com.mk.medtrust.util.Result
import com.mk.medtrust.util.UtilObject
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.exp

@AndroidEntryPoint
class DoctorDetailsFragment : Fragment() {

    private var _binding: FragmentDoctorDetailsBinding? = null
    private val binding get() = _binding!!

    private val args : DoctorDetailsFragmentArgs by navArgs()
    private val viewModel : DoctorDetailsViewModel by viewModels()
    private lateinit var dateAdapter : DateAdapter
    private lateinit var slotAdapter : SlotAdapter
    private lateinit var mainAdapter : DoctorDetailMainAdapter

    private var appointment : Appointment? =null
    private val appointmentMapByDate : MutableMap<String, MutableList<Appointment>> = mutableMapOf()

    // payment launcher
    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        when(result.resultCode){
            PaymentConstants.RESULT_PAYMENT_SUCCESS -> {
                Toast.makeText(requireContext(), "Payment Successful", Toast.LENGTH_SHORT).show()

                appointment?.let { viewModel.appointmentBooking(it) }

                findNavController().popBackStack()
            }

            PaymentConstants.RESULT_PAYMENT_FAILED -> {
                Toast.makeText(requireContext(), "Payment Failed or Cancelled", Toast.LENGTH_SHORT).show()
                // stay on same fragment
            }
        }
    }


        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnclickListener()
        setObserver()



        val doctor  = args.doctor
        bindingData(doctor)
        viewModel.getAllAppointmentsByDoctor(doctorId = doctor.uid)
    }

    private fun setObserver() {
        viewModel.doctorBookings.observe(viewLifecycleOwner){result ->
            when(result){
                is Result.Error -> {
                    Log.d("Krishna",result.message)
                    Toast.makeText(requireContext(),result.message, Toast.LENGTH_LONG).show()
                    hideLoader()
                }
                Result.Loading -> {
                   showLoader()
                }
                is Result.Success -> {
                    hideLoader()
                    val list  = result.data
//                    Log.d("Krishna",list.toString())
                    appointmentMapByDate.clear()
                    appointmentMapByDate.putAll(appointmentMapFunction(list))
                }
            }
        }
        viewModel.bookingState.observe(viewLifecycleOwner){result ->
            when(result){
                is Result.Error -> {
                    Log.d("Krishna",result.message)
                    Toast.makeText(requireContext(),result.message, Toast.LENGTH_LONG).show()
                }
               Result.Loading -> {

                }
                is Result.Success -> {
                    Toast.makeText(requireContext(),result.data, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun appointmentMapFunction(list : List<Appointment>) : MutableMap<String, MutableList<Appointment>>{
        val map  = mutableMapOf<String, MutableList<Appointment>>()
        list.forEach {
           val appointmentsForDate =  map.getOrPut(it.dateId){ mutableListOf() }
           appointmentsForDate.add(it)
        }

        return map
    }

    private fun bindingData(doctor: Doctor) {
        with(binding){
            name.text = "Dr ${doctor.name}"
            specialist.text = doctor.specialisation
            hospitalName.text = doctor.hospital
            experience.text = "${doctor.experience} \nExperience"
            rating.text = "${doctor.rating}\nRating"
            description.text = "Dr. ${doctor.name} is a highly experienced ${doctor.specialisation} with" +
                    " over ${doctor.experience} of expertise."
            fee.text = doctor.consultationFee
        }
        // for slot
        slotAdapter = SlotAdapter(){  slotItem ->
            val slotId = "${slotItem.dateId}_${slotItem.time.replace(" ","_")}"

            val appmt = Appointment(
                appointmentId = slotId,
                doctorId = doctor.uid,
                doctorName = doctor.name,
                patientId = AppPreferences.getString(AppConstant.UID),
                patientName = AppPreferences.getString(AppConstant.USERNAME),
                slotTime = slotItem.time,
                dateId = slotItem.dateId,
                slotId = slotId
            )
            appointment = appmt
            Log.d("Krishna",appointment.toString())
        }
//        binding.slotRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
//        binding.slotRecyclerView.adapter = slotAdapter

        // for dates
        val datesAvailable = generateDates(doctor.availability)
        dateAdapter = DateAdapter(){ dateItem ->
            // to access on going appointments on that day from firestore
            val dateId  = "${dateItem.dayOfMonth}_${dateItem.monthOfYear}_${dateItem.year}"

            Log.d("Krishna",dateId)
            val ap = appointmentMapByDate[dateId]
            val bookedSlots = ap?.map { appointment -> appointment.slotTime } ?: emptyList()


            val slots = generateSlots(doctor.availability.startTime,doctor.availability.endTime,bookedSlots,dateItem)
            Timber.tag("Krishna").d(slots.toString())

            slotAdapter.updateNewList(slots)
            appointment = null // resetting for new
        }.apply {
            updateNewList(datesAvailable)
        }

//        binding.dateRecyclerView.adapter = dateAdapter.apply {
//            updateNewList(datesAvailable as MutableList<DateItem>)
//        }

// nested recycle view
        mainAdapter = DoctorDetailMainAdapter(slotAdapter,dateAdapter)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.mainRecyclerView.adapter = mainAdapter

        // availability shown here
        showAvailabilityDays(binding.availabilityContainer ,  doctor.availability.days)

    }

    private fun showAvailabilityDays(container: LinearLayout, days: List<String>) {
        container.removeAllViews()

        days.forEach {day ->
            val tv  = TextView(container.context).apply {
                text = day
                setTextColor(Color.WHITE)
                textSize =  12f
                setPadding(24, 12, 24, 12)
                background = ContextCompat.getDrawable(container.context , R.drawable.availability_day_bg)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 12
                layoutParams = params
            }
            container.addView(tv)
        }
    }

    private fun setUpOnclickListener() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.bookButton.setOnClickListener {
            if (appointment == null) {
                Toast.makeText(requireContext(), "Please select a slot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var amtInPaise = (binding.fee.text.toString().replace("[^0-9]".toRegex(),"")).toInt()
            amtInPaise *= 100

            val intent = Intent(requireContext() , PaymentActivity::class.java)
            intent.putExtra(PaymentConstants.EXTRA_AMOUNT,amtInPaise)

            paymentLauncher.launch(intent)
        }
    }

    private fun generateSlots(startTime: String, endTime: String , bookedSlots: List<String>, dateItem  : DateItem): List<SlotItem> {
        val slotDuration  = 30
        val timeFormatter24 = DateTimeFormatter.ofPattern("HH:mm")
        val timeFormatter12 = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

        val dateId  = "${dateItem.dayOfMonth}_${dateItem.monthOfYear}_${dateItem.year}"

        val start = LocalTime.parse(startTime, timeFormatter12)
        val end = LocalTime.parse(endTime, timeFormatter12)

        val slotDate  = LocalDate.of(dateItem.year,dateItem.monthOfYear,dateItem.dayOfMonth)
        val now  = LocalDateTime.now()

        var current = start
        val slots = mutableListOf<SlotItem>()

        while (current.isBefore(end)) {
            val displayTime = current.format(timeFormatter12)

            val slotDateTime  = LocalDateTime.of(
                slotDate,
                current
            )
            Timber.tag("Krishna").d(slotDateTime.toString())
            val isAvailable = !bookedSlots.contains(displayTime) && slotDateTime.isAfter(now)

            slots.add(
                SlotItem(
                    dateId = dateId,
                    time = displayTime,
                    isAvailable = isAvailable
                )
            )

            current = current.plusMinutes(slotDuration.toLong())
        }

        return slots
    }

    private fun generateDates(availability: Doctor.Availability, daysAhead: Int = 10): List<DateItem> {
        val today = LocalDate.now()
        val list = mutableListOf<DateItem>()
        for (i in 0 until daysAhead) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.name.take(3) // "MON", "TUE"
            val monthOfYear = date.monthValue
            val year  = date.year
            val isAvailable = availability.days.any {
                it.equals(dayOfWeek, ignoreCase = true)
            }
            list.add(DateItem(dayOfWeek, date.dayOfMonth, monthOfYear,year,isAvailable))
        }
//        Log.d("Krishna",list.toString())
        return list
    }


    private fun showLoader() {
        binding.progressAnim.mainProgress.visibility = View.VISIBLE
        binding.contentView.alpha = 0.3f
        binding.bookAppointment.alpha = 0.3f

        var dots = listOf<View>()
        with(binding.progressAnim) {
            dot1.setBgTint(R.color.lightGreen)
            dot2.setBgTint(R.color.lightGreen)
            dot3.setBgTint(R.color.lightGreen)
            dots = listOf(dot1, dot2, dot3)
        }

        UtilObject.startDotsAnimation(dots)

    }

    private fun hideLoader() {
        var dots : List<View> = emptyList()
        with(binding.progressAnim){
            dots = listOf(dot1,dot2,dot3)
        }
        UtilObject.stopDotsAnimation(dots)
        binding.contentView.alpha = 1f
        binding.bookAppointment.alpha = 1f
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leaks
    }
}
