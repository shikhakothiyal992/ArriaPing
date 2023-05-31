package com.arria.ping.ui.actions

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.arria.ping.R
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_actions.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_header.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_for_action.view.*
import kotlinx.android.synthetic.main.common_header_for_action.view.filter_icon


class BaseActionsActivity : AppCompatActivity() {
    var context: Context? = null
    lateinit var dbHelper: DatabaseHelperImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actions)
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
        setNavigationIcon()
        if (StorePrefData.role == getString(R.string.ceo_text) || StorePrefData.role == getString(R.string.do_text) || StorePrefData.role == getString(
                R.string.supervisor_text)
        ) {
            tabLayout!!.addTab(tabLayout!!.newTab().setText(""))
            tabLayout.setSelectedTabIndicatorHeight(0)
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
            tabLayout.visibility = View.GONE
            tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
            val adapter = ActionAdapter(supportFragmentManager, 1)
            viewPager!!.adapter = adapter
        } else {
            common_header.filter_icon.visibility = View.GONE
            common_header.view_for_gm.visibility = View.VISIBLE
            tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.title_actions_caps)))
            tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.title_alerts)))
            tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.title_check_in)))
            tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
            val adapter = ActionAdapter(supportFragmentManager, tabLayout!!.tabCount)
            viewPager!!.adapter = adapter
        }


        val linearLayoutActionActivity = tabLayout.getChildAt(0) as LinearLayout
        linearLayoutActionActivity.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

        val drawableActionActivity = GradientDrawable()
        drawableActionActivity.setColor(getColor(R.color.tab_divider))
        drawableActionActivity.setSize(1, 1)
        linearLayoutActionActivity.dividerPadding = 1
        linearLayoutActionActivity.dividerDrawable = drawableActionActivity
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener( tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Log.i("Tab Layout","onTabUnselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Log.i("Tab Layout","onTabReselected")
            }
        })

        common_header.date.text = DateFormatterUtil.currentDate()

        common_header.filter_icon.setOnClickListener {
            Validation().openFilter(this)
        }

        common_header.action_allstore_parent.setOnClickListener {
            Validation().openFilter(this)
        }
        action_bottom_navigation_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_bonus -> {
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_bonus)
                    callMainActivity()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_kpis -> {
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_kpis)
                    callMainActivity()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_action -> {
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_actions)
                    callMainActivity()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_phone -> {
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_phone)
                    callMainActivity()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_more -> {
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_settings)
                    callMainActivity()
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateStoreNumber(storeNumberFromGM: String, isFromGM: Boolean) {
         if (isFromGM) {
             period_range.text = "$storeNumberFromGM | Jul 2021 | MTD"
         } else {

        val periodText: String = StorePrefData.isSelectedDate+" | "+ StorePrefData.isSelectedPeriod


        Validation().validateFilterKPI(this, dbHelper, period_range, periodText)

        }

    }

    private fun callMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setNavigationIcon() {
        if ((StorePrefData.role == getString(R.string.ceo_text) || StorePrefData.role == getString(
                R.string.do_text))
        ) {
            action_bottom_navigation_view.menu.getItem(0).isVisible = false
            action_bottom_navigation_view.menu.getItem(2).isChecked = true
        } else {
            action_bottom_navigation_view.menu.getItem(0).isVisible = true
            action_bottom_navigation_view.menu.getItem(2).isChecked = true
        }
    }

}