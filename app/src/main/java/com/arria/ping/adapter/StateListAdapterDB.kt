package com.arria.ping.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.database.StateListEntity
import com.arria.ping.util.SelectedDataItem
import java.util.*
import kotlin.collections.ArrayList

class StateListAdapterDB(
    private var stateListFilterData: List<StateListEntity>
) :
    RecyclerView.Adapter<StateListAdapterDB.MultipleViewHolder>(), Filterable {

    private var onClickStateList: OnItemClickListener? = null
    private var isAllCheckedStateList = 0
    var stateFilterList = listOf<StateListEntity>()
    var tempSelectedStateListData : ArrayList<Int> = ArrayList()
    var selectedStateListData : List<Int> = ArrayList()

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickStateList = mOnItemClickListener
    }
    fun setAllCheckbox(isAllCheckedFromActivity: Int) {
        this.isAllCheckedStateList = isAllCheckedFromActivity
        notifyDataSetChanged()
    }
    init {
        stateFilterList = stateListFilterData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.store_filter_list_item, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return stateFilterList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val stateListViewHolder: MultipleViewHolder = holder
        val stateListBean = stateFilterList[position]
        stateListViewHolder.mTvName.text = stateListBean.name
        stateListViewHolder.itemView.isSelected = stateListBean.isSelect

        when (isAllCheckedStateList) {
            SelectedDataItem.SELECTED_ITEM.selectedItem -> {
                stateListBean.isSelect = true
                stateListViewHolder.mCheckBox.isChecked =  stateListBean.isSelect
                tempSelectedStateListData.clear()
                for (stateItem in stateFilterList){
                    stateItem.isSelect = true
                    tempSelectedStateListData.add(stateItem.id)
                }
                selectedStateListData = tempSelectedStateListData.toSet().toList()
            }
            SelectedDataItem.UNSELECTED_ITEM.selectedItem  -> {
                stateListBean.isSelect = false
                stateListViewHolder.mCheckBox.isChecked =  stateListBean.isSelect
                tempSelectedStateListData.clear()
            }
            else -> {
                stateListViewHolder.mCheckBox.isChecked = stateListBean.isSelect
                val statesList: ArrayList<Int> = ArrayList()
                if(stateListBean.isSelect){
                    if(tempSelectedStateListData.isEmpty()){
                        tempSelectedStateListData.add(stateListBean.id)
                    }else{
                        for(stateItem in tempSelectedStateListData){
                            if(stateItem != stateListBean.id){
                                statesList.add(stateListBean.id)
                            }
                        }
                        tempSelectedStateListData.addAll(statesList.toSet().toList())
                    }
                }else{
                    tempSelectedStateListData.remove(stateListBean.id)
                }
                selectedStateListData = tempSelectedStateListData.toSet().toList()
            }
        }

        if (onClickStateList != null) stateListViewHolder.mCheckBox.setOnClickListener {
            isAllCheckedStateList = 0
            stateListBean.isSelect = !stateListBean.isSelect
            notifyDataSetChanged()

            if(stateListBean.isSelect){
                tempSelectedStateListData.add(stateListBean.id)
            }else{
                tempSelectedStateListData.remove(stateListBean.id)
            }
            selectedStateListData = tempSelectedStateListData.toSet().toList()
            onClickStateList!!.onItemClick(stateListBean.isSelect,stateListBean.id,stateFilterList.size)
            tempSelectedStateListData.clear()

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
                val charSearchState = constraint.toString()
                stateFilterList = if (charSearchState.isEmpty()) {
                    stateListFilterData
                } else {
                    val resultListState = mutableListOf<StateListEntity>()
                    for (row in stateListFilterData) {
                        if (row.name.trim().toLowerCase(Locale.ROOT).contains(charSearchState.toLowerCase(Locale.ROOT))) {
                            resultListState.add(row)
                        }
                    }
                    resultListState
                }
                val filterResultsState = FilterResults()
                filterResultsState.values = stateFilterList
                return filterResultsState
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                stateFilterList = results?.values as List<StateListEntity>
                notifyDataSetChanged()
            }

        }
    }
}
