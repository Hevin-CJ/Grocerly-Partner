package com.example.grocerlypartners.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.grocerlypartners.R
import com.example.grocerlypartners.databinding.FilterLayoutBinding
import com.example.grocerlypartners.databinding.FragmentBusinessBinding
import com.example.grocerlypartners.model.BusinessUiState
import com.example.grocerlypartners.model.SalesStatus
import com.example.grocerlypartners.utils.DashboardFilter
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.viewmodel.BusinessViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.zze
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class Business : Fragment() {
    private var business: FragmentBusinessBinding?=null
    private val binding get() = business!!

    private val businessViewModel: BusinessViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       business = FragmentBusinessBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeBusinessUiState()
        showFilterDialogue()
        observeTimeFilter()
        observeNoInternetState()
    }

    private fun observeNoInternetState() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(businessViewModel.businessUiState,businessViewModel.networkState){stateList,isOnline->
                updateUiState(stateList,isOnline)
            }.collect()
        }
    }

    private fun updateUiState(
        stateList: BusinessUiState?,
        isOnline: Boolean,
    ) {

        val hasData = stateList!=null && stateList !is NetworkResult.Error<*>
        val hasError = stateList!=null && stateList is NetworkResult.Error<*>

        when{
            !isOnline && hasData -> {
                binding.txtviewOffline.visibility = View.VISIBLE
                binding.txtviewOfflinesummary.visibility = View.VISIBLE
                binding.nodatagifimg.visibility = View.INVISIBLE
                binding.txtviewnodatafound.visibility = View.INVISIBLE
                binding.txtviewtiming.visibility = View.VISIBLE
            }

            !isOnline && !hasData -> {
                binding.materialCardView4.visibility = View.GONE
                binding.materialCardView6.visibility = View.GONE
                binding.nodatagifimg.visibility = View.VISIBLE
                binding.txtviewnodatafound.visibility = View.VISIBLE
                binding.txtviewtiming.visibility = View.INVISIBLE
                binding.txtviewOffline.visibility = View.INVISIBLE
                binding.txtviewOfflinesummary.visibility = View.INVISIBLE
            }

            isOnline && hasData -> {
                binding.materialCardView6.visibility = View.VISIBLE
                binding.materialCardView4.visibility = View.VISIBLE
                binding.nodatagifimg.visibility = View.INVISIBLE
                binding.txtviewnodatafound.visibility = View.INVISIBLE
                binding.txtviewtiming.visibility = View.VISIBLE
                binding.txtviewOffline.visibility = View.INVISIBLE
                binding.txtviewOfflinesummary.visibility = View.INVISIBLE
            }

        }

    }

    private fun observeTimeFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            businessViewModel.filterState.collect { (filter,_) ->
                showTimeFrame(filter)
                businessViewModel.loadDataWithFilter(filter)
            }

        }
    }

    private fun showFilterDialogue() {
        binding.apply {

            txtviewfilterbtn.setOnClickListener {
                val filterLayout = FilterLayoutBinding.inflate(LayoutInflater.from(requireContext()))
                val dialog = createDialogue(filterLayout)

                var (pendingFilter, pendingButtonId) = businessViewModel.filterState.value

              val updateDialogueUi = {
                  updateButtonSelection(filterLayout,pendingFilter,pendingButtonId)
              }

                setUpPresetClickListeners(filterLayout,updateDialogueUi){ newFilter,newId ->
                    pendingFilter = newFilter
                    pendingButtonId = newId
                }

                setUpCustomDateSelector(filterLayout, updateDialogueUi) { newFilter, newId ->
                    pendingFilter = newFilter
                    pendingButtonId = newId
                }

                setupActionButtons(filterLayout, dialog) {
                    businessViewModel.saveBusinessFilter(pendingFilter, pendingButtonId)
                }

                updateDialogueUi()
                dialog.show()

            }
        }
    }


    private fun setUpPresetClickListeners(filterLayout: FilterLayoutBinding,onUpdate :() -> Unit,onStateChange:(DashboardFilter,Int) -> Unit ){

        val filterMap = mapOf(
            filterLayout.todaybtn to DashboardFilter.Today,
            filterLayout.yesterdaybtn to DashboardFilter.Yesterday,
            filterLayout.weekbtn to DashboardFilter.Week,
            filterLayout.monthbtn to DashboardFilter.Month
        )

        filterMap.forEach { button,filter ->
            button.setOnClickListener {
                onStateChange(filter,it.id)
                onUpdate()
            }
        }
    }


    private fun setUpCustomDateSelector(binding: FilterLayoutBinding,
                                        updateUi: () -> Unit,
                                        onStateChange: (DashboardFilter, Int) -> Unit){
        binding.customDatePicker.setOnClickListener {

            onStateChange(businessViewModel.filterState.value.first, it.id)
            updateUi()

            val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())

            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .setCalendarConstraints(constraintsBuilder.build())
                .build()



            datePicker.addOnPositiveButtonClickListener { selection ->


                val zoneId = ZoneId.of("Asia/Kolkata")

                val adjustedEndDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(selection.second), zoneId)
                    .with(LocalTime.MAX)
                    .toInstant()
                    .toEpochMilli()

                val startDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(selection.first), zoneId).toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()


                val customFilter = DashboardFilter.Custom(startDate, adjustedEndDate)
                onStateChange(customFilter, it.id)
                updateUi()
            }

            datePicker.addOnNegativeButtonClickListener {
                val (previousFilter, previousButtonId) = businessViewModel.filterState.value
                onStateChange(previousFilter, previousButtonId)
                updateUi()
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }


    private fun setupActionButtons(
        binding: FilterLayoutBinding,
        dialog: Dialog,
        onApply: () -> Unit
    ) {
        binding.applybtn.setOnClickListener {
            onApply()
            dialog.dismiss()
        }

        binding.resetbtn.setOnClickListener {
            businessViewModel.saveBusinessFilter(DashboardFilter.Today, R.id.todaybtn)
            dialog.dismiss()
        }
    }

    private fun updateButtonSelection(filterLayout: FilterLayoutBinding,pendingFilter: DashboardFilter,pendingButtonId: Int) {
        val allButtons = listOf(
            filterLayout.todaybtn,
            filterLayout.yesterdaybtn,
            filterLayout.weekbtn,
            filterLayout.monthbtn,
            filterLayout.customDatePicker
        )
        allButtons.forEach { button ->
            val isSelected = (button.id == pendingButtonId)
            button.setBackgroundColor(
                ContextCompat.getColor(requireContext(), if (isSelected) R.color.light_green else R.color.green)
            )
            button.setTextColor(
                ContextCompat.getColor(requireContext(), if (isSelected) R.color.black else R.color.white)
            )

            filterLayout.todaybtn.isEnabled = (pendingFilter != DashboardFilter.Today)
        }

        val isCustomSelected = pendingButtonId == filterLayout.customDatePicker.id
        filterLayout.customDatePicker.setBackgroundColor(
            ContextCompat.getColor(requireContext(), if (isCustomSelected) R.color.light_green else R.color.green)
        )
        filterLayout.customDatePicker.setTextColor(
            ContextCompat.getColor(requireContext(), if (isCustomSelected) R.color.black else R.color.white)
        )

        if (pendingFilter is DashboardFilter.Custom) {
            val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            filterLayout.customDatePicker.text =
                buildString {
                    append(dateFormat.format(Date(pendingFilter.startTime)))
                    append(" - ")
                    append(dateFormat.format(Date(pendingFilter.endTime)))
                }
        } else {
            filterLayout.customDatePicker.text = getString(R.string.select_date_range)
        }
    }

    private fun createDialogue(filterLayout: FilterLayoutBinding): Dialog {
       val dialog = Dialog(requireContext())
        dialog.setContentView(filterLayout.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialog.create()
        return dialog
    }

    private fun showTimeFrame(selectedFilter: DashboardFilter) {
        val zoneId = ZoneId.of("Asia/Kolkata")
        val mainDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        val timeOnlyFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        val detailedFormatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault())

        val timeframeText = when (val filter = selectedFilter) {
            is DashboardFilter.Today -> {

                val now = ZonedDateTime.now(zoneId)
                val startOfDay = now.toLocalDate().atStartOfDay(zoneId)

                val startTimeString = startOfDay.format(timeOnlyFormatter)
                val endTimeString = now.format(timeOnlyFormatter)
                binding.txtviewtiming.text = "From $startTimeString to $endTimeString Today"

                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
               "Today - ${now.format(dateFormatter)}"

            }
            is DashboardFilter.Yesterday ->{
                val yesterday = LocalDate.now(zoneId).minusDays(1)
                val startOfDay = yesterday.atStartOfDay(zoneId)
                val endOfDay = yesterday.atTime(23, 59, 59).atZone(zoneId)

                val startTimeString = startOfDay.format(detailedFormatter)
                val endTimeString = endOfDay.format(detailedFormatter)
                binding.txtviewtiming.text = "From $startTimeString to $endTimeString Yesterday"


                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
                "Yesterday - ${yesterday.format(dateFormatter)}"
            }
            is DashboardFilter.Custom -> {

                val startDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(filter.startTime), zoneId)
                val endDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(filter.endTime), zoneId)


                val startTimeString = startDateTime.format(detailedFormatter)
                val endTimeString = endDateTime.format(detailedFormatter)
                binding.txtviewtiming.text = "From $startTimeString to $endTimeString dates"

                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                "From ${dateFormat.format(Date(filter.startTime))} - ${dateFormat.format(Date(filter.endTime))}"
            }

            DashboardFilter.Month -> {
                val now = ZonedDateTime.now(zoneId)
                val firstDay = now.toLocalDate().withDayOfMonth(1)
                val startOfMonth = firstDay.atStartOfDay(zoneId)

                val startTimeString = startOfMonth.format(detailedFormatter)
                val endTimeString = now.format(detailedFormatter)
                binding.txtviewtiming.text = "From $startTimeString to $endTimeString this Month"


                val formatter = DateTimeFormatter.ofPattern("dd MMM")
                "From ${firstDay.format(formatter)} - ${now.format(formatter)}"
            }
            DashboardFilter.Week -> {
                val now = ZonedDateTime.now(zoneId)
                val firstDayOfWeek = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val startOfWeek = firstDayOfWeek.atStartOfDay(zoneId)

                val startTimeString = startOfWeek.format(detailedFormatter)
                val endTimeString = now.format(timeOnlyFormatter)
                binding.txtviewtiming.text = "From $startTimeString to $endTimeString this Week"
                binding.txtviewtiming.visibility = View.VISIBLE

                val dateFormat = DateTimeFormatter.ofPattern("dd MMM")
                "From ${firstDayOfWeek.format(dateFormat)} - ${now.format(dateFormat)}"
            }
        }
        binding.txtviewtimeframe.text = timeframeText
    }

    private fun observeBusinessUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            businessViewModel.businessUiState.filterIsInstance<BusinessUiState>().collectLatest { uiState ->
                when(uiState){
                    is BusinessUiState.CancelledOrderAmount -> {
                       if ( uiState.totalAmount is NetworkResult.Success){
                           binding.totalcancelledamount.text = uiState.totalAmount.data.toString()
                       }

                        if ( uiState.totalAmount is NetworkResult.Error){
                            Toast.makeText(requireContext(), uiState.totalAmount.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is BusinessUiState.CancelledOrderSize -> {
                        if ( uiState.totalSize is NetworkResult.Success){
                            binding.totalcancelledordersize.text = uiState.totalSize.data.toString()
                        }
                        if ( uiState.totalSize is NetworkResult.Error){
                            Toast.makeText(requireContext(), uiState.totalSize.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is BusinessUiState.TotalActiveOrderAmount -> {
                        if ( uiState.totalAmount is NetworkResult.Success){
                            binding.totalorderamount.text = uiState.totalAmount.data.toString()
                        }

                        if ( uiState.totalAmount is NetworkResult.Error){
                            Toast.makeText(requireContext(), uiState.totalAmount.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is BusinessUiState.TotalOrderSize -> {
                        if ( uiState.totalSize is NetworkResult.Success){
                            binding.totalordersize.text = uiState.totalSize.data.toString()
                        }

                        if ( uiState.totalSize is NetworkResult.Error){
                            Toast.makeText(requireContext(), uiState.totalSize.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    is BusinessUiState.SalesComparison -> {
                        if (uiState.comparisonResult is NetworkResult.Success){

                            uiState.comparisonResult.data?.let {
                                when(it.status){
                                    SalesStatus.INCREASE -> {
                                        binding.apply {
                                            imgviewpercentageamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.increase))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                           val  txtEdited = SpannableString(fulltext).apply {

                                               setSpan(
                                                   ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.green)),
                                                   0,
                                                   coloredPart.length,
                                                   Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                               )

                                               setSpan(
                                                   StyleSpan(Typeface.BOLD),
                                                   0,
                                                   coloredPart.length,
                                                   Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                               )

                                            }
                                            txtviewsalespercentageamount.text = txtEdited
                                        }
                                    }
                                    SalesStatus.DECREASE -> {
                                        binding.apply {
                                            imgviewpercentageamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.decrease))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.red)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewsalespercentageamount.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_CHANGE ->{
                                        binding.apply {
                                            imgviewpercentageamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.repeaticon))
                                            val coloredPart = it.percentageChange.toString()
                                            val fulltext = "No Change "
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.black)),
                                                    0,
                                                    fulltext.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewsalespercentageamount.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_PRIOR_DATA -> {
                                        binding.apply {
                                            imgviewpercentageamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.dashedline))
                                            txtviewsalespercentageamount.text = " No Prior Data"
                                        }
                                    }
                                }
                            }

                        }
                    }

                    is BusinessUiState.SalesComparisonCancelled -> {
                        if (uiState.comparisonResult is NetworkResult.Success){

                            uiState.comparisonResult.data?.let {
                                when(it.status){
                                    SalesStatus.INCREASE -> {
                                        binding.apply {
                                            imgviewpercentagecancelledamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.increasecancelled))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.red)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewcancelledpercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.DECREASE -> {
                                        binding.apply {
                                            imgviewpercentagecancelledamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.decreasecancelled))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.green)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewcancelledpercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_CHANGE ->{
                                        binding.apply {
                                            imgviewpercentagecancelledamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.repeaticon))
                                            val coloredPart = it.percentageChange.toString()
                                            val fulltext = "No Change "
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.black)),
                                                    0,
                                                    fulltext.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewcancelledpercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_PRIOR_DATA -> {
                                        binding.apply {
                                            imgviewpercentagecancelledamount.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.dashedline))
                                            txtviewcancelledpercentage.text = " No Prior Data"
                                        }
                                    }
                                }
                            }

                        }
                    }
                    is BusinessUiState.SalesComparisonCancelledSize -> {
                        if (uiState.comparisonResult is NetworkResult.Success){

                            uiState.comparisonResult.data?.let {
                                when(it.status){
                                    SalesStatus.INCREASE -> {
                                        binding.apply {
                                            imgviewpercentagecancelled.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.increasecancelled))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.red)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewcancelledsizepercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.DECREASE -> {
                                        binding.apply {
                                            imgviewpercentagecancelled.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.decreasecancelled))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.green)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewcancelledsizepercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_CHANGE ->{
                                        binding.apply {
                                            imgviewpercentagecancelled.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.repeaticon))
                                            val coloredPart = it.percentageChange.toString()
                                            val fulltext = "No Change "
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.black)),
                                                    0,
                                                    fulltext.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewcancelledsizepercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_PRIOR_DATA -> {
                                        binding.apply {
                                            imgviewpercentagecancelled.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.dashedline))
                                            txtviewcancelledsizepercentage.text = " No Prior Data"
                                        }
                                    }
                                }
                            }

                        }
                    }
                    is BusinessUiState.SalesComparisonSize -> {
                        if (uiState.comparisonResult is NetworkResult.Success){

                            uiState.comparisonResult.data?.let {
                                when(it.status){
                                    SalesStatus.INCREASE -> {
                                        binding.apply {
                                            imgviewpercentageorders.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.increase))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.green)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewtotalorderspercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.DECREASE -> {
                                        binding.apply {
                                            imgviewpercentageorders.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.decrease))
                                            val coloredPart = "%.1f%%".format(abs(it.percentageChange))
                                            val remaining = getFilterFillerMessage(uiState.comparisonResult.data.filter)
                                            val fulltext = coloredPart+remaining
                                            val  txtEdited = SpannableString(fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.red)),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                                setSpan(
                                                    StyleSpan(Typeface.BOLD),
                                                    0,
                                                    coloredPart.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewtotalorderspercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_CHANGE ->{
                                        binding.apply {
                                            imgviewpercentageorders.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.repeaticon))
                                            val coloredPart = it.percentageChange.toString()
                                            val fulltext = "No Change"
                                            val  txtEdited = SpannableString( fulltext).apply {

                                                setSpan(
                                                    ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.black)),
                                                    0,
                                                    fulltext.length,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            }
                                            txtviewtotalorderspercentage.text = txtEdited
                                        }
                                    }
                                    SalesStatus.NO_PRIOR_DATA -> {
                                        binding.apply {
                                            imgviewpercentageorders.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.dashedline))
                                            txtviewtotalorderspercentage.text = " No Prior Data"
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun getFilterFillerMessage(filter: DashboardFilter): String {
      return  when(filter){
            is DashboardFilter.Custom ->{
                 " vs Prev. Range"
            }
            DashboardFilter.Month -> {
                " vs Prev. Month"
            }
            DashboardFilter.Today -> {
                " vs Yesterday"
            }
            DashboardFilter.Week -> {
                " vs Prev. Week"
            }
            DashboardFilter.Yesterday -> {
                " vs Yesterday"
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        businessViewModel.resetBusinessFilter()
        business=null
    }

}