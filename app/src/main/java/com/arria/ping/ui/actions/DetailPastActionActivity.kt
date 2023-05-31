package com.arria.ping.ui.actions

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.kpi.GMActionQuery
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.activity_past_action.*

class DetailPastActionActivity : AppCompatActivity() {

    val gson = Gson()
    private var isInitialAction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_action)

        Logger.info("Detail Past Action Screen","Detail Past Action")

        cross_button_past.setOnClickListener {
            finish()
        }
        action_to_perform_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        isInitialAction = intent.getBooleanExtra("isInitialAction", false)

        if (StorePrefData.role == getString(R.string.ceo_text)) {
            if (isInitialAction) {
                setDataCEOInitialAction()
            } else {
                setDataCEO()
            }
        } else if (StorePrefData.role == getString(R.string.do_text)) {
            if (isInitialAction) {
                setDataDOInitialAction()
            } else {
                setDataDO()
            }
        } else if (StorePrefData.role == getString(R.string.supervisor_text)) {
            if (isInitialAction) {
                setDataSupervisorInitialAction()
            } else {
                setDataSupervisor()
            }
        } else {
            if (isInitialAction) {
                setDataGMInitialAction()
            } else {
                setDataGM()
            }

        }
    }

    private fun setDataGM() {
        val pastGMDataPosition = intent.getIntExtra("detail_past_action_data_position", 0)
        val pastGMData = gson.fromJson(
            intent.getStringExtra("detail_past_action_data"),
            GMActionQuery.GeneralManager::class.java
        )
        val dataToSetGM = pastGMData.actions!!.store!!.pastActions[pastGMDataPosition]!!

        past_action_title.text = dataToSetGM.actionTitle
        created_date.text = DateFormatterUtil.formatDateForAction(dataToSetGM.actionCreatedOn!!)
        accepted_date.text =DateFormatterUtil.formatDateForAction(dataToSetGM.actionAcceptedOn!!)

        if (dataToSetGM.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText = dataToSetGM.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text = dataToSetGM.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataToSetGM.narrative

        bonus_at_risk_value.text =
            if (dataToSetGM.actionMetric!!.actual != null && !dataToSetGM.actionMetric.actual!!.amount!!.isNaN()) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataToSetGM.actionMetric.actual.amount)
            ) else ""


        if (dataToSetGM.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataToSetGM.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataToSetGM.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))
            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataToSetGM.actionRemainingDays)

        val gmActionAdapter = GMPastActionToPerformListAdapter(this, dataToSetGM.actionsToPerform)
        action_to_perform_rv.adapter = gmActionAdapter
    }

    private fun setDataGMInitialAction() {
        val dataToSetGMInitialAction = gson.fromJson(
            intent.getStringExtra("detail_past_action_data"),
            GMActionQuery.CurrentAction::class.java
        )
        past_action_title.text = dataToSetGMInitialAction.actionTitle
        created_date.text =
           DateFormatterUtil.formatDateForAction(dataToSetGMInitialAction.actionCreatedOn!!)
        accepted_date.text =
           DateFormatterUtil.formatDateForAction(dataToSetGMInitialAction.actionAcceptedOn!!)
        if (dataToSetGMInitialAction.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText =
                dataToSetGMInitialAction.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text =
                dataToSetGMInitialAction.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataToSetGMInitialAction.narrative

        bonus_at_risk_value.text =
            if (dataToSetGMInitialAction.actionMetric!!.actual != null && dataToSetGMInitialAction.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataToSetGMInitialAction.actionMetric.actual.amount)
            ) else ""


        if (dataToSetGMInitialAction.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataToSetGMInitialAction.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataToSetGMInitialAction.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataToSetGMInitialAction.actionRemainingDays)


        val gmInitialActionAdapter = GMPastActionToPerformListAdapter(this, dataToSetGMInitialAction.actionsToPerform)
        action_to_perform_rv.adapter = gmInitialActionAdapter
    }

    private fun setDataCEO() {
        val pastCEODataPosition = intent.getIntExtra("detail_past_action_data_position", 0)
        val pastCEOData = ActionsFragment.actionDataCEO
        val dataCEOToSet =
            pastCEOData.actions!!.stores[pastCEODataPosition]!!.pastActions[pastCEODataPosition]!!
        past_action_title.text = dataCEOToSet.actionTitle
        created_date.text =DateFormatterUtil.formatDateForAction(dataCEOToSet.actionCreatedOn!!)
        accepted_date.text =DateFormatterUtil.formatDateForAction(dataCEOToSet.actionAcceptedOn!!)
        if (dataCEOToSet.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText = dataCEOToSet.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text = dataCEOToSet.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataCEOToSet.narrative

        bonus_at_risk_value.text =
            if (dataCEOToSet.actionMetric!!.actual != null && dataCEOToSet.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataCEOToSet.actionMetric.actual.amount)
            ) else ""


        if (dataCEOToSet.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataCEOToSet.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataCEOToSet.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataCEOToSet.actionRemainingDays)

        val ceoAdapter = GMPastActionToPerformListAdapter(this, dataCEOToSet.actionsToPerform)
        action_to_perform_rv.adapter = ceoAdapter
    }

    private fun setDataCEOInitialAction() {
        val pastCEOInitialDataPosition = intent.getIntExtra("detail_past_action_data_position", 0)
        val pastCEOInitialData = ActionsFragment.actionDataCEO
        val dataCEOInitialToSet =
            pastCEOInitialData.actions!!.stores[pastCEOInitialDataPosition]!!.currentActions[pastCEOInitialDataPosition]!!
        past_action_title.text = dataCEOInitialToSet.actionTitle
        created_date.text =DateFormatterUtil.formatDateForAction(dataCEOInitialToSet.actionCreatedOn!!)
        accepted_date.text =
           DateFormatterUtil.formatDateForAction(dataCEOInitialToSet.actionAcceptedOn!!)
        if (dataCEOInitialToSet.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText =
                dataCEOInitialToSet.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text = dataCEOInitialToSet.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataCEOInitialToSet.narrative

        bonus_at_risk_value.text =
            if (dataCEOInitialToSet.actionMetric!!.actual != null && dataCEOInitialToSet.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataCEOInitialToSet.actionMetric.actual.amount)
            ) else ""


        if (dataCEOInitialToSet.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataCEOInitialToSet.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataCEOInitialToSet.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataCEOInitialToSet.actionRemainingDays)

        val ceoInitialActionAdapter =
            GMPastActionToPerformListAdapter(this, dataCEOInitialToSet.actionsToPerform)
        action_to_perform_rv.adapter = ceoInitialActionAdapter
    }

    private fun setDataDO() {
        val pastDODataPosition = intent.getIntExtra("detail_past_action_data_position", 0)
        val pastDOData = ActionsFragment.actionDataDO
        val dataDOToSet =
            pastDOData.actions!!.stores[pastDODataPosition]!!.pastActions[pastDODataPosition]!!
        past_action_title.text = dataDOToSet.actionTitle
        created_date.text =DateFormatterUtil.formatDateForAction(dataDOToSet.actionCreatedOn!!)
        accepted_date.text =DateFormatterUtil.formatDateForAction(dataDOToSet.actionAcceptedOn!!)
        if (dataDOToSet.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText = dataDOToSet.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text = dataDOToSet.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataDOToSet.narrative

        bonus_at_risk_value.text =
            if (dataDOToSet.actionMetric!!.actual != null && dataDOToSet.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataDOToSet.actionMetric.actual.amount)
            ) else ""


        if (dataDOToSet.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataDOToSet.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataDOToSet.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataDOToSet.actionRemainingDays)

        val doAdapter = GMPastActionToPerformListAdapter(this, dataDOToSet.actionsToPerform)
        action_to_perform_rv.adapter = doAdapter
    }

    private fun setDataDOInitialAction() {
        val pastDOInitialDataPosition = intent.getIntExtra("detail_past_action_data_position", 0)
        val pastDOInitialData = ActionsFragment.actionDataDO
        val dataDOInitialToSet =
            pastDOInitialData.actions!!.stores[pastDOInitialDataPosition]!!.currentActions[pastDOInitialDataPosition]!!
        past_action_title.text = dataDOInitialToSet.actionTitle
        created_date.text =DateFormatterUtil.formatDateForAction(dataDOInitialToSet.actionCreatedOn!!)
        accepted_date.text =DateFormatterUtil.formatDateForAction(dataDOInitialToSet.actionAcceptedOn!!)
        if (dataDOInitialToSet.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText =
                dataDOInitialToSet.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text = dataDOInitialToSet.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataDOInitialToSet.narrative

        bonus_at_risk_value.text =
            if (dataDOInitialToSet.actionMetric!!.actual != null && dataDOInitialToSet.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataDOInitialToSet.actionMetric.actual.amount)
            ) else ""


        if (dataDOInitialToSet.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataDOInitialToSet.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataDOInitialToSet.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataDOInitialToSet.actionRemainingDays)

       val doInitialActionAdapter = GMPastActionToPerformListAdapter(this, dataDOInitialToSet.actionsToPerform)
        action_to_perform_rv.adapter = doInitialActionAdapter
    }

    private fun setDataSupervisor() {
        val pastSupervisorDataPosition = intent.getIntExtra("detail_past_action_data_position", 0)
        val pastSupervisorData = ActionsFragment.actionDataSupervisor
        val dataSupervisorToSet =
            pastSupervisorData.actions!!.stores[pastSupervisorDataPosition]!!.pastActions[pastSupervisorDataPosition]!!
        past_action_title.text = dataSupervisorToSet.actionTitle
        created_date.text =DateFormatterUtil.formatDateForAction(dataSupervisorToSet.actionCreatedOn!!)
        accepted_date.text =
           DateFormatterUtil.formatDateForAction(dataSupervisorToSet.actionAcceptedOn!!)
        if (dataSupervisorToSet.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText =
                dataSupervisorToSet.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text = dataSupervisorToSet.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataSupervisorToSet.narrative

        bonus_at_risk_value.text =
            if (dataSupervisorToSet.actionMetric!!.actual != null && dataSupervisorToSet.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataSupervisorToSet.actionMetric.actual.amount)
            ) else ""

        if (dataSupervisorToSet.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataSupervisorToSet.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataSupervisorToSet.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataSupervisorToSet.actionRemainingDays)

        val supervisorAdapter = GMPastActionToPerformListAdapter(this, dataSupervisorToSet.actionsToPerform)
        action_to_perform_rv.adapter = supervisorAdapter
    }

    private fun setDataSupervisorInitialAction() {
        val pastSupervisorInitialDataPosition =
            intent.getIntExtra("detail_past_action_data_position", 0)
        val pastSupervisorInitialData = ActionsFragment.actionDataSupervisor
        val dataSupervisorInitialToSet =
            pastSupervisorInitialData.actions!!.stores[pastSupervisorInitialDataPosition]!!.currentActions[pastSupervisorInitialDataPosition]!!
        past_action_title.text = dataSupervisorInitialToSet.actionTitle
        created_date.text =
           DateFormatterUtil.formatDateForAction(dataSupervisorInitialToSet.actionCreatedOn!!)
        accepted_date.text =
           DateFormatterUtil.formatDateForAction(dataSupervisorInitialToSet.actionAcceptedOn!!)
        if (dataSupervisorInitialToSet.sevenDayTrackerNarrative!!.contains("\n")) {
            val sevenDayTrackerNarrativeText =
                dataSupervisorInitialToSet.sevenDayTrackerNarrative.split("\n")
            seven_day_tracker_narrative.text = sevenDayTrackerNarrativeText[1].trimStart()
            seven_day_tracker_narrative_status.text = sevenDayTrackerNarrativeText[0]
        } else {
            seven_day_tracker_narrative.text =
                dataSupervisorInitialToSet.sevenDayTrackerNarrative.trim()
            seven_day_tracker_narrative_status.visibility = View.GONE
        }
        issue_text_narrative.text = dataSupervisorInitialToSet.narrative

        bonus_at_risk_value.text =
            if (dataSupervisorInitialToSet.actionMetric!!.actual != null && dataSupervisorInitialToSet.actionMetric.actual!!.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(dataSupervisorInitialToSet.actionMetric.actual.amount)
            ) else ""


        if (dataSupervisorInitialToSet.actionMetric.variance!!.percentage?.isNaN() == false) {
            bonus_at_risk_variance_value.text =
                dataSupervisorInitialToSet.actionMetric.variance.percentage.toString()
                    .plus(getString(R.string.percentage_text))
            if (dataSupervisorInitialToSet.actionMetric.status.toString() == resources.getString(R.string.out_of_range)) {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.red))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.red))


            } else {
                bonus_at_risk_variance_value.setTextColor(this.getColor(R.color.green))
                seven_day_tracker_narrative_status.setTextColor(this.getColor(R.color.green))
            }
        }

        calRemainingDays(dataSupervisorInitialToSet.actionRemainingDays)

        val supervisorInitialActionAdapter = GMPastActionToPerformListAdapter(this, dataSupervisorInitialToSet.actionsToPerform)
        action_to_perform_rv.adapter = supervisorInitialActionAdapter
    }


    private fun calRemainingDays(actionRemainingDays: Int?) {
        when (actionRemainingDays) {
            1 -> {
                action_remaining_days_1.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_2.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)

                action_remaining_days_3.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_4.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_5.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_6.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
            }
            2 -> {
                action_remaining_days_1.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_2.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_3.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_4.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_5.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
            }
            3 -> {
                action_remaining_days_1.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_2.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_3.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_4.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
            }
            4 -> {
                action_remaining_days_1.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_2.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_3.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)

            }
            5 -> {
                action_remaining_days_1.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                action_remaining_days_2.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
            }
            6 -> {
                action_remaining_days_1.background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
            }
        }
    }

}