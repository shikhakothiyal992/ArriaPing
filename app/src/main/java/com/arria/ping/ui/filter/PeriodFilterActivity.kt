package com.arria.ping.ui.filter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.IpConstants
import com.arria.ping.util.StorePrefData
import kotlinx.android.synthetic.main.activity_period_filter.*
import kotlinx.android.synthetic.main.common_header_filter_initial_screen.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*


class PeriodFilterActivity : AppCompatActivity(), View.OnClickListener {
    var selectedDate = 0
    var apiArgument = ""
    var startDateValueDate = ""
    var endDateValueDate = ""
    var action = ""
    var filterData = ""
    var isAnySelectionChanged = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    lateinit var  typefaceBold :Typeface
    lateinit var  typefaceRegular :Typeface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_period_filter)
        Logger.info("Period Filter Screen","Period Filter")
    }
    override fun onResume() {
        super.onResume()
        setData()
    }
    @SuppressLint("NewApi")
    private fun setData() {
        typefaceBold = resources.getFont(R.font.sf_ui_text_bold)
        typefaceRegular = resources.getFont(R.font.sf_ui_text_regular)
        reset_text.visibility = View.INVISIBLE
        today_select.setOnClickListener(this)
        yesterday_select.setOnClickListener(this)
        this_week_select.setOnClickListener(this)
        last_week_select.setOnClickListener(this)
        this_month_select.setOnClickListener(this)
        last_month_select.setOnClickListener(this)
        this_year_select.setOnClickListener(this)
        last_year_select.setOnClickListener(this)
        last_seven_days_select.setOnClickListener(this)
        last_twenty_eight_days_select.setOnClickListener(this)
        custom_range_select.setOnClickListener(this)
        btn_apply.setOnClickListener(this)
        cross_filter_img_layout.setOnClickListener(this)
        reset_text_layout.setOnClickListener(this)
        checkSelectedRadioButton()
    }


    override fun onBackPressed() {
        Logger.info("Back-pressed","Period Filter")
        resetAllFilter()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.today_select -> {
                Logger.info("Today Filter selected","Period Filter")
                setFontOnSelection(today_select)
                setDataAfterSelection(IpConstants.Today, 0, IpConstants.Today)
            }
            R.id.yesterday_select -> {
                Logger.info("Last Service Filter selected","Period Filter")
                setFontOnSelection(yesterday_select)
                setDataAfterSelection(IpConstants.Yesterday, 1, IpConstants.Yesterday)
            }
            R.id.this_week_select -> {
                Logger.info("This Week Period Filter selected","Period Filter")
                setFontOnSelection(this_week_select)
                setDataAfterSelection(IpConstants.ThisWeek, 7, IpConstants.date)

            }
            R.id.last_week_select -> {
                Logger.info("Last Week Period Filter selected","Period Filter")
                setFontOnSelection(last_week_select)
                setDataAfterSelection(IpConstants.LastWeek, 7, IpConstants.date)

            }
            R.id.this_month_select -> {
                Logger.info("This Moth Period Filter selected","Period Filter")
                setFontOnSelection(this_month_select)
                setDataAfterSelection(IpConstants.ThisMonth, 30, IpConstants.date)

            }
            R.id.last_month_select -> {
                Logger.info("Last Month Period Filter selected","Period Filter")
                setFontOnSelection(last_month_select)
                apiArgument = IpConstants.rangeFrom
                setDataAfterSelection(IpConstants.LastMonth, 30, IpConstants.date)

            }
            R.id.this_year_select -> {
                Logger.info("This Year Period Filter selected","Period Filter")
                setFontOnSelection(this_year_select)
                apiArgument = IpConstants.rangeFrom
                setDataAfterSelection(IpConstants.ThisYear, 365, IpConstants.date)

            }
            R.id.last_year_select -> {
                Logger.info("Last Year Period Filter selected","Period Filter")
                setFontOnSelection(last_year_select)
                apiArgument = IpConstants.rangeFrom
                setDataAfterSelection(IpConstants.LastYear, 365, IpConstants.date)

            }
            R.id.last_seven_days_select -> {
                Logger.info("Last 7 Days Period Filter selected","Period Filter")
                setFontOnSelection(last_seven_days_select)
                setDataAfterSelection(IpConstants.Last7Days, 7, IpConstants.rollingSevenDays)

            }
            R.id.last_twenty_eight_days_select -> {
                Logger.info("Last 20 Days Period Filter selected","Period Filter")
                setFontOnSelection(last_twenty_eight_days_select)
                setDataAfterSelection(
                        IpConstants.Last28Days,
                        28,
                        IpConstants.rollingTwentyEightDays
                )

            }
            R.id.custom_range_select -> {
                Logger.info("Custom Range Period Filter selected","Period Filter")
                setFontOnSelection(custom_range_select)
                StorePrefData.isSelectedPeriod = getString(R.string.custom_text)
                callCustomRangeCalendarFilterActivity()
            }
            R.id.btn_apply -> {
                Logger.info("Period Filter Applied","Period Filter")
                callFilterActivity()
            }
            R.id.cross_filter_img_layout -> {
                Logger.info("Period Filter Cancelled","Period Filter")
                resetAllFilter()
                finish()
            }
            R.id.reset_text_layout -> {
                Logger.info("Period Filter Reset","Period Filter")
                resetAllFilter()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataAfterSelection(
        selectedValue: String,
        selectedDateRange: Int,
        apiArgumentSelected: String,
    ) {
        StorePrefData.isCalendarSelected = false
        StorePrefData.isPeriodSelected = selectedValue
        apiArgument = apiArgumentSelected
        selectedDate = selectedDateRange
        custom_range_select.text = getString(R.string.custom_range_text)
        when (selectedValue) {
            IpConstants.ThisWeek -> {
                StorePrefData.isSelectedPeriod = getString(R.string.wtd_text)
                val firstDayOfWeek = WeekFields.of(Locale.ENGLISH).firstDayOfWeek + 1
                val lastDayOfWeek =
                    DayOfWeek.of(((firstDayOfWeek.value + 5) % DayOfWeek.values().size) + 1)
                LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                LocalDate.now().with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
                startDateValueDate =
                    LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).toString()

                StorePrefData.isSelectedDate = DateFormatterUtil.formatDateAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.THIS_WEEK
            }
            IpConstants.LastWeek -> {
                val c = Calendar.getInstance()
                StorePrefData.isSelectedPeriod = getString(R.string.last_week_text)

                c[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
                c.add(Calendar.DATE, -1 * 7)
                val listDate = ArrayList<String>()
                for (i in 0..6) {
                    listDate.add(dateFormat.format(c.time))
                    c.add(Calendar.DAY_OF_MONTH, 1)
                }
                startDateValueDate = listDate[0]
                endDateValueDate = listDate[6]// monday to sunday
                StorePrefData.isSelectedDate = DateFormatterUtil.formatDateAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.LAST_WEEK
            }
            IpConstants.ThisMonth -> {
                val monthBegin = LocalDate.now().withDayOfMonth(1)
                StorePrefData.isSelectedPeriod = getString(R.string.mtd_text)
                startDateValueDate = monthBegin.toString()
                StorePrefData.isSelectedDate = DateFormatterUtil.formatMonthAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.THIS_MONTH
            }
            IpConstants.LastMonth -> {
                val aCalendar = Calendar.getInstance()
                StorePrefData.isSelectedPeriod = getString(R.string.last_month_text)

                aCalendar.add(Calendar.MONTH, -1)
                aCalendar[Calendar.DATE] = 1
                val firstDateOfPreviousMonth = dateFormat.format(aCalendar.time)

                aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val lastDateOfPreviousMonth = dateFormat.format(aCalendar.time)

                startDateValueDate = firstDateOfPreviousMonth
                endDateValueDate = lastDateOfPreviousMonth
                StorePrefData.isSelectedDate = DateFormatterUtil.formatMonthAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.LAST_MONTH
            }
            IpConstants.ThisYear -> {
                val now = LocalDate.now() // 2015-11-23
                StorePrefData.isSelectedPeriod = getString(R.string.ytd_text)
                val firstDay = now.with(TemporalAdjusters.firstDayOfYear()) // 2015-01-01
                val lastDay = now.with(TemporalAdjusters.lastDayOfYear()) // 2015-12-31
                startDateValueDate = firstDay.toString()
                endDateValueDate = lastDay.toString()
                StorePrefData.isSelectedDate = DateFormatterUtil.formatYearAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.THIS_YEAR
            }
            IpConstants.LastYear -> {
                val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                StorePrefData.isSelectedPeriod = getString(R.string.last_year_text)
                val calendar = Calendar.getInstance()
                calendar.time = Date()
                calendar.add(Calendar.YEAR, -1)
                calendar[Calendar.DAY_OF_YEAR] = 1
                val yearFirstDay = calendar.time

                calendar[Calendar.MONTH] = 11
                calendar[Calendar.DAY_OF_MONTH] = 31
                val yearLastDay = calendar.time

                val dateStart = formatter.parse(yearFirstDay.toString())
                val dateEnd = formatter.parse(yearLastDay.toString())

                startDateValueDate = dateFormat.format(dateStart!!) //Wed Jan 01 23:44:25 GMT+05:30 2020//Thu Dec 31 23:47:09 GMT+05:30 2020
                endDateValueDate = dateFormat.format(dateEnd!!)
                StorePrefData.isSelectedDate = DateFormatterUtil.formatYearAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.LAST_YEAR
            }
            IpConstants.Last7Days -> {
                StorePrefData.isSelectedPeriod = getString(R.string.last_seven_days_text)
                val currentCal = Calendar.getInstance()
                val currentDate = dateFormat.format(currentCal.time)
                currentCal.add(Calendar.DATE, -7)
                StorePrefData.isSelectedDate = startDateValueDate
                val toDate: String? = dateFormat.format(currentCal.time)
                startDateValueDate = currentDate.toString()
                endDateValueDate = toDate.toString()
                StorePrefData.isSelectedDate = DateFormatterUtil.formatDateAction(currentDate)//  new change show current date
                StorePrefData.filterType = IpConstants.LAST_7_DAYS
            }
            IpConstants.Last28Days -> {
                StorePrefData.isSelectedPeriod = getString(R.string.last_twenty_eight_days)
                val currentCal = Calendar.getInstance()
                val currentDate = dateFormat.format(currentCal.time)
                currentCal.add(Calendar.DATE, -28)
                val toDate: String? = dateFormat.format(currentCal.time)
                startDateValueDate = currentDate.toString()
                endDateValueDate = toDate.toString()
                StorePrefData.isSelectedDate = DateFormatterUtil.formatDateAction(currentDate)// new change show current date
                StorePrefData.filterType = IpConstants.LAST_28_DAYS
            }
            IpConstants.Today -> {
                StorePrefData.isSelectedPeriod = selectedValue
                val currentCal = Calendar.getInstance()
                val currentDate = dateFormat.format(currentCal.time)
                val toDate: String? = dateFormat.format(currentCal.time)
                startDateValueDate = currentDate.toString()
                endDateValueDate = toDate.toString()
                StorePrefData.startDateValue = startDateValueDate
                StorePrefData.isSelectedDate = DateFormatterUtil.formatDateAction(startDateValueDate)
                StorePrefData.endDateValue = endDateValueDate
            }
            IpConstants.Yesterday -> {
                StorePrefData.isSelectedPeriod = selectedValue
                val currentCal = Calendar.getInstance()
                val currentDate = dateFormat.format(currentCal.time)
                currentCal.add(Calendar.DATE, -1)
                val toDate: String? = dateFormat.format(currentCal.time)
                startDateValueDate = toDate.toString()
                endDateValueDate = currentDate.toString()
                StorePrefData.isSelectedDate = DateFormatterUtil.formatDateAction(startDateValueDate)
                StorePrefData.filterType = IpConstants.LAST_SERVICE
            }
        }


    }

    private fun callFilterActivity() {
        finish()
    }

    private fun callCustomRangeCalendarFilterActivity() {
        val intent = Intent(this, CustomRangeCalendarFilterActivity::class.java)
        startActivity(intent)
    }

    private fun checkSelectedRadioButton() {
        if (StorePrefData.isPeriodSelected.isNotEmpty()) {
            //custom_range_select.text = getString(R.string.custom_range_text)
            if (StorePrefData.isPeriodSelected == IpConstants.Today) {
                today_select.isChecked = true
                today_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.Yesterday) {
                yesterday_select.isChecked = true
                yesterday_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.ThisWeek) {
                this_week_select.isChecked = true
                this_week_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.ThisMonth) {
                this_month_select.isChecked = true
                this_month_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.LastMonth) {
                last_month_select.isChecked = true
                last_month_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.ThisYear) {
                this_year_select.isChecked = true
                this_year_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.LastYear) {
                last_year_select.isChecked = true
                last_year_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.Last7Days) {
                last_seven_days_select.isChecked = true
                last_seven_days_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.Last28Days) {
                last_twenty_eight_days_select.isChecked = true
                last_twenty_eight_days_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            } else if (StorePrefData.isPeriodSelected == IpConstants.LastWeek) {
                last_week_select.isChecked = true
                last_week_select.typeface = typefaceBold
                custom_range_select.typeface = typefaceRegular
            }else {
                custom_range_select.isChecked = true
                custom_range_select.typeface = typefaceBold
                if (StorePrefData.isCalendarSelected) {
                    custom_range_select.text = StorePrefData.isPeriodSelected
                } else {
                    custom_range_select.text = IpConstants.CustomRange
                }

            }
        } else {
            if (StorePrefData.role == getString(R.string.gm_text)) {
                today_select.isChecked = true
                today_select.typeface = typefaceBold
                StorePrefData.isPeriodSelected = IpConstants.Today
                StorePrefData.filterType = IpConstants.Today
            } else {
                yesterday_select.isChecked = true
                yesterday_select.typeface = typefaceBold
                StorePrefData.isPeriodSelected = IpConstants.Yesterday
                StorePrefData.filterType = IpConstants.LAST_SERVICE
            }
        }
    }
    private fun resetAllFilter(){
        /*if (StorePrefData.role == getString(R.string.gm_text)) {
            // uncomment after demo
            *//* today_select.isChecked = true
             StorePrefData.isPeriodSelected = getString(R.string.today_text)*//*
            yesterday_select.isChecked = true
            StorePrefData.isPeriodSelected = getString(R.string.yesterday_text)
        } else {
            yesterday_select.isChecked = true
            StorePrefData.isPeriodSelected = getString(R.string.yesterday_text)
        }*/
    }
    private fun setFontOnSelection(selectedRadioButton: RadioButton) {
        today_select.typeface = typefaceRegular
        yesterday_select.typeface = typefaceRegular
        this_week_select.typeface = typefaceRegular
        last_week_select.typeface = typefaceRegular
        this_month_select.typeface = typefaceRegular
        last_month_select.typeface = typefaceRegular
        this_year_select.typeface = typefaceRegular
        last_year_select.typeface = typefaceRegular
        last_seven_days_select.typeface = typefaceRegular
        last_twenty_eight_days_select.typeface = typefaceRegular
        custom_range_select.typeface = typefaceRegular
        selectedRadioButton.typeface = typefaceBold
    }

}
