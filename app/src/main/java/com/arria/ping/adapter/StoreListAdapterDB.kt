package com.arria.ping.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.database.StoreListEntity
import com.arria.ping.util.SelectedDataItem
import java.util.*

class StoreListAdapterDB(
    private var storeDBFilterData: List<StoreListEntity>
) :
    RecyclerView.Adapter<StoreListAdapterDB.MultipleViewHolder>(), Filterable {

    var onClick: OnItemClickListener? = null
    var isAllChecked = 0
    var storeDBFilterList = listOf<StoreListEntity>()
    var tempSelectedStoreListData : ArrayList<Int> = ArrayList()
    var selectedStoreListData : List<Int> = ArrayList()


    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClick = mOnItemClickListener
    }
    init {
        storeDBFilterList = storeDBFilterData
    }

    fun setAllStoreDBCheckbox(isAllCheckedFromActivity: Int) {
        this.isAllChecked = isAllCheckedFromActivity
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.store_filter_list_item, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return storeDBFilterList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolderStoreDB: MultipleViewHolder = holder
        val beanStoreDB = storeDBFilterList[position]
        viewHolderStoreDB.mTvNameStoreDB.text = beanStoreDB.storeNumber
        viewHolderStoreDB.itemView.isSelected = beanStoreDB.isSelect
        when (isAllChecked) {
            SelectedDataItem.SELECTED_ITEM.selectedItem -> {
                beanStoreDB.isSelect = true
                viewHolderStoreDB.mCheckBoxStoreDB.isChecked =  beanStoreDB.isSelect
                tempSelectedStoreListData.clear()
                for (data in storeDBFilterList){
                    data.isSelect = true
                    tempSelectedStoreListData.add(data.id)
                }
                selectedStoreListData = tempSelectedStoreListData.toSet().toList()

            }
            SelectedDataItem.UNSELECTED_ITEM.selectedItem -> {
                beanStoreDB.isSelect = false
                viewHolderStoreDB.mCheckBoxStoreDB.isChecked =  beanStoreDB.isSelect
                tempSelectedStoreListData.clear()
            }
            else -> {
                viewHolderStoreDB.mCheckBoxStoreDB.isChecked = beanStoreDB.isSelect
                val storesList: ArrayList<Int> = ArrayList()
                if(beanStoreDB.isSelect){
                    if(tempSelectedStoreListData.isEmpty()){
                        tempSelectedStoreListData.add(beanStoreDB.id)
                    }else{
                        for(storeItem in tempSelectedStoreListData){
                            if(storeItem != beanStoreDB.id){
                                storesList.add(beanStoreDB.id)
                            }
                        }
                        tempSelectedStoreListData.addAll(storesList.toSet().toList())
                    }

                }else{
                    tempSelectedStoreListData.remove(beanStoreDB.id)
                }
                selectedStoreListData = tempSelectedStoreListData.toSet().toList()
            }
        }

        if (onClick != null) viewHolderStoreDB.mCheckBoxStoreDB.setOnClickListener {
            isAllChecked = 0
            beanStoreDB.isSelect = !beanStoreDB.isSelect
            notifyDataSetChanged()

            if(beanStoreDB.isSelect){
                tempSelectedStoreListData.add(beanStoreDB.id)
            }else{
                tempSelectedStoreListData.remove(beanStoreDB.id)
            }
            selectedStoreListData = tempSelectedStoreListData.toSet().toList()

            onClick!!.onItemClick(beanStoreDB.isSelect,beanStoreDB.id,storeDBFilterList.size)
            tempSelectedStoreListData.clear()
        }
    }


    interface OnItemClickListener {
        fun onItemClick(isStoreDBChecked: Boolean, id: Int, size: Int)
    }


    inner class MultipleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvNameStoreDB: TextView = itemView.findViewById(R.id.store_filter_id)
        var mCheckBoxStoreDB: CheckBox = itemView.findViewById(R.id.check_box_filter_item)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchStoreDB = constraint.toString()
                storeDBFilterList = if (charSearchStoreDB.isEmpty()) {
                    storeDBFilterData
                } else {
                    val resultListStoreDB = mutableListOf<StoreListEntity>()
                    for (row in storeDBFilterData) {
                        if (row.storeNumber.trim().toLowerCase(Locale.ROOT).contains(charSearchStoreDB.toLowerCase(Locale.ROOT))) {
                            resultListStoreDB.add(row)
                        }
                    }
                    resultListStoreDB

                }
                val filterResultsStoreDB = FilterResults()
                filterResultsStoreDB.values = storeDBFilterList
                return filterResultsStoreDB
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                storeDBFilterList = results?.values as List<StoreListEntity>
                notifyDataSetChanged()
            }

        }
    }
}
