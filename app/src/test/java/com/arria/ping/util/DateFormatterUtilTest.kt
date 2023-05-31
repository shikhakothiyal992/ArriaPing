package com.arria.ping.util

import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class DateFormatterUtilTest {
    private val strOverviewData = "Fri Mar 11 00:00:00 GMT+05:30 2022"
    private val strPeriodFromDate = "2022-03-13"
    private val YEAR_PATTERN: Pattern = Pattern.compile("(19|20|21)[0-9][0-9]")
    private val MONTH_PATTERN: Pattern = Pattern.compile("[a-zA-Z]{3}[ /.-][0-9]{4}")
    private val DATE_PATTERN: Pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}")


    @Test
    fun formatDateAction() {
        val resultDate = DateFormatterUtil.formatDateAction(strPeriodFromDate)
        assertThat(resultDate)
    }

    @Test
    fun formatYearActionInvalid() {
        val resultYear = DateFormatterUtil.formatYearAction(strPeriodFromDate)
        assertThat(resultYear).containsMatch(YEAR_PATTERN)
    }

    @Test
    fun formatMonthAction() {
        val resultMoth = DateFormatterUtil.formatMonthAction(strPeriodFromDate)
        if (assertThat(resultMoth).containsMatch(YEAR_PATTERN)
                    .equals(true)
        ) {
            assertThat(resultMoth).containsMatch(MONTH_PATTERN)
        }
    }

    @Test
    fun getFormattedOverviewDate() {
        val resultOverviewDate = DateFormatterUtil.getFormattedOverviewDate(strOverviewData)
        if (assertThat(resultOverviewDate).containsMatch(YEAR_PATTERN)
                    .equals(true)
        ) {
            assertThat(resultOverviewDate).containsMatch(DATE_PATTERN)
        }
    }

    @Test
    fun formatDateInToCustomRange_Input_FromDate_And_ToDate_Expected_Date_In_Custom_Range() {
        val result = DateFormatterUtil.formatDateInToCustomRange("2022-06-01", "2022-06-14")
        val expected = "1 JUN 22 - 14 JUN 22"
        Assert.assertEquals(result, expected)
    }

    @Test
    fun formatDateInToCustomRange_For_Same_InputDates_Expected_Date_Custom_Range(){
        val result = DateFormatterUtil.formatDateInToCustomRange("2022-06-02","2022-06-02")
        val expected = "2 JUN 22 - 2 JUN 22"
        Assert.assertEquals(result, expected)
    }

    @Test
    fun convertLongIntoYearMonthDayDateFormat_Input_LongValue_Expected_Date() {
        val result = DateFormatterUtil.convertLongIntoYearMonthDayDateFormat(1654264800000)
        val expected = "2022-06-04"
        Assert.assertEquals(result, expected)
    }

    @Test
    fun getRemainingDaysOfMonth_Get_Last_Date_Of_Month_And_Subtract_With_Last_Service_Date_Expected_RemainingDay_Value(){
        val result = DateFormatterUtil.getRemainingDays("2022-03-24")
        Assert.assertEquals(result, "7")
    }
    @Test
    fun getRemainingDaysOfMonth_Get_Last_Date_Of_Month_And_Subtract_With_Same_Last_Service_Date_Expected_RemainingDay_Value(){
        val result = DateFormatterUtil.getRemainingDays("2022-03-31")
        Assert.assertEquals(result, "0")
        print(result)
    }

    @Test
    fun getRemainingDaysOfMonth_When_Leap_Year_Get_Last_Date_Of_Month_And_Subtract_With_Last_Service_Date_Expected_RemainingDay_Value(){
        val result = DateFormatterUtil.getRemainingDays("2024-02-24")
        Assert.assertEquals(result, "5")
    }

    @Test
    fun getRemainingDaysOfMonth_When_Normal_Year_Get_Last_Date_Of_Month_And_Subtract_With_Last_Service_Date_Expected_RemainingDay_Value(){
        val result = DateFormatterUtil.getRemainingDays("2022-02-24")
        Assert.assertEquals(result, "4")
    }

}