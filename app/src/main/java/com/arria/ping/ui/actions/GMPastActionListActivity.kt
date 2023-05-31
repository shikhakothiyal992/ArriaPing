package com.arria.ping.ui.actions

import android.content.Intent
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
import com.arria.ping.kpi.GMActionQuery
import kotlinx.android.synthetic.main.activity_past_action_list.*

class GMPastActionListActivity : AppCompatActivity() {
    val gson = Gson()
    private var gmActionAdapter: GMPastActionListAdapter? = null
    lateinit var pastActionDataGM: GMActionQuery.GeneralManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_action_list)
        initialise()

       Logger.info("GM Past Action List Screen","Past Action")

    }

    private fun initialise() {
        filter_icon.visibility = View.GONE
        cross_button_past.setOnClickListener {
            finish()
        }
        past_action_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        pastActionDataGM = gson.fromJson(intent.getStringExtra("past_action_data"), GMActionQuery.GeneralManager::class.java)


        search_past_action.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                search_past_action.onActionViewExpanded()
                search_past_action.setIconifiedByDefault(true)
                if (query!!.isNotEmpty()) {
                    gmActionAdapter?.filter?.filter(query)
                } else {
                    setDataGM()
                }

                return false
            }

        })
        val cancelIconGM = search_past_action.findViewById<ImageView>(R.id.search_close_btn)
        cancelIconGM.setImageResource(R.drawable.ic_icons_delete)

        cancelIconGM.setOnClickListener {
            search_past_action.setIconifiedByDefault(true)
            search_past_action.setQuery("", false)
            setDataGM()
        }
        search_past_action.onActionViewExpanded()
        search_past_action.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_past_action.clearFocus() }, 300)

        setDataGM()
    }

    private fun setDataGM() {
        if (pastActionDataGM.actions?.store?.pastActions!!.isNotEmpty()) {
            no_past_action_linear.visibility = View.GONE
            past_action_rv.visibility = View.VISIBLE
            gmActionAdapter =
                GMPastActionListAdapter(this, pastActionDataGM.actions!!.store!!.pastActions)
            past_action_rv.adapter = gmActionAdapter
        } else {
            no_past_action_linear.visibility = View.VISIBLE
            past_action_rv.visibility = View.GONE

        }
        gmActionAdapter?.setOnGMPastActionItemClickListener(object :
            GMPastActionListAdapter.OnItemClickListener {
            override fun onItemClick( position: Int) {
                callDetailView(position)
            }
        })

    }
    private fun callDetailView(position: Int) {
        val intent = Intent(this, DetailPastActionActivity::class.java)
        intent.putExtra("detail_past_action_data_position", position)
        intent.putExtra("detail_past_action_data", gson.toJson(pastActionDataGM))
        startActivity(intent)
    }
}