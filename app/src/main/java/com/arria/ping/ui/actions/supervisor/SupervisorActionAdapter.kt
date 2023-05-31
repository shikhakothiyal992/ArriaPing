package com.arria.ping.ui.actions.supervisor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.kpi.SupervisorActionQuery
import com.arria.ping.ui.actions.supervisor.SupervisorCurrentActionMetricsDisplayNameAdapter

class SupervisorActionAdapter(
    private var context: Context,
    private var currentActionList: List<SupervisorActionQuery.Store?>,
) :
    RecyclerView.Adapter<SupervisorActionAdapter.MultipleViewHolder>() {

    var onClick: OnItemClickListener? = null

    fun setOnItemClickLitener(mOnItemClickLitener: OnItemClickListener?) {
        this.onClick = mOnItemClickLitener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val data = currentActionList[position]
        viewHolder.storeName.text = data!!.storeNumber + "-" + data!!.storeName

        viewHolder.actionMetricsRv.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        val ceoCurrentActionMetricsDisplayNameAdapter = SupervisorCurrentActionMetricsDisplayNameAdapter(context, currentActionList[position]!!.currentActions)
        viewHolder.actionMetricsRv.adapter = ceoCurrentActionMetricsDisplayNameAdapter

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, storeNumber: String?, action: String)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var storeName: TextView = viewHolder.findViewById(R.id.store_name)
        var actionMetricsRv: RecyclerView = viewHolder.findViewById(R.id.action_metrics_rv)

    }

}
