package com.arria.ping.util

import com.arria.ping.kpi.type.FilterType

object EnumMapperUtil {

    private var result: FilterType = FilterType.LAST_SERVICE
    private var filterSelectedDate: String = ""

    fun getFilterTypeENUM(filterType: String): FilterType {

        when (filterType) {
            FilterType.THIS_MONTH.rawValue -> result = FilterType.THIS_MONTH
            FilterType.THIS_WEEK.rawValue -> result = FilterType.THIS_WEEK
            FilterType.THIS_YEAR.rawValue -> result = FilterType.THIS_YEAR
            FilterType.LAST_MONTH.rawValue -> result = FilterType.LAST_MONTH
            FilterType.LAST_YEAR.rawValue -> result = FilterType.LAST_YEAR
            FilterType.LAST_WEEK.rawValue -> result = FilterType.LAST_WEEK
            FilterType.LAST_3_DAYS.rawValue -> result = FilterType.LAST_3_DAYS
            FilterType.LAST_7_DAYS.rawValue -> result = FilterType.LAST_7_DAYS
            FilterType.LAST_28_DAYS.rawValue -> result = FilterType.LAST_28_DAYS
            FilterType.LAST_SERVICE.rawValue -> result = FilterType.LAST_SERVICE
            FilterType.CUSTOM_RANGE.rawValue -> result = FilterType.CUSTOM_RANGE

            else -> result = FilterType.LAST_SERVICE
        }
        return result

    }

    fun getSelectedDate(
            periodFrom: String,
            periodTo: String,
            filterType: FilterType
    ): String {

        when (filterType) {

            FilterType.THIS_WEEK -> filterSelectedDate = DateFormatterUtil.formatDateAction(periodFrom)
            FilterType.LAST_WEEK -> filterSelectedDate = DateFormatterUtil.formatDateAction(periodFrom)

            FilterType.THIS_MONTH -> filterSelectedDate = DateFormatterUtil.formatMonthAction(periodFrom)
            FilterType.LAST_MONTH -> filterSelectedDate = DateFormatterUtil.formatMonthAction(periodFrom)

            FilterType.THIS_YEAR -> filterSelectedDate = DateFormatterUtil.formatYearAction(periodFrom)
            FilterType.LAST_YEAR -> filterSelectedDate = DateFormatterUtil.formatYearAction(periodFrom)

            FilterType.LAST_3_DAYS -> filterSelectedDate = DateFormatterUtil.formatDateAction(periodFrom)
            FilterType.LAST_7_DAYS -> filterSelectedDate = DateFormatterUtil.formatDateAction(periodFrom)
            FilterType.LAST_28_DAYS -> filterSelectedDate = DateFormatterUtil.formatDateAction(periodFrom)

            FilterType.CUSTOM_RANGE -> filterSelectedDate = DateFormatterUtil.formatDateInToCustomRange(periodFrom, periodTo)

            else -> filterSelectedDate = DateFormatterUtil.formatDateAction(periodFrom)
        }
        return filterSelectedDate
    }
}