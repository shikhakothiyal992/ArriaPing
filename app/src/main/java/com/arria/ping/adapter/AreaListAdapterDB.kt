package com.arria.ping.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.database.AreaListEntity
import com.arria.ping.util.SelectedDataItem
import java.util.*

class AreaListAdapterDB(
    private var areaListFilterData: List<AreaListEntity>
) :
    RecyclerView.Adapter<AreaListAdapterDB.MultipleViewHolder>(), Filterable {

    var onClick: OnItemClickListener? = null
    private var isAllCheckedArea = 0
    var areaFilterList = listOf<AreaListEntity>()
    var tempSelectedAreaListData : ArrayList<Int> = ArrayList()
    var selectedAreaListData : List<Int> = ArrayList()

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClick = mOnItemClickListener
    }
    fun setAllAreaCheckbox(isAllCheckedFromActivity: Int) {
        this.isAllCheckedArea = isAllCheckedFromActivity
        notifyDataSetChanged()
    }
    init {
        areaFilterList = areaListFilterData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.store_filter_list_item, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {

        return areaFilterList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolderArea: MultipleViewHolder = holder
        val beanArea = areaFilterList[position]

        viewHolderArea.mTvNameArea.text = beanArea.name
        viewHolderArea.itemView.isSelected = beanArea.isSelect

        when (isAllCheckedArea) {
            SelectedDataItem.SELECTED_ITEM.selectedItem -> {
                beanArea.isSelect = true
                viewHolderArea.mCheckBoxArea.isChecked =  beanArea.isSelect
                tempSelectedAreaListData.clear()
                for (areaItem in areaFilterList){
                    areaItem.isSelect = true
                    tempSelectedAreaListData.add(areaItem.id)
                }
                selectedAreaListData = tempSelectedAreaListData.toSet().toList()

            }
            SelectedDataItem.UNSELECTED_ITEM.selectedItem -> {
                beanArea.isSelect = false
                viewHolderArea.mCheckBoxArea.isChecked =  beanArea.isSelect
                tempSelectedAreaListData.clear()
            }
            else -> {
                viewHolderArea.mCheckBoxArea.isChecked = beanArea.isSelect
                val areasList: ArrayList<Int> = ArrayList()
                if(beanArea.isSelect){
                    if(tempSelectedAreaListData.isEmpty()){
                        tempSelectedAreaListData.add(beanArea.id)
                    }else{
                        for(areaItem in tempSelectedAreaListData){
                            if(areaItem != beanArea.id){
                                areasList.add(beanArea.id)
                            }
                        }
                        tempSelectedAreaListData.addAll(areasList.toSet().toList())
                    }

                }else{
                    tempSelectedAreaListData.remove(beanArea.id)
                }
                selectedAreaListData = tempSelectedAreaListData.toSet().toList()
            }
        }

        if (onClick != null) viewHolderArea.mCheckBoxArea.setOnClickListener {
            isAllCheckedArea = 0
            beanArea.isSelect = !beanArea.isSelect
            notifyDataSetChanged()

            if(beanArea.isSelect){
                tempSelectedAreaListData.add(beanArea.id)
            }else{
                tempSelectedAreaListData.remove(beanArea.id)
            }
            selectedAreaListData = tempSelectedAreaListData.toSet().toList()

            onClick!!.onItemClick(beanArea.isSelect,beanArea.id,areaFilterList.size)
            tempSelectedAreaListData.clear()

        }
    }


    interface OnItemClickListener {
        fun onItemClick(isAreaChecked: Boolean, id: Int, size: Int)
    }


    inner class MultipleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvNameArea: TextView = itemView.findViewById(R.id.store_filter_id)
        var mCheckBoxArea: CheckBox = itemView.findViewById(R.id.check_box_filter_item)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchArea = constraint.toString()
                areaFilterList = if (charSearchArea.isEmpty()) {
                    areaListFilterData
                } else {
                    val resultListArea = mutableListOf<AreaListEntity>()
                    for (row in areaListFilterData) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearchArea.toLowerCase(Locale.ROOT))) {
                            resultListArea.add(row)
                        }
                    }
                    resultListArea
                }
                val filterResultsArea = FilterResults()
                filterResultsArea.values = areaFilterList
                return filterResultsArea
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                areaFilterList = results?.values as List<AreaListEntity>
                notifyDataSetChanged()
            }

        }
    }
}
