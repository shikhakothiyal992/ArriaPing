package com.arria.ping.ui.actions

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.arria.ping.R
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.common_header_for_action.view.*
import kotlinx.android.synthetic.main.fragment_actions.*


class BaseActionsFragment : Fragment()  {

    lateinit var dbHelper: DatabaseHelperImpl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialise()

    }
    private fun initialise(){
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(activity!!))

        if (StorePrefData.role == getString(R.string.ceo_text) || StorePrefData.role == getString(R.string.do_text) || StorePrefData.role == getString(
                R.string.supervisor_text)
        ) {
            tabLayoutFrag!!.addTab(tabLayoutFrag!!.newTab().setText(""))
            tabLayoutFrag.setSelectedTabIndicatorHeight(0)
            tabLayoutFrag.setSelectedTabIndicatorColor(activity!!.getColor(R.color.white))
            tabLayoutFrag.visibility = View.GONE
            tabLayoutFrag!!.tabGravity = TabLayout.GRAVITY_FILL

            val adapter = ActionAdapter(activity!!.supportFragmentManager, 1)

            viewPagerFrag!!.adapter = adapter
        } else {

            common_header_frag.filter_icon.visibility = View.GONE
            common_header_frag.view_for_gm.visibility = View.VISIBLE
            tabLayoutFrag!!.addTab(tabLayoutFrag!!.newTab().setText(getString(R.string.title_actions_caps)))
            tabLayoutFrag!!.addTab(tabLayoutFrag!!.newTab().setText(getString(R.string.title_alerts)))
            tabLayoutFrag!!.addTab(tabLayoutFrag!!.newTab().setText(getString(R.string.title_check_in)))
            tabLayoutFrag!!.tabGravity = TabLayout.GRAVITY_FILL
            val adapter = ActionAdapter(activity!!.supportFragmentManager, tabLayoutFrag!!.tabCount)
            viewPagerFrag!!.adapter = adapter
        }


        val linearLayoutBaseActionFragment = tabLayoutFrag!!.getChildAt(0) as LinearLayout
        linearLayoutBaseActionFragment.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

        val drawableBaseActionFragment = GradientDrawable()
        drawableBaseActionFragment.setColor(activity!!.getColor(R.color.tab_divider))
        drawableBaseActionFragment.setSize(1, 1)
        linearLayoutBaseActionFragment.dividerPadding = 1
        linearLayoutBaseActionFragment.dividerDrawable = drawableBaseActionFragment
        viewPagerFrag!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayoutFrag))

        tabLayoutFrag!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPagerFrag!!.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Log.i("BaseActionsFragment","onTabUnselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Log.i("BaseActionsFragment","onTabReselected")
            }
        })
        common_header_frag.date.text = DateFormatterUtil.currentDate()

        common_header_frag.filter_icon.setOnClickListener {
            Validation().openFilter(activity!!)
        }
    }
}