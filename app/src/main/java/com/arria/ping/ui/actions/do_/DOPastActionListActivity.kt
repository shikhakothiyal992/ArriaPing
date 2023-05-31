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

class DOPastActionListActivity : AppCompatActivity() {
    val gson = Gson()
    private var   doActionAdapter: DOPastActionListAdapter? = null
    lateinit var pastActionDoData: DOActionQuery.Do_
    var position =0
    lateinit var dbHelperDo: DatabaseHelperImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ceo_activity_past_action_list)
        initialise()
    }

    private fun initialise() {
        dbHelperDo = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
        cross_button_past.setOnClickListener {
            finish()
        }
        past_action_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        pastActionDoData = ActionsFragment.actionDataDO
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
                      doActionAdapter?.filter?.filter(query)
                } else {
                    filter_icon.visibility = View.VISIBLE
                    setDataDo()
                }

                return false
            }

        })
        val cancelIconDo = search_past_action.findViewById<ImageView>(R.id.search_close_btn)
        cancelIconDo.setImageResource(R.drawable.ic_icons_delete)

        cancelIconDo.setOnClickListener {
            Logger.info("Cancel button clicked","Past Action List")
            filter_icon.visibility = View.VISIBLE
            search_past_action.setIconifiedByDefault(true)
            search_past_action.setQuery("", false)
            setDataDo()
        }
        filter_icon.setOnClickListener {
            Logger.info("Filter button clicked","Past Action List")
            StorePrefData.isFromDOPastActionList = true
            Validation().openFilter(this)
            finish()
        }
        search_past_action.onActionViewExpanded()
        search_past_action.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_past_action.clearFocus() }, 300)

        setDataDo()
    }

    private fun setDataDo() {
        if (pastActionDoData.actions?.stores?.isNotEmpty() == true) {
            store_number.text = pastActionDoData.actions!!.stores[position]!!.storeNumber
            store_name.text = pastActionDoData.actions!!.stores[position]!!.storeName
            no_past_action_linear.visibility = View.GONE
            past_action_rv.visibility = View.VISIBLE
              doActionAdapter =
                DOPastActionListAdapter(this, pastActionDoData.actions!!.stores[position]!!.pastActions)
            past_action_rv.adapter =   doActionAdapter
        } else {
            no_past_action_linear.visibility = View.VISIBLE
            past_action_rv.visibility = View.GONE

        }
          doActionAdapter?.setOnDoPastActionItemClickListener(object :
              DOPastActionListAdapter.OnItemClickListener {
            override fun onItemClick( position: Int) {
                callDetailView(position)
            }
        })

    }
    private fun callDetailView(position: Int) {
        Logger.info("Navigation to Detail Past Action clicked","Past Action List")
        val intent = Intent(this, DetailPastActionActivity::class.java)
        intent.putExtra("detail_past_action_data_position", position)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        if (StorePrefData.isFromDOPastActionList) {
            callDOAction()
        }
    }
    private fun callDOAction() {
        StorePrefData.isFromDOPastActionList = false
        val progressDialog = CustomProgressDialog(this)
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCode = dbHelperDo.getAllSelectedAreaList(true)
            val stateCode = dbHelperDo.getAllSelectedStoreListState(true)
            val storeNumber = dbHelperDo.getAllSelectedStoreList(true)


            Logger.info(
                DOActionQuery.OPERATION_NAME.name(),
                "Past Action List",
                mapQueryFilters(
                    areaCode, stateCode, Collections.emptyList(),storeNumber, DOActionQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(this@DOPastActionListActivity).query(DOActionQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Past Action List")
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialog.dismissProgressDialog()
                pastActionDoData = response.data?.do_!!
               setDataDo()
            }
        }
    }


}