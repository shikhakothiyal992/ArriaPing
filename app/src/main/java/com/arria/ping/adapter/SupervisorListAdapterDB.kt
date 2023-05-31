package com.arria.ping.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.database.SuperVisorListEntity
import com.arria.ping.util.SelectedDataItem
import java.util.*

class SupervisorListAdapterDB(
    private var storeFilterData: List<SuperVisorListEntity>
) :
    RecyclerView.Adapter<SupervisorListAdapterDB.MultipleViewHolder>(), Filterable {

    var onClick: OnItemClickListener? = null
    private var isAllCheckedSupervisorList = 0
    var storeFilterList = listOf<SuperVisorListEntity>()
    var tempSelectedSupervisorListData : ArrayList<Int> = ArrayList()
    var selectedSupervisorListData : List<Int> = ArrayList()

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClick = mOnItemClickListener
    }
    fun setAllCheckbox(isAllCheckedFromActivity: Int) {
        this.isAllCheckedSupervisorList = isAllCheckedFromActivity
        notifyDataSetChanged()
    }
    init {
        storeFilterList = storeFilterData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.store_filter_list_item, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return storeFilterList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val bean = storeFilterList[position]
        viewHolder.mTvName.text = bean.name
        viewHolder.itemView.isSelected = bean.isSelect
        when (isAllCheckedSupervisorList) {
            SelectedDataItem.SELECTED_ITEM.selectedItem -> {
                bean.isSelect = true
                viewHolder.mCheckBox.isChecked =  bean.isSelect
                tempSelectedSupervisorListData.clear()
                for (data in storeFilterList){
                    tempSelectedSupervisorListData.add(data.id)
                }
                selectedSupervisorListData = tempSelectedSupervisorListData.toSet().toList()

            }
            SelectedDataItem.UNSELECTED_ITEM.selectedItem -> {
                bean.isSelect = false
                viewHolder.mCheckBox.isChecked =  bean.isSelect
                tempSelectedSupervisorListData.clear()
            }
            else -> {
                viewHolder.mCheckBox.isChecked = bean.isSelect
                val supervisorsList: ArrayList<Int> = ArrayList()
                if(bean.isSelect){
                    if (tempSelectedSupervisorListData.isEmpty()) {
                        tempSelectedSupervisorListData.add(bean.id)
                    } else {
                        for (storeItem in tempSelectedSupervisorListData) {
                            if (storeItem != bean.id) {
                                supervisorsList.add(bean.id)
                            }
                        }
                        tempSelectedSupervisorListData.addAll(supervisorsList.toSet().toList())
                    }
                }else{
                    tempSelectedSupervisorListData.remove(bean.id)
                }
                selectedSupervisorListData = tempSelectedSupervisorListData.toSet().toList()
            }
        }

        if (onClick != null) viewHolder.mCheckBox.setOnClickListener {
            isAllCheckedSupervisorList = 0
            bean.isSelect = !bean.isSelect
            notifyDataSetChanged()

            if(bean.isSelect){
                tempSelectedSupervisorListData.add(bean.id)
            }else{
                tempSelectedSupervisorListData.remove(bean.id)
            }
            selectedSupervisorListData = tempSelectedSupervisorListData.toSet().toList()

            onClick!!.onItemClick(bean.isSelect,bean.id,storeFilterList.size)
            tempSelectedSupervisorListData.clear()

        }
    }


    interface OnItemClickListener {
        fun onItemClick(isChecked: Boolean, id: Int, size: Int)
    }


    inner class MultipleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvName: TextView = itemView.findViewById(R.id.store_filter_id)
        var mCheckBox: CheckBox = itemView.findViewById(R.id.check_box_filter_item)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                storeFilterList = if (charSearch.isEmpty()) {
                    storeFilterData
                } else {
                    val resultList = mutableListOf<SuperVisorListEntity>()
                    for (row in storeFilterData) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = storeFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                storeFilterList = results?.values as List<SuperVisorListEntity>
                notifyDataSetChanged()
            }

        }
    }
}
