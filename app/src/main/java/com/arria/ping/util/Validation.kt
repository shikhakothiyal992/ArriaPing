package com.arria.ping.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.arria.ping.R
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.ui.filter.FilterActivity
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs


class Validation {
    lateinit var  typefaceBold :Typeface
    fun showMessageToast(context: Context, msg: String) {
        Toast.makeText(
            context, msg,
            Toast.LENGTH_SHORT
        )
            .show()
    }



    fun isEmailValid(email: String): Boolean {
        var isValid = false
        val expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }

       fun dollarFormatting(valueString: Double?): String {
           val usa = Locale("en", "US")
           val dollarFormat: NumberFormat = NumberFormat.getInstance(usa)
           return dollarFormat.format(valueString)
    }

    fun ignoreZeroAfterDecimal(value: Double?): String {
        val format = DecimalFormat("#,###.##")
        return format.format(value)
    }

    fun openFilter(context: Context) {
        StorePrefData.isFromAction = true
        val intent = Intent(context, FilterActivity::class.java)
        context.startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateFilterKPI(
        activity: FragmentActivity,
        dbHelper: DatabaseHelperImpl,
        textView: TextView,
        valTexT: String,
    ) {
        activity.lifecycleScope.launch {

            val listStore = dbHelper.getAllSelectedStoreList(true)

            val listSuperVisor = dbHelper.getAllSelectedSuperVisorName(true)

            val listArea = dbHelper.getAllSelectedAreaName(true)

            val listState = dbHelper.getAllSelectedStateName(true)

                if (StorePrefData.isStoreSelected) {
                if (StorePrefData.isSupervisorSelected) {
                    if (StorePrefData.isStateSelected) {
                        if (StorePrefData.isAreaSelected) {
                            
                            if (StorePrefData.whichBottomNavigationClicked == activity.getString(R.string.title_phone) && StorePrefData.role == activity.getString(
                                    R.string.gm_text)
                            ) {
                                highlightTextData(activity,
                                                  StorePrefData.StoreIdFromLogin,
                                                  valTexT,
                                                  textView)

                            } else {
                                if (listStore.size > 2 && !StorePrefData.isStoreSelected) {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                } else if (listStore.size in 1..2) {
                                    
                                    highlightTextData(activity, listStore.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "), valTexT, textView)
                                }else{
                                    if (listStore.size > 2 || listArea.size > 2 || listState.size > 2 || listSuperVisor.size > 2){
                                        highlightTextData(activity,
                                            activity.getString(R.string.filter_store),
                                            valTexT,
                                            textView)
                                    }else{
                                        highlightTextData(activity,
                                            activity.getString(R.string.all_store_text),
                                            valTexT,
                                            textView)
                                    }
                                }


                            }

                        } else {
                            when {
                                listState.size > 2 -> {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                }
                                listState.size in 1..2 -> {
                                    highlightTextData(activity, listState.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "), valTexT, textView)
                                }
                                listArea.size > 2 -> {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                }
                                else -> {
                                    highlightTextData(activity, listArea.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "), valTexT, textView)
                                }
                            }
                        }
                    } else {
                        if (listSuperVisor.size > 2) {
                            highlightTextData(activity,
                                activity.getString(R.string.filter_store),
                                valTexT,
                                textView)
                        } else if (listSuperVisor.isNotEmpty()) {
                            if(listSuperVisor.size == 1){
                                highlightTextData(activity, listSuperVisor.toString().replace("[", "")
                                    .replace("]", "").plus(" store"), valTexT, textView)
                            }else{
                                highlightTextData(activity, listSuperVisor.toString().replace("[", "")
                                    .replace("]", "").replace(",", " & "), valTexT, textView)
                            }
                        }else if (listState.size > 2) {
                            highlightTextData(activity,
                                activity.getString(R.string.filter_store),
                                valTexT,
                                textView)
                        } else {
                            highlightTextData(activity, listState.toString().replace("[", "")
                                .replace("]", "").replace(",", " & "), valTexT, textView)
                        }
                    }
                } else {
                    if (listSuperVisor.size > 2) {
                        highlightTextData(activity,
                            activity.getString(R.string.filter_store),
                            valTexT,
                            textView)
                    } else if (listSuperVisor.isNotEmpty()) {
                                if(listSuperVisor.size == 1){
                                    highlightTextData(activity, listSuperVisor.toString().replace("[", "")
                                        .replace("]", "").plus(" stores"), valTexT, textView)
                                }else{
                                    highlightTextData(activity, listSuperVisor.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "), valTexT, textView)
                                }
                    } else {
                        if (StorePrefData.isStateSelected) {
                            if (StorePrefData.isAreaSelected) {
                                textView.text =
                                    activity.getString(R.string.all_store_text) + " | " + valTexT
                                highlightTextData(activity,
                                    listSuperVisor.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "),
                                    valTexT,
                                    textView)
                            } else {
                                if (listArea.size > 2) {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                } else if (listArea.isNotEmpty()) {

                                    highlightTextData(activity,
                                        listArea.toString().replace("[", "")
                                            .replace("]", "").replace(",", " & "),
                                        valTexT,
                                        textView)
                                } else if (listState.size > 2) {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                } else if (listState.isNotEmpty()) {
                                    highlightTextData(activity, listState.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "), valTexT, textView)
                                }
                            }
                        } else {
                            if (listState.size > 2) {

                                highlightTextData(activity,
                                    activity.getString(R.string.filter_store),
                                    valTexT,
                                    textView)
                            } else {

                                highlightTextData(activity, listState.toString().replace("[", "")
                                    .replace("]", "").replace(",", " & "), valTexT, textView)
                            }
                        }
                    }
                }

            } else {
                if (listStore.size > 2) {
                    highlightTextData(activity,
                        activity.getString(R.string.filter_store),
                        valTexT,
                        textView)
                } else if (listStore.isNotEmpty()) {

                    highlightTextData(activity, listStore.toString().replace("[", "")
                        .replace("]", "").replace(",", " & "), valTexT, textView)
                } else {
                    if (StorePrefData.isSupervisorSelected) {
                        if (StorePrefData.isStateSelected) {
                            if (StorePrefData.isAreaSelected) {
                                highlightTextData(activity,
                                    activity.getString(R.string.all_store_text),
                                    valTexT,
                                    textView)
                            } else {
                                if (listArea.size > 2) {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                } else {
                                    
                                    highlightTextData(activity, listArea.toString().replace("[", "")
                                        .replace("]", "").replace(",", " & "), valTexT, textView)
                                }
                            }
                        } else {
                            if (listState.size > 2) {
                                highlightTextData(activity,
                                    activity.getString(R.string.filter_store),
                                    valTexT,
                                    textView)
                            } else {
                                highlightTextData(activity, listState.toString().replace("[", "")
                                    .replace("]", "").replace(",", " & "), valTexT, textView)
                            }
                        }
                    } else {
                        if (listSuperVisor.size > 2) {
                            highlightTextData(activity,
                                activity.getString(R.string.filter_store),
                                valTexT,
                                textView)
                        } else if (listSuperVisor.isNotEmpty()) {
                            highlightTextData(activity, listSuperVisor.toString().replace("[", "")
                                .replace("]", "").replace(",", " & "), valTexT, textView)
                        } else {
                            if (StorePrefData.isStateSelected) {
                                if (StorePrefData.isAreaSelected) {
                                    highlightTextData(activity,
                                        activity.getString(R.string.all_store_text),
                                        valTexT,
                                        textView)
                                } else {
                                    if (listArea.size > 2) {
                                        highlightTextData(activity,
                                            activity.getString(R.string.filter_store),
                                            valTexT,
                                            textView)
                                    } else if (listArea.isNotEmpty()) {
                                        highlightTextData(activity,
                                            listState.toString().replace("[", "")
                                                .replace("]", "").replace(",", " & "),
                                            valTexT,
                                            textView)
                                    } else if (listState.size > 2) {
                                        highlightTextData(activity,
                                            activity.getString(R.string.filter_store),
                                            valTexT,
                                            textView)
                                    } else if (listState.isNotEmpty()) {
                                        highlightTextData(activity,
                                            listArea.toString().replace("[", "")
                                                .replace("]", "").replace(",", " & "),
                                            valTexT,
                                            textView)
                                    }
                                }
                            } else {
                                if (listState.size > 2) {
                                    highlightTextData(activity,
                                        activity.getString(R.string.filter_store),
                                        valTexT,
                                        textView)
                                } else {
                                    highlightTextData(activity,
                                        listState.toString().replace("[", "")
                                            .replace("]", "").replace(",", " & "),
                                        valTexT,
                                        textView)
                                }
                            }
                        }
                    }
                }

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun highlightTextData(
        activity: FragmentActivity,
        storeText: String,
        valTexT: String,
        textView: TextView,
    ) {
        typefaceBold = if(storeText ==  activity.getString(R.string.filter_store) || storeText ==  activity.getString(R.string.all_store_text)){
            activity.resources.getFont(R.font.sf_compact_text_regular)
        }else{
            activity.resources.getFont(R.font.sf_ui_text_bold)
        }

        val spannable: Spannable = SpannableString("$storeText | $valTexT")
        spannable.setSpan(
                CustomTypefaceSpan("", typefaceBold),
                0,
                storeText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.setText(spannable, TextView.BufferType.SPANNABLE)

    }


    fun checkAmountPercentageValue(
        context: Context,
        amount: Double?,
        percentage: Double?,
        value: Double?,
    ): String {
        var finalValue = ""

        if (value != null) {
            finalValue = Validation().dollarFormatting(value)
        }else if (percentage != null) {
            finalValue = (Validation().ignoreZeroAfterDecimal(percentage)).plus(
                context.getString(
                    R.string.percentage_text))
        }else if (amount != null) {
            finalValue = if(amount < 0){

                context.getString(R.string.nagetive_dollar_text).plus(Validation().dollarFormatting(abs(amount)))
            }else{
                context.getString(R.string.dollar_text).plus(Validation().dollarFormatting(amount))
            }
        }
        return finalValue

    }

    
   
    
    fun checkAmountPercentageValueForHeaderCircle(
        context: Context,
        amount: Double?,
        percentage: Double?,
        value: Double?,
        textView: TextView,
    ) {
        var finalValue = ""
        when {
            value != null -> {
                finalValue = Validation().dollarFormatting(
                    value)
            }
            percentage != null -> {
                finalValue = (Validation().ignoreZeroAfterDecimal(
                    percentage)).plus(
                    context.getString(
                        R.string.percentage_text))
            }
            amount != null -> {
                finalValue = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        amount))
            }
        }
       
        textView.text = finalValue
    }


     fun showCurrentDate():String{
         val dateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
        val currentCal = Calendar.getInstance()
        return  dateFormat.format(currentCal.time)
    }
    fun checkNullValueToShowView(
        context: Context,
        displayName: String?,
        lastOerParent: LinearLayout,
    ){
        if(displayName!=null){
            lastOerParent.visibility = View.VISIBLE
        }else{
            lastOerParent.visibility = View.GONE
        }
    }
    fun setCustomCalendar(simpleCalendar: SimpleCalendar) {
        val cal = Calendar.getInstance()
        val date = Date()
        cal.time = date

        val year = cal[Calendar.YEAR]

        val month = cal[Calendar.MONTH]
        Log.e("MONTH", month.toString() + "")
        simpleCalendar.setUserCurrentMonthYear(month, year)

    }


}
