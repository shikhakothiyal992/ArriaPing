package com.arria.ping.ui.actions.checkins

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.kpi.GMCheckInsQuery

class GMCheckInsAdapter(
    private var context: Context,
    private var currentActionList: List<GMCheckInsQuery.CheckInDetail?>,
) :
    RecyclerView.Adapter<GMCheckInsAdapter.MultipleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_check_ins_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val data = currentActionList[position]
        viewHolder.checkInTitle.text = data!!.title
        viewHolder.checkInNarrative.text = Html.fromHtml(data.narrative, Html.FROM_HTML_MODE_COMPACT)
    }

    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var checkInTitle: TextView = viewHolder.findViewById(R.id.check_in_title)
        var checkInNarrative: TextView = viewHolder.findViewById(R.id.check_in_narrative)

    }

}

