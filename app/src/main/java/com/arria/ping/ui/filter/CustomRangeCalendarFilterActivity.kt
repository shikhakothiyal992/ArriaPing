package com.arria.ping.ui.filter

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.arria.ping.R
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.DialogUtil
import com.arria.ping.util.IpConstants
import com.arria.ping.util.StorePrefData
import kotlinx.android.synthetic.main.activity_custom_range_filter.*
import kotlinx.android.synthetic.main.common_header_filter.*
import kotlinx.android.synthetic.main.common_header_filter_initial_screen.*
import kotlinx.android.synthetic.main.customize_calendar_header.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomRangeCalendarFilterActivity : AppCompatActivity() {

    var startDateValueDate = ""
    var endDateValueDate = ""
    var selectedStartDate = ""
    var selectedEndDate = ""
    var apiArgument = ""
    var selectedCalendarStartDate: Calendar? = null
    var selectedCalendarEndDate: Calendar? = null
    private lateinit var userSelectedStartDateCalendar: Calendar
    private lateinit var userSelectedEndDateCalendar: Calendar
    private var userSelectedStartDate: Date? = null
    private var userSelectedEndDate: Date? = null
    private var dialog: Dialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_range_filter)
        setData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData() {

        val typeface = resources.getFont(R.font.sf_ui_text_regular)
        calendar_custom_range.setFonts(typeface)
        calendar_custom_range.setFonts(ResourcesCompat.getFont(this, R.font.sf_ui_text_regular)!!)

        val startMonth = Calendar.getInstance()
        startMonth.add(Calendar.MONTH, -12)
        val endMonth = Calendar.getInstance()
        calendar_custom_range.setVisibleMonthRange(startMonth, endMonth)

        val startDateSelectable = startMonth.clone() as Calendar
        startDateSelectable.add(Calendar.DATE, 0)
        val endDateSelectable = endMonth.clone() as Calendar
        endDateSelectable.add(Calendar.DATE, 0)
        calendar_custom_range.setSelectableDateRange(startDateSelectable, endDateSelectable)

        val current = Calendar.getInstance()
        calendar_custom_range.setCurrentMonth(current)

        val startSelectionDate = Calendar.getInstance()
        startSelectionDate.add(Calendar.HOUR, -1)



        calendar_custom_range.setSelectedDateRange(startSelectionDate, endMonth)

        val dateFormatter = SimpleDateFormat("yyyy-MM-d", Locale.getDefault())
        userSelectedEndDateCalendar = Calendar.getInstance()
        userSelectedStartDateCalendar = Calendar.getInstance()

        if (StorePrefData.startDateValue.isNotEmpty() && StorePrefData.endDateValue.isNotEmpty() && (StorePrefData
                    .startDateValue == StorePrefData.endDateValue)
        ) {
            userSelectedStartDate = dateFormatter.parse(StorePrefData.startDateValue)
            userSelectedEndDate = dateFormatter.parse(StorePrefData.endDateValue)
            selectedStartDate = StorePrefData.startDateValue
            selectedEndDate = StorePrefData.endDateValue
        } else if (StorePrefData.startDateValue.isNotEmpty() && StorePrefData.endDateValue.isNotEmpty() && (StorePrefData
                    .startDateValue != StorePrefData.endDateValue)
        ) {

            userSelectedStartDate = dateFormatter.parse(StorePrefData.startDateValue)
            userSelectedEndDate = dateFormatter.parse(StorePrefData.endDateValue)
            selectedStartDate = StorePrefData.startDateValue
            selectedEndDate = StorePrefData.endDateValue
        }

        if (userSelectedStartDate != null && userSelectedEndDate != null) {
            userSelectedStartDateCalendar.time = userSelectedStartDate!!
            userSelectedEndDateCalendar.time = userSelectedEndDate!!
            selectedCalendarStartDate = userSelectedStartDateCalendar
            selectedCalendarEndDate = userSelectedEndDateCalendar
            calendar_custom_range.setSelectedDateRange(userSelectedStartDateCalendar, userSelectedEndDateCalendar)
        } else if (userSelectedStartDate != null && userSelectedEndDate == null) {
            userSelectedStartDateCalendar.time = userSelectedStartDate!!
            selectedCalendarStartDate = userSelectedStartDateCalendar
            selectedCalendarEndDate = userSelectedStartDateCalendar
            calendar_custom_range.setSelectedDateRange(userSelectedStartDateCalendar, userSelectedStartDateCalendar)
        }
        calendar_custom_range.setCalendarListener(object : CalendarListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDateRangeSelected(
                    startDate: Calendar,
                    endDate: Calendar
            ) {
                if (startDate.time.after(Date()) || endDate.time.after(Date())) {
                    Toast.makeText(
                            this@CustomRangeCalendarFilterActivity,
                            "Please select past date",
                            Toast.LENGTH_SHORT
                    )
                            .show()
                } else {
                    apiArgument = IpConstants.rangeFrom
                    selectedCalendarStartDate = startDate
                    selectedCalendarEndDate = endDate
                    getSelectedDatesFromCalendar(
                            startDate.time.time,
                            endDate.time.time
                    )
                }

            }

            override fun onFirstDateSelected(startDate: Calendar) {
                if (startDate.time.after(Date())) {
                    Toast.makeText(
                            this@CustomRangeCalendarFilterActivity,
                            "Please select past date",
                            Toast.LENGTH_SHORT
                    )
                            .show()
                }  else {
                    if (selectedCalendarStartDate == null && selectedCalendarEndDate == null) {
                        selectedCalendarStartDate = startDate
                        calendar_custom_range.setSelectedDateRange(selectedCalendarStartDate!!, selectedCalendarStartDate!!)
                        getSelectedDatesFromCalendar(
                                selectedCalendarStartDate!!.time.time,
                                selectedCalendarStartDate!!.time.time
                        )
                    }
                    else if (selectedCalendarStartDate == startDate && selectedCalendarEndDate != null) {
                        calendar_custom_range.setSelectedDateRange(selectedCalendarEndDate!!, selectedCalendarEndDate!!)
                        selectedCalendarStartDate = null
                        getSelectedDatesFromCalendar(
                                selectedCalendarEndDate!!.time.time,
                                selectedCalendarEndDate!!.time.time
                        )
                    }
                    else if (selectedCalendarEndDate == startDate && selectedCalendarStartDate != null) {
                        calendar_custom_range.setSelectedDateRange(selectedCalendarStartDate!!, selectedCalendarStartDate!!)
                        selectedCalendarEndDate = null
                        getSelectedDatesFromCalendar(
                                selectedCalendarStartDate!!.time.time,
                                selectedCalendarStartDate!!.time.time
                        )

                    }
                    else if (selectedCalendarStartDate != startDate && selectedCalendarStartDate != null && selectedCalendarEndDate == null) {
                        if (selectedCalendarStartDate!! > startDate) {
                            selectedCalendarEndDate = selectedCalendarStartDate
                            selectedCalendarStartDate = startDate
                            calendar_custom_range.setSelectedDateRange(
                                    selectedCalendarStartDate!!,
                                    selectedCalendarEndDate!!
                            )
                        } else {
                            selectedCalendarEndDate = startDate
                            calendar_custom_range.setSelectedDateRange(
                                    selectedCalendarStartDate!!,
                                    selectedCalendarEndDate!!
                            )
                        }
                        getSelectedDatesFromCalendar(
                                selectedCalendarStartDate!!.time.time,
                                selectedCalendarEndDate!!.time.time
                        )
                    }
                    else if (selectedCalendarEndDate != startDate && selectedCalendarEndDate != null && selectedCalendarStartDate == null) {
                        selectedCalendarStartDate = startDate
                        if (selectedCalendarStartDate!! > selectedCalendarEndDate!!) {
                            selectedCalendarStartDate = selectedCalendarEndDate
                            selectedCalendarEndDate = startDate
                            calendar_custom_range.setSelectedDateRange(
                                    selectedCalendarStartDate!!,
                                    selectedCalendarEndDate!!
                            )
                        } else {
                            calendar_custom_range.setSelectedDateRange(
                                    selectedCalendarStartDate!!,
                                    selectedCalendarEndDate!!
                            )
                        }
                        getSelectedDatesFromCalendar(
                                selectedCalendarStartDate!!.time.time,
                                selectedCalendarEndDate!!.time.time
                        )
                    }
                    else if (selectedCalendarStartDate != null && selectedCalendarEndDate != null && (selectedCalendarStartDate != startDate) && (selectedCalendarEndDate != startDate)) {
                        selectedCalendarStartDate = startDate
                        selectedCalendarEndDate = null
                        calendar_custom_range.setSelectedDateRange(
                                selectedCalendarStartDate!!,
                                selectedCalendarStartDate!!
                        )

                        getSelectedDatesFromCalendar(
                                startDate.time.time,
                                startDate.time.time
                        )

                    }
                    else { getSelectedDatesFromCalendar(startDate.time.time, startDate.time.time) }
                }
            }
        })
        custom_calendar_header_layout.custom_calendar_cancel_button.setOnClickListener {
            finish()
        }
        btn_apply.setOnClickListener {

            if (selectedStartDate.isNotEmpty() && selectedEndDate.isNotEmpty()) {
                setCalendarPreferencesVales(selectedStartDate, selectedEndDate)
                finish()
            } else {
                showCalendarDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSelectedDatesFromCalendar(
            startDateValue: Long,
            endDateValue: Long
    ) {
        selectedStartDate = DateFormatterUtil.convertLongIntoYearMonthDayDateFormat(startDateValue)
        selectedEndDate = DateFormatterUtil.convertLongIntoYearMonthDayDateFormat(endDateValue)
    }

    fun setCalendarPreferencesVales(
            startDate: String,
            endDate: String
    ) {
        val dateStart = SimpleDateFormat("yyyy-MM-d", Locale.getDefault()).parse(startDate)
        val dateEnd = SimpleDateFormat("yyyy-MM-d", Locale.getDefault()).parse(endDate)
        val dayMonthYearFormatter = SimpleDateFormat("d MMM yy", Locale.getDefault())

            StorePrefData.isPeriodSelected = dayMonthYearFormatter.format(dateStart!!)
                    .toString()
                    .uppercase() + "-" + " " + dayMonthYearFormatter.format(
                    dateEnd!!
            )
                    .toString()
                    .uppercase()

        StorePrefData.startDateValue = startDate
        StorePrefData.endDateValue = endDate
        StorePrefData.isCalendarSelected = true
        StorePrefData.isSelectedDate = StorePrefData.isPeriodSelected
        StorePrefData.filterType = IpConstants.CUSTOM_RANGE
    }


    override fun onBackPressed() {
        finish()
    }

    fun showCalendarDialog() {
        dialog = DialogUtil.getErrorDialogAccessDialog(
                this,
                "",
                "Please select a date range",
                getString(R.string.ok_text),
                {
                    dialog?.dismiss()
                    dialog = null

                },
                null,
                null
        )
        dialog?.show()
    }


}
