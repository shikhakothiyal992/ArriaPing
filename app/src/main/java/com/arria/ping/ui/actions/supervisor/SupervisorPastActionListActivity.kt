package com.arria.ping.ui.actions.supervisor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.SupervisorActionQuery
import com.arria.ping.ui.actions.ActionsFragment
import com.arria.ping.ui.actions.DetailPastActionActivity
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.activity_past_alerts_list.*
import kotlinx.android.synthetic.main.ceo_activity_past_action_list.*
import kotlinx.android.synthetic.main.ceo_activity_past_action_list.cross_button_past
import kotlinx.android.synthetic.main.ceo_activity_past_action_list.filter_icon
import kotlinx.android.synthetic.main.ceo_activity_past_action_list.no_past_action_linear
import kotlinx.android.synthetic.main.ceo_activity_past_action_list.search_past_action
import java.util.*

class SupervisorPastActionListActivity : AppCompatActivity() {
    val gson = Gson()
    private var   supervisorActionAdapter: SupervisorPastActionListAdapter? = null
    lateinit var pastActionDataSupervisor: SupervisorActionQuery.Supervisor
    var position =0
    lateinit var dbHelperSupervisor: DatabaseHelperImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ceo_activity_past_action_list)
        initialise()
    }

    private fun initialise() {
        dbHelperSupervisor = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))

        cross_button_past.setOnClickListener {
            finish()
        }
        past_action_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        pastActionDataSupervisor = ActionsFragment.actionDataSupervisor
        position = intent.getIntExtra("detail_past_action_data_position_ceo",0)


        search_past_action.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                filter_icon.visibility = View.GONE
                search_past_action.onActionViewExpanded()
                search_past_action.setIconifiedByDefault(true)
                if (query!!.isNotEmpty()) {
                      supervisorActionAdapter?.filter?.filter(query)
                } else {
                    filter_icon.visibility = View.VISIBLE
                    setDataSupervisor()
                }

                return false
            }

        })
        val cancelIconSupervisor = search_past_action.findViewById<ImageView>(R.id.search_close_btn)
        cancelIconSupervisor.setImageResource(R.drawable.ic_icons_delete)

        cancelIconSupervisor.setOnClickListener {
            Logger.info("Cancelled button clicked","Past Action Store List")
            filter_icon.visibility = View.VISIBLE
            search_past_action.setIconifiedByDefault(true)
            search_past_action.setQuery("", false)
            setDataSupervisor()
        }
        filter_icon.setOnClickListener {
            Logger.info("Filter button clicked","Past Action Store List")
            StorePrefData.isFromSupervisorPastActionList = true
            Validation().openFilter(this)
            finish()
        }
        search_past_action.onActionViewExpanded()
        search_past_action.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_past_action.clearFocus() }, 300)

        setDataSupervisor()
    }

    private fun setDataSupervisor() {
        if (pastActionDataSupervisor.actions?.stores?.isNotEmpty() == true) {
            store_number.text = pastActionDataSupervisor.actions!!.stores[position]!!.storeNumber
            store_name.text = pastActionDataSupervisor.actions!!.stores[position]!!.storeName
            no_past_action_linear.visibility = View.GONE
            past_action_rv.visibility = View.VISIBLE
              supervisorActionAdapter =
                SupervisorPastActionListAdapter(this, pastActionDataSupervisor.actions!!.stores[position]!!.pastActions)
            past_action_rv.adapter =   supervisorActionAdapter
        } else {
            no_past_action_linear.visibility = View.VISIBLE
            past_action_rv.visibility = View.GONE

        }
          supervisorActionAdapter?.setOnSupervisorPastActionItemClickListener(object :
              SupervisorPastActionListAdapter.OnItemClickListener {
            override fun onItemClick( position: Int) {
                callDetailView(position)
            }
        })

    }
    private fun callDetailView(position: Int) {
        Logger.info("Navigation to DetailPast List clicked","Past Action Store List")
        val intent = Intent(this, DetailPastActionActivity::class.java)
        intent.putExtra("detail_past_action_data_position", position)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        if (StorePrefData.isFromSupervisorPastActionList) {
            callSupervisorAction()
        }
    }

    private fun callSupervisorAction() {
        StorePrefData.isFromSupervisorPastActionList = false
        val progressDialog = CustomProgressDialog(this)
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            val areaCode = dbHelperSupervisor.getAllSelectedAreaList(true)
            val stateCode = dbHelperSupervisor.getAllSelectedStoreListState(true)
            val storeNumber = dbHelperSupervisor.getAllSelectedStoreList(true)

            Logger.info(
                SupervisorActionQuery.OPERATION_NAME.name(), "Past Action Store List",
                mapQueryFilters(
                        areaCode, stateCode, Collections.emptyList(), storeNumber, SupervisorActionQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(this@SupervisorPastActionListActivity).query(SupervisorActionQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Past Action Store List")
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {
                progressDialog.dismissProgressDialog()
                pastActionDataSupervisor = response.data?.supervisor!!
                setDataSupervisor()
            }
        }
    }

}