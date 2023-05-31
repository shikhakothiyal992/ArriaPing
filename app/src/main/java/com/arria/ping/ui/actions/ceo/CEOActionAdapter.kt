package com.arria.ping.ui.actions.ceo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.kpi.CEOActionQuery

class CEOActionAdapter(
    private var mContext: Context,
    private var ceoCurrentActionList: List<CEOActionQuery.Store?>,
) :
    RecyclerView.Adapter<CEOActionAdapter.MultipleViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return ceoCurrentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val ceoViewHolder: MultipleViewHolder = holder
        val ceoData = ceoCurrentActionList[position]
        ceoViewHolder.ceoStoreName.text = ceoData!!.storeNumber + "-" + ceoData.storeName

        ceoViewHolder.ceoActionMetricsRv.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        val ceoCurrentActionMetricsDisplayNameAdapter =
            CEOCurrentActionMetricsDisplayNameAdapter(mContext, ceoCurrentActionList[position]!!.currentActions)
        ceoViewHolder.ceoActionMetricsRv.adapter = ceoCurrentActionMetricsDisplayNameAdapter

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, storeNumber: String?, action: String)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var ceoStoreName: TextView = viewHolder.findViewById(R.id.store_name)
        var ceoActionMetricsRv: RecyclerView = viewHolder.findViewById(R.id.action_metrics_rv)

    }

}
