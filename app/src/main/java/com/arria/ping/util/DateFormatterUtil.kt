package com.arria.ping.util

import android.os.Build
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import com.arria.ping.log.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object DateFormatterUtil {

    fun currentDate(): String {
        try {
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            return df.format(c.time).uppercase(Locale.getDefault())
        } catch (e: Exception) {
            Logger.error(e.message.toString(), "DateFormatterUtils- currentDate")
            e.printStackTrace()
            return ""
        }
    }

    fun previousDate(): String {
        try {
            val c = Calendar.getInstance()
            c.add(Calendar.DATE, -1)
            val df = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            return df.format(c.time).uppercase(Locale.getDefault())
        } catch (e: Exception) {
            Logger.error(e.message.toString(), "DateFormatterUtils- previousDate")
            e.printStackTrace()
            return ""
        }
    }

    fun formatDateForAction(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        try {
            val date1 = format.parse(dateString)
            val spf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            return spf.format(date1).uppercase(Locale.getDefault())
        } catch (e: ParseException) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatDateForAction")
            e.printStackTrace()
        }
        return ""

    }

    fun formatDateAction(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date1 = format.parse(dateString)
            val spf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            return spf.format(date1).uppercase(Locale.getDefault())
        } catch (e: ParseException) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatDateAction")
            e.printStackTrace()
        }
        return ""

    }

    fun formatMonthAction(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date1 = format.parse(dateString)
            val spf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val date = spf.format(date1)
            return date.uppercase(Locale.getDefault())
        } catch (e: ParseException) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatMonthAction")
            e.printStackTrace()
        }
        return ""

    }

    fun formatYearAction(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date1 = format.parse(dateString)
            val spf = SimpleDateFormat("yyyy", Locale.getDefault())
            return spf.format(date1)
        } catch (e: ParseException) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatMonthAction")
            e.printStackTrace()
        }
        return ""

    }
    fun formatDateForPastAction(dateString: String): String {
        try {

            val dtStart = Calendar.getInstance()
            val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
            val dateCurrent = format.parse(dtStart.time.toString())

            val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dateTime: String = dateFormat.format(dateCurrent!!)
            val dateTimeFinal = dateFormat.parse(dateTime)

            val finalDateToDisplay: String
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
            val output = SimpleDateFormat("dd MMM", Locale.getDefault())
            output.timeZone = TimeZone.getDefault()
            val date = dateFormatter.parse(dateString)
            val formatted = output.format(date!!)
            val formattedFinal = output.parse(formatted)

            // Get time from date
            val timeFormatter = SimpleDateFormat("h aa", Locale.getDefault())
            val displayValue = timeFormatter.format(date)

            if (formattedFinal.equals(dateTimeFinal)) {
                finalDateToDisplay = "TODAY"
            } else if (formattedFinal.before(dateTimeFinal)) {
                finalDateToDisplay = "YESTERDAY"
            } else {
                finalDateToDisplay = formatted.uppercase(Locale.getDefault()) + "," + displayValue
            }
            return finalDateToDisplay
        } catch (e: Exception) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatDateForPastAction")
            return ""
        }
    }

    fun formatDateForCurrentCheckIn(dateString: String): String {
        try {
            val dtStart = Calendar.getInstance()
            val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
            val dateCurrent = format.parse(dtStart.time.toString())

            val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dateTime: String = dateFormat.format(dateCurrent!!)
            val dateTimeFinal = dateFormat.parse(dateTime)


            val finalDateToDisplay: String
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
            val output = SimpleDateFormat("dd MMM", Locale.getDefault())
            val date = dateFormatter.parse(dateString)
            val formatted = output.format(date)
            val formattedFinal = output.parse(formatted)
            // Get time from date
            val timeFormatter = SimpleDateFormat("h aa", Locale.getDefault())
            timeFormatter.timeZone = TimeZone.getDefault()
            val displayValue = timeFormatter.format(date)

            finalDateToDisplay = when {
                formattedFinal.equals(dateTimeFinal) -> {
                    "TODAY"
                }
                formattedFinal.before(dateTimeFinal) -> {
                    "YESTERDAY"
                }
                else -> {
                    displayValue
                }
            }
            return finalDateToDisplay
        } catch (e: Exception) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatDateForCurrentCheckIn")
            return ""
        }

    }

    fun getFormattedOverviewDate(selectedDate: String): String {
        val dateCEOPeriodKpi: Date?
        val formatterCEOPeriodKpi = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        try {
            dateCEOPeriodKpi = formatterCEOPeriodKpi.parse(selectedDate)
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateCEOPeriodKpi!!)
        } catch (e: ParseException) {
            Logger.error(e.message.toString(), "DateFormatterUtils- getFormattedOverviewDate")
            e.printStackTrace()
        }
        return ""
    }


    fun setCalendar(calendarView: CalendarView) {
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        calendarView.date = cal.timeInMillis
        calendarView.maxDate = System.currentTimeMillis()
        calendarView.minDate = cal.timeInMillis
        calendarView.isEnabled = false

    }

    fun formatDateInToCustomRange(startDate: String, endDate: String): String {
        var formattedCustomRangeDate = ""
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val formattedFromDate = format.parse(startDate)
            val formattedToDate = format.parse(endDate)
            val customRangeSimpleDateFormatter = SimpleDateFormat("d MMM yy", Locale.getDefault())
            val fromDate =  customRangeSimpleDateFormatter.format(formattedFromDate).uppercase(Locale.getDefault())
            val toDate =  customRangeSimpleDateFormatter.format(formattedToDate).uppercase(Locale.getDefault())
            formattedCustomRangeDate = "$fromDate - $toDate"
            return formattedCustomRangeDate
        } catch (e: ParseException) {
            Logger.error(e.message.toString(), "DateFormatterUtils- formatDateInToCustomRange")
            e.printStackTrace()
        }
        return formattedCustomRangeDate
    }


    fun convertLongIntoYearMonthDayDateFormat(date: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
     }

    fun currentCalendarDateForBonus(): String {
        val bonusDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val bonusCalendar: Calendar = Calendar.getInstance()
        return bonusDateFormat.format(bonusCalendar.time)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRemainingDays(dayOfLastServiceDate: String) : String {
        val formatter = SimpleDateFormat("yyyy-MM-d", Locale.getDefault())
        val lastDateOfLastServiceMonth = SimpleDateFormat("yyyy-MM-d", Locale.getDefault()).parse(dayOfLastServiceDate)
        var lastDateOfMonth: LocalDate = LocalDate.parse(
                formatter.format(lastDateOfLastServiceMonth!!), DateTimeFormatter.ofPattern
        ("yyyy-MM-d")
        )
        lastDateOfMonth = lastDateOfMonth.withDayOfMonth(
                lastDateOfMonth.month.length(lastDateOfMonth.isLeapYear)
        )
        val lastDateOfTheCurrentMonth: Date = Date.from(
                lastDateOfMonth.atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        )
        val lastServiceDate: Date = formatter.parse(dayOfLastServiceDate)
        val diffInTimeUnit = abs(lastDateOfTheCurrentMonth.time - lastServiceDate.time)
        val diff: Long = TimeUnit.DAYS.convert(diffInTimeUnit, TimeUnit.MILLISECONDS)
        return diff.toString()

    }
}

