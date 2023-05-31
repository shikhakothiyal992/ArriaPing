package com.arria.ping.ui.kpi.supervisor.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.model.StoreDetailPojo


class SupervisorYesterdayAndPeriodAdapter(
    private var mYesterdaySupervisorContext: Context,
    private var mYesterdaySupervisorStoreFilterData: List<StoreDetailPojo>,
    private var mYesterdaySupervisorAction: String,
    private var supervisorName: String?,

    ) :
    RecyclerView.Adapter<SupervisorYesterdayAndPeriodAdapter.YesterdaySupervisorMultipleViewHolder>() {

    var onClick: OnItemClickListener? = null


    fun setOnSupervisorYesterdayItemClickListener(listener: OnItemClickListener?) {
        this.onClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YesterdaySupervisorMultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expandable_list_store, parent, false)
        return YesterdaySupervisorMultipleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mYesterdaySupervisorStoreFilterData.size
    }


    override fun onBindViewHolder(holder: YesterdaySupervisorMultipleViewHolder, position: Int) {
        val viewHolder: YesterdaySupervisorMultipleViewHolder = holder
        val mYesterdaySupervisorBean = mYesterdaySupervisorStoreFilterData[position]

        if (!mYesterdaySupervisorBean.storeNumber.isNullOrEmpty()) {
            viewHolder.yesterdaySupervisorStoreName.text = mYesterdaySupervisorBean.storeNumber
            viewHolder.yesterdaySupervisorStoreName.paintFlags =
                viewHolder.yesterdaySupervisorStoreName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            if(mYesterdaySupervisorBean.storeGoal.isNullOrEmpty() && mYesterdaySupervisorBean.storeVariance.isNullOrEmpty() && mYesterdaySupervisorBean.storeActual.isNullOrEmpty()){

                if(mYesterdaySupervisorAction == mYesterdaySupervisorContext.getString(R.string.service_text)){
                    viewHolder.yesterdaySupervisorStoreError.visibility = View.GONE
                }else{
                    viewHolder.yesterdaySupervisorStoreError.visibility = View.VISIBLE
                }

                val paramsSupervisorStoreError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsSupervisorStoreError.weight = 2.0f
                viewHolder.yesterdaySupervisorStoreName.layoutParams = paramsSupervisorStoreError

                viewHolder.yesterdaySupervisorStoreGoal.visibility = View.GONE
                viewHolder.yesterdaySupervisorStoreVariance.visibility = View.GONE
                viewHolder.yesterdaySupervisorStoreActual.visibility = View.GONE

                viewHolder.supervisorStoreGoalLayout.visibility = View.GONE
                viewHolder.supervisorStoreVarianceLayout.visibility = View.GONE
                viewHolder.supervisorStoreActualLayout.visibility = View.GONE

            }else if(!mYesterdaySupervisorBean.storeGoal.isNullOrEmpty() && !mYesterdaySupervisorBean.storeVariance.isNullOrEmpty() && !mYesterdaySupervisorBean.storeActual.isNullOrEmpty()){
                viewHolder.yesterdaySupervisorStoreError.visibility = View.GONE
                viewHolder.yesterdaySupervisorStoreGoal.visibility = View.VISIBLE
                viewHolder.yesterdaySupervisorStoreVariance.visibility = View.VISIBLE
                viewHolder.yesterdaySupervisorStoreActual.visibility = View.VISIBLE

                viewHolder.supervisorStoreGoalError.visibility = View.GONE
                viewHolder.supervisorStoreVarianceError.visibility = View.GONE
                viewHolder.supervisorStoreActualError.visibility = View.GONE

                viewHolder.supervisorStoreGoalLayout.visibility = View.VISIBLE
                viewHolder.supervisorStoreVarianceLayout.visibility = View.VISIBLE
                viewHolder.supervisorStoreActualLayout.visibility = View.VISIBLE
                val paramsSupervisorStores: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsSupervisorStores.weight = 0.94f
                viewHolder.yesterdaySupervisorStoreName.layoutParams = paramsSupervisorStores


                viewHolder.yesterdaySupervisorStoreGoal.text = mYesterdaySupervisorBean.storeGoal
                viewHolder.yesterdaySupervisorStoreVariance.text = mYesterdaySupervisorBean.storeVariance
                viewHolder.yesterdaySupervisorStoreActual.text = mYesterdaySupervisorBean.storeActual
            }else{
                val paramsSupervisorStore: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsSupervisorStore.weight = 0.94f
                viewHolder.yesterdaySupervisorStoreName.layoutParams = paramsSupervisorStore

                viewHolder.yesterdaySupervisorStoreError.visibility = View.GONE

                if (!mYesterdaySupervisorBean.storeGoal.isNullOrEmpty()) {
                    viewHolder.yesterdaySupervisorStoreGoal.text = mYesterdaySupervisorBean.storeGoal
                    viewHolder.yesterdaySupervisorStoreGoal.visibility = View.VISIBLE
                    viewHolder.supervisorStoreGoalError.visibility = View.GONE

                }else{
                    viewHolder.yesterdaySupervisorStoreGoal.visibility = View.INVISIBLE
                    viewHolder.supervisorStoreGoalError.visibility = View.VISIBLE
                 }

                if (!mYesterdaySupervisorBean.storeVariance.isNullOrEmpty()) {
                    viewHolder.yesterdaySupervisorStoreVariance.text = mYesterdaySupervisorBean.storeVariance
                    viewHolder.yesterdaySupervisorStoreVariance.visibility = View.VISIBLE
                    viewHolder.supervisorStoreVarianceError.visibility = View.GONE

                }else{
                    viewHolder.yesterdaySupervisorStoreVariance.visibility = View.INVISIBLE
                    viewHolder.supervisorStoreVarianceError.visibility = View.VISIBLE
                }
                if (!mYesterdaySupervisorBean.storeActual.isNullOrEmpty()) {
                    viewHolder.yesterdaySupervisorStoreActual.text = mYesterdaySupervisorBean.storeActual
                    viewHolder.yesterdaySupervisorStoreActual.visibility = View.VISIBLE
                    viewHolder.supervisorStoreActualError.visibility = View.GONE

                }else{
                    viewHolder.yesterdaySupervisorStoreActual.visibility = View.INVISIBLE
                    viewHolder.supervisorStoreActualError.visibility = View.VISIBLE
                }

            }
            if (!mYesterdaySupervisorBean.storeActual.isNullOrEmpty() && !mYesterdaySupervisorBean.status.isNullOrEmpty()) {
                viewHolder.yesterdaySupervisorStoreActual.text = mYesterdaySupervisorBean.storeActual
                when (mYesterdaySupervisorBean.status) {
                    mYesterdaySupervisorContext.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        viewHolder.yesterdaySupervisorStoreActual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        viewHolder.yesterdaySupervisorStoreActual.setTextColor(mYesterdaySupervisorContext.getColor(R.color.red))

                    }
                    mYesterdaySupervisorContext.resources.getString(
                        R.string.under_limit
                    ) -> {
                        viewHolder.yesterdaySupervisorStoreActual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        viewHolder.yesterdaySupervisorStoreActual.setTextColor(mYesterdaySupervisorContext.getColor(R.color.green))

                    } else -> {
                        viewHolder.yesterdaySupervisorStoreActual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        viewHolder.yesterdaySupervisorStoreActual.setTextColor(mYesterdaySupervisorContext.getColor(R.color.text_color))

                    }
                }
            }

        }


        if (onClick != null) viewHolder.yesterdaySupervisorStoreParent.setOnClickListener {
            if (mYesterdaySupervisorAction == mYesterdaySupervisorContext.getString(R.string.service_text)) {
                onClick!!.onItemClick(position, mYesterdaySupervisorBean.storeNumber, mYesterdaySupervisorAction)
            } else {
                if (mYesterdaySupervisorBean.storeGoal?.isNotEmpty() == true || mYesterdaySupervisorBean.storeVariance?.isNotEmpty() == true || mYesterdaySupervisorBean.storeActual?.isNotEmpty() == true) {
                    onClick!!.onItemClick(position, mYesterdaySupervisorBean.storeNumber, mYesterdaySupervisorAction)
                }
            }
            Logger.info("Supervisor $supervisorName selected", "Supervisor KPI")
        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, storeNumber: String?, action: String)
    }

    inner class YesterdaySupervisorMultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var yesterdaySupervisorStoreName: TextView = viewHolder.findViewById(R.id.store_name)
        var yesterdaySupervisorStoreGoal: TextView = viewHolder.findViewById(R.id.store_goal)
        var yesterdaySupervisorStoreVariance: TextView = viewHolder.findViewById(R.id.store_variance)
        var yesterdaySupervisorStoreActual: TextView = viewHolder.findViewById(R.id.store_actual)
        var yesterdaySupervisorStoreParent: LinearLayout = viewHolder.findViewById(R.id.store_parent)
        var yesterdaySupervisorStoreError: TextView = viewHolder.findViewById(R.id.store_error_ceo_period_range_kpi)

        var supervisorStoreGoalLayout: LinearLayout = viewHolder.findViewById(R.id.store_goal_layout)
        var supervisorStoreVarianceLayout: LinearLayout = viewHolder.findViewById(R.id.store_variance_layout)
        var supervisorStoreActualLayout: LinearLayout = viewHolder.findViewById(R.id.store_actual_layout)

        var supervisorStoreGoalError: TextView = viewHolder.findViewById(R.id.store_goal_error)
        var supervisorStoreVarianceError: TextView = viewHolder.findViewById(R.id.store_variance_error)
        var supervisorStoreActualError: TextView = viewHolder.findViewById(R.id.store_actual_error)


    }

}
