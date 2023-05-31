package com.arria.ping.ui.actions.do_

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
import com.arria.ping.kpi.DOActionQuery
import com.arria.ping.ui.actions.ActionsFragment
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.activity_past_action_list.*
import kotlinx.android.synthetic.main.activity_past_action_list.cross_button_past
import kotlinx.android.synthetic.main.activity_past_action_list.filter_icon
import kotlinx.android.synthetic.main.activity_past_action_list.no_past_action_linear
import kotlinx.android.synthetic.main.activity_past_action_list.past_action_rv
import kotlinx.android.synthetic.main.activity_past_action_list.search_past_action
import kotlinx.android.synthetic.main.activity_past_alerts_list.*
import kotlinx.android.synthetic.main.ceo_activity_past_action_list.*
import java.util.*

class DOPastActionStoreListActivity : AppCompatActivity() {
    val gson = Gson()
    private var ceoActionAdapter: DOPastActionStoreListAdapter? = null
    lateinit var pastActionDataDO: DOActionQuery.Do_
    lateinit var dbHelper: DatabaseHelperImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_action_list)
        initialise()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }
    private fun initialise() {
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
        cross_button_past.setOnClickListener {
            finish()
        }
        past_action_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        pastActionDataDO = ActionsFragment.actionDataDO


        search_past_action.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                filter_icon.visibility = View.GONE
                search_past_action.onActionViewExpanded()
                search_past_action.setIconifiedByDefault(true)
                if (query!!.isNotEmpty()) {
                    ceoActionAdapter?.filter?.filter(query)
                } else {
                    filter_icon.visibility = View.VISIBLE
                    setData()
                }

                return false
            }

        })
        val cancelIcon = search_past_action.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setImageResource(R.drawable.ic_icons_delete)

        cancelIcon.setOnClickListener {
            Logger.info("Cancel button clicked","Past Action Store List")
            filter_icon.visibility = View.VISIBLE
            search_past_action.setIconifiedByDefault(true)
            search_past_action.setQuery("", false)
            setData()
        }
        filter_icon.setOnClickListener {
            Logger.info("Filter button clicked","Past Action Store List")
            StorePrefData.isFromDOPastActionStore = true
            Validation().openFilter(this)
            finish()
        }
        search_past_action.onActionViewExpanded()
        search_past_action.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_past_action.clearFocus() }, 300)

        setData()
    }

    private fun setData() {
        if (pastActionDataDO.actions?.stores!!.isNotEmpty()) {
            no_past_action_linear.visibility = View.GONE
            past_action_rv.visibility = View.VISIBLE
            ceoActionAdapter =
                DOPastActionStoreListAdapter(this, pastActionDataDO.actions!!.stores)
            past_action_rv.adapter = ceoActionAdapter
        } else {
            no_past_action_linear.visibility = View.VISIBLE
            past_action_rv.visibility = View.GONE

        }
        ceoActionAdapter?.setOnItemDoStoreClickListener(object :
            DOPastActionStoreListAdapter.OnItemClickListener {
            override fun onItemClick( position: Int) {
                callDetailView(position)
            }
        })

    }
    private fun callDetailView(position: Int) {
        Logger.info("Navigation to DO Past Action clicked","Past Action Store List")
        val intent = Intent(this, DOPastActionListActivity::class.java)
        intent.putExtra("detail_past_action_data_position_ceo", position)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        if (StorePrefData.isFromDOPastActionStore) {
            callDOAction()
        }
    }
    private fun callDOAction() {
        StorePrefData.isFromDOPastActionStore = false
        val progressDialog = CustomProgressDialog(this)
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCode = dbHelper.getAllSelectedAreaList(true)
            val stateCode = dbHelper.getAllSelectedStoreListState(true)
            val storeNumber = dbHelper.getAllSelectedStoreList(true)


            Logger.info(
                DOActionQuery.OPERATION_NAME.name(),
                "Past Action Store List",
               mapQueryFilters(
                       areaCode, stateCode, Collections.emptyList(), storeNumber, DOActionQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(this@DOPastActionStoreListActivity).query(DOActionQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Past Action Store List")
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialog.dismissProgressDialog()
                pastActionDataDO = response.data?.do_!!
                setData()
            }
        }
    }

}