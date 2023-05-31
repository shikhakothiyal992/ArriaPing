package com.arria.ping.ui.actions.alerts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.kpi.GMAlertsQuery
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.StorePrefData
import kotlinx.android.synthetic.main.fragment_alerts.*
import kotlinx.android.synthetic.main.fragment_alerts.alerts_rv
import kotlinx.android.synthetic.main.fragment_alerts.no_new_action_linear
import kotlinx.android.synthetic.main.fragment_alerts.past_alerts
import kotlinx.android.synthetic.main.fragment_check_ins.*
import java.util.*

class AlertsFragment : Fragment() {
    val gson = Gson()
    private lateinit var alertsDataGM: GMAlertsQuery.GeneralManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_alerts, container, false)
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


            val gmAlertsListValue = mutableListOf<String>()
            gmAlertsListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                GMAlertsQuery.OPERATION_NAME.name(), "ALERTS", mapQueryFilters(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    gmAlertsListValue,
                    GMAlertsQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(requireContext()).query(GMAlertsQuery(StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Log.d("ApolloError", "Failure", e)
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialog.dismissProgressDialog()
                alertsDataGM = response.data!!.generalManager!!
                setDataGM(response.data?.generalManager!!)
            }
        }
    }
    private fun setDataGM(generalManager: GMAlertsQuery.GeneralManager) {
        if (generalManager.alerts!!.store!!.currentAlerts.isNotEmpty()) {
            no_new_action_linear.visibility = View.GONE
            alerts_rv.visibility = View.VISIBLE
            alerts_actions_to_perform_parent.visibility = View.VISIBLE
            val gmActionAdapter =
                GMAlertsAdapter(requireContext(), generalManager.alerts.store!!.currentAlerts)
            alerts_rv.adapter = gmActionAdapter
        } else {
            no_new_action_linear.visibility = View.VISIBLE
            alerts_rv.visibility = View.GONE
            alerts_actions_to_perform_parent.visibility = View.GONE

        }

    }

    private fun openPastAlertsGM() {
        val intent = Intent(requireContext(), GMPastAlertsListActivity::class.java)
        intent.putExtra("past_action_data", gson.toJson(alertsDataGM))
        startActivity(intent)
    }
}