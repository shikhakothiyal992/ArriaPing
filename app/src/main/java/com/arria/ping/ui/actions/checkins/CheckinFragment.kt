package com.arria.ping.ui.actions.checkins

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.kpi.GMCheckInsQuery
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.StorePrefData
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_alerts.*
import kotlinx.android.synthetic.main.fragment_alerts.alerts_rv
import kotlinx.android.synthetic.main.fragment_alerts.no_new_action_linear
import kotlinx.android.synthetic.main.fragment_alerts.past_alerts
import kotlinx.android.synthetic.main.fragment_check_ins.*
import java.util.*

class CheckInFragment : Fragment() {
    val gson = Gson()
    private lateinit var checkinsDataGM: GMCheckInsQuery.GeneralManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_check_ins, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialise()

    }
    private fun initialise() {
        alerts_rv.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        past_alerts.setOnClickListener {
            openPastAlertsGM()
        }
        callGMAlerts()
    }
    private fun callGMAlerts() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {


            val gmCheckInListValue = mutableListOf<String>()
            gmCheckInListValue.add(StorePrefData.StoreIdFromLogin)

            Logger.info(
                GMCheckInsQuery.OPERATION_NAME.name(), "CheckIn", mapQueryFilters(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    gmCheckInListValue,
                    GMCheckInsQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(requireContext()).query(GMCheckInsQuery(StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"CheckIn")
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialog.dismissProgressDialog()
                checkinsDataGM = response.data!!.generalManager!!
                setDataGM(response.data?.generalManager!!)
            }
        }
    }
    private fun setDataGM(generalManager: GMCheckInsQuery.GeneralManager) {
        if (generalManager.checkIns!!.store!!.currentCheckIns.isNotEmpty()) {
            chek_in_time.text = DateFormatterUtil.formatDateForCurrentCheckIn(generalManager.checkIns.store!!.currentCheckIns[0]?.createdOn!!)
            no_new_action_linear.visibility = View.GONE
            alerts_rv.visibility = View.VISIBLE
            check_ins_actions_to_perform_parent.visibility = View.VISIBLE
            val gmActionAdapter =
                GMCheckInsAdapter(requireContext(), generalManager.checkIns.store.currentCheckIns[0]!!.checkInDetails)
            alerts_rv.adapter = gmActionAdapter
        } else {
            no_new_action_linear.visibility = View.VISIBLE
            alerts_rv.visibility = View.GONE
            check_ins_actions_to_perform_parent.visibility = View.GONE


        }

    }

    private fun openPastAlertsGM() {
        Logger.info("Navigation to GM Past CheckIn clicked","CheckIn")

        val intent = Intent(requireContext(), GMPastCheckInsListActivity::class.java)
        intent.putExtra("past_action_data", gson.toJson(checkinsDataGM))
        startActivity(intent)
    }
}