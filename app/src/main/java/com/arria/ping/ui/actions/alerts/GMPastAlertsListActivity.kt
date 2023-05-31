package com.arria.ping.ui.actions.alerts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.kpi.GMAlertsQuery
import kotlinx.android.synthetic.main.activity_past_alerts_list.*

class GMPastAlertsListActivity : AppCompatActivity() {
    val gson = Gson()
    private var gmAlertsAdapter: GMPastPastAlertListAdapter? = null
    lateinit var pastAlertsData: GMAlertsQuery.GeneralManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_alerts_list)
        initialise()

        Logger.info(GMAlertsQuery.OPERATION_NAME.name(),"PAST ALERTS")

    }

    private fun initialise() {
        filter_icon.visibility = View.GONE
        past_alerts_checkin_common_header.text = getString(R.string.past_alerts_without_underline)
        cross_button_past.setOnClickListener {
            finish()
        }
        past_alerts_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        pastAlertsData = gson.fromJson(intent.getStringExtra("past_action_data"), GMAlertsQuery.GeneralManager::class.java)

        search_past_action.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                search_past_action.onActionViewExpanded()
                search_past_action.setIconifiedByDefault(true)
                if (query!!.isNotEmpty()) {
                    gmAlertsAdapter?.filter?.filter(query)
                } else {
                    setData()
                }

                return false
            }

        })
        val cancelIcon = search_past_action.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setImageResource(R.drawable.ic_icons_delete)

        cancelIcon.setOnClickListener {
            search_past_action.setIconifiedByDefault(true)
            search_past_action.setQuery("", false)
            setData()
        }
        search_past_action.onActionViewExpanded()
        search_past_action.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_past_action.clearFocus() }, 300)

        setData()
    }

    private fun setData() {
        if (pastAlertsData.alerts!!.store!!.pastAlerts.isNotEmpty()) {
            no_past_action_linear.visibility = View.GONE
            past_alerts_rv.visibility = View.VISIBLE
            past_alerts_actions_to_perform_parent.visibility = View.VISIBLE
            gmAlertsAdapter =
                GMPastPastAlertListAdapter(this, pastAlertsData.alerts!!.store!!.pastAlerts)
            past_alerts_rv.adapter = gmAlertsAdapter
        } else {
            no_past_action_linear.visibility = View.VISIBLE
            past_alerts_rv.visibility = View.GONE
            past_alerts_actions_to_perform_parent.visibility = View.GONE

        }

    }
}