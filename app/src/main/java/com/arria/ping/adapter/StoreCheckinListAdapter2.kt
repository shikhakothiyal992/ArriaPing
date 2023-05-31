package com.arria.ping.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import kotlinx.android.synthetic.main.check_in_item.view.*

class StoreCheckinListAdapter2(
    val context: FragmentActivity,
    val userList: List<String>,
    val checkInHour: String?
) : RecyclerView.Adapter<StoreCheckinListAdapter2.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreCheckinListAdapter2.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.check_in_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: StoreCheckinListAdapter2.ViewHolder, position: Int) {
        holder.bindItems(context, userList[position],checkInHour,position)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindItems(
            context: FragmentActivity,
            checkInPoint: String,
            checkInHour: String?,
            position: Int
        ) {
            itemView.check_in_item.text=checkInPoint.toString()

            if(checkInHour == checkInPoint.replace("PM","")){
                val typefaceBold = context.resources.getFont(R.font.sf_ui_text_bold)
                itemView.check_in_item.background = getDrawable(context,R.drawable.checkin_buton_background)
                itemView.check_in_item.setTextColor(context.getColor(R.color.white))
                itemView.check_in_item.typeface = typefaceBold
            }else{
                itemView.check_in_item.background = null
                itemView.check_in_item.setTextColor(context.getColor(R.color.text_color))
            }
            if(position == 1 ){
                itemView.check_in_item.background = getDrawable(context,R.drawable.checkin_buton_background_light_grey)
                itemView.check_in_item.setTextColor(context.getColor(R.color.black))
            }

        }
    }

}