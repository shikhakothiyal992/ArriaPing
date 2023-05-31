package com.arria.ping.ui.actions.checkins

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.kpi.GMCheckInsQuery
import com.arria.ping.util.DateFormatterUtil
import kotlinx.android.synthetic.main.activity_past_alerts_list.*
import java.util.*

class GMPastCheckInsListActivity : AppCompatActivity() {
    val gson = Gson()
    private var gmPastCheckInAdapter: GMPastCheckInsListAdapter? = null
    lateinit var pastCheckInData: GMCheckInsQuery.GeneralManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_alerts_list)
        initialise()

        Logger.info(GMCheckInsQuery.OPERATION_NAME.name(),"Past CheckIn")

    }

    private fun initialise() {
        filter_icon.visibility = View.GONE
        past_alerts_checkin_common_header.text = getString(R.string.past_check_ins_without_underline)
            cross_button_past.setOnClickListener {
            finish()
        }

        past_alerts_rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        pastCheckInData = gson.fromJson(intent.getStringExtra("past_action_data"), GMCheckInsQuery.GeneralManager::class.java)

        search_past_action.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                search_past_action.onActionViewExpanded()
                search_past_action.setIconifiedByDefault(true)
                if (query!!.isNotEmpty()) {
                    gmPastCheckInAdapter?.filter?.filter(query)
                } else {
                    setDataGMPastCheckIn()
                }

                return false
            }

        })
        val cancelIconGMPastCheckIn = search_past_action.findViewById<ImageView>(R.id.search_close_btn)
        cancelIconGMPastCheckIn.setImageResource(R.drawable.ic_icons_delete)

        cancelIconGMPastCheckIn.setOnClickListener {
            search_past_action.setIconifiedByDefault(true)
            search_past_action.setQuery("", false)
            setDataGMPastCheckIn()
        }
        search_past_action.onActionViewExpanded()
        search_past_action.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_past_action.clearFocus() }, 300)

        setDataGMPastCheckIn()
    }

    private fun setDataGMPastCheckIn() {
        if (pastCheckInData.checkIns!!.store!!.pastCheckIns.isNotEmpty()) {
            no_past_action_linear.visibility = View.GONE
            past_alerts_rv.visibility = View.VISIBLE
            past_alerts_actions_to_perform_parent.visibility = View.VISIBLE

            gmPastCheckInAdapter =
                GMPastCheckInsListAdapter(this,pastCheckInData.checkIns!!.store!!.pastCheckIns, past_alerts_actions_to_perform_parent)
            past_alerts_rv.adapter = gmPastCheckInAdapter
        } else {
            no_past_action_linear.visibility = View.VISIBLE
            past_alerts_rv.visibility = View.GONE
            past_alerts_actions_to_perform_parent.visibility = View.GONE

        }

    }
}
class GMPastCheckInsListAdapter(
    private var context: Context,
    private var currentActionListData: List<GMCheckInsQuery.PastCheckIn?>,
    private var checkInActionsToPerformParent: LinearLayout,
) :
    RecyclerView.Adapter<GMPastCheckInsListAdapter.MultipleViewHolder>(), Filterable {
    val gson = Gson()
    var onClick: OnItemClickListener? = null
    var currentActionList = listOf<GMCheckInsQuery.PastCheckIn?>()

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClick = mOnItemClickListener
    }

    init {
        currentActionList = currentActionListData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_check_ins_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val data = currentActionList[position]
        viewHolder.pastCheckInDetailsRV.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        viewHolder.pastCheckInTime.text = DateFormatterUtil.formatDateForPastAction(data!!.createdOn!!)
        val gmPastCheckInItemsAdapter =
            GMPastCheckInsDetailListAdapter(context, data.checkInDetails)
        viewHolder.pastCheckInDetailsRV.adapter = gmPastCheckInItemsAdapter

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var pastCheckInTime: TextView = viewHolder.findViewById(R.id.past_check_in_time)
        var pastCheckInDetailsRV: RecyclerView = viewHolder.findViewById(R.id.past_check_in_details_rv)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    currentActionList = currentActionListData
                } else {
                    val resultList = mutableListOf<GMCheckInsQuery.PastCheckIn?>()
                    for (row in currentActionListData) {
                        if (row!!.createdOn!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    currentActionList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = currentActionList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currentActionList = results?.values as List<GMCheckInsQuery.PastCheckIn?>
                if (currentActionList.isEmpty()) {
                    checkInActionsToPerformParent.visibility = View.GONE
                } else {
                    checkInActionsToPerformParent.visibility = View.VISIBLE
                }
                notifyDataSetChanged()
            }

        }
    }

    class GMPastCheckInsDetailListAdapter(
        private var context: Context,
        private var currentActionListData: List<GMCheckInsQuery.CheckInDetail1?>
    ) :
        RecyclerView.Adapter<GMPastCheckInsDetailListAdapter.MultipleViewHolder>(), Filterable {
        val gson = Gson()
        var onClick: OnItemClickListener? = null
        var currentActionList = listOf<GMCheckInsQuery.CheckInDetail1?>()

        fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
            this.onClick = mOnItemClickListener
        }

        init {
            currentActionList = currentActionListData
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.gm_past_check_ins_detail_items, parent, false)
            return MultipleViewHolder(view)
        }


        override fun getItemCount(): Int {
            return currentActionList.size
        }


        override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
            val viewHolder: MultipleViewHolder = holder
            val data = currentActionList[position]
            viewHolder.pastCheckInTitle.text = data?.title
            viewHolder.pastCheckInNarrative.text =
                Html.fromHtml(data?.narrative, Html.FROM_HTML_MODE_COMPACT)

        }


        interface OnItemClickListener {
            fun onItemClick(position: Int)
        }


        inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
            var pastCheckInTitle: TextView = viewHolder.findViewById(R.id.past_check_in_title_detail)
            var pastCheckInNarrative: TextView =
                viewHolder.findViewById(R.id.past_check_in_narrative_detail)

        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        currentActionList = currentActionListData
                    } else {
                        val resultList = mutableListOf<GMCheckInsQuery.CheckInDetail1?>()
                        for (row in currentActionListData) {
                            if (row!!.createdOn!!.trim().toLowerCase(Locale.ROOT)
                                    .contains(charSearch.toLowerCase(Locale.ROOT)) || row.title!!.trim()
                                    .toLowerCase(
                                        Locale.ROOT)
                                    .contains(charSearch.toLowerCase(Locale.ROOT)) || row.narrative!!.toString()
                                    .trim().toLowerCase(
                                    Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        currentActionList = resultList
                    }
                    val filterResults = FilterResults()
                    filterResults.values = currentActionList
                    return filterResults
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    currentActionList = results?.values as List<GMCheckInsQuery.CheckInDetail1?>
                    notifyDataSetChanged()
                }

            }
        }


    }
}