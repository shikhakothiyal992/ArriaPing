package com.arria.ping.ui.actions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.arria.ping.R
import java.util.*

class GMPastActionToPerformListAdapter(
    private var context: Context,
    private var currentActionToPerformList: List<String?>,
) :
    RecyclerView.Adapter<GMPastActionToPerformListAdapter.MultipleViewHolder>() {
    val gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_action_to_perform_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentActionToPerformList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val data = currentActionToPerformList[position]
        viewHolder.actionToPerform.text = data

    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var actionToPerform: TextView = viewHolder.findViewById(R.id.action_to_perform)

    }


}
