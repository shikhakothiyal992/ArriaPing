package com.arria.ping.ui.actions.do_

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.kpi.DOActionQuery

class DOActionAdapter(
    private var context: Context,
    private var doCurrentActionList: List<DOActionQuery.Store?>,
) :
    RecyclerView.Adapter<DOActionAdapter.MultipleViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return doCurrentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val doViewHolder: MultipleViewHolder = holder
        val doData = doCurrentActionList[position]
        doViewHolder.doStoreName.text = doData!!.storeNumber + "-" + doData.storeName

        doViewHolder.doActionMetricsRv.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        val doCurrentActionMetricsDisplayNameAdapter =
            DOCurrentActionMetricsDisplayNameAdapter(context, doCurrentActionList[position]!!.currentActions)
        doViewHolder.doActionMetricsRv.adapter = doCurrentActionMetricsDisplayNameAdapter

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, storeNumber: String?, action: String)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var doStoreName: TextView = viewHolder.findViewById(R.id.store_name)
        var doActionMetricsRv: RecyclerView = viewHolder.findViewById(R.id.action_metrics_rv)

    }

}
