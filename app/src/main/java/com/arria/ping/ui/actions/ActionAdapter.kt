package com.arria.ping.ui.actions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arria.ping.ui.actions.alerts.AlertsFragment
import com.arria.ping.ui.actions.checkins.CheckInFragment

class ActionAdapter(fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ActionsFragment()
            }
            1 -> {
                AlertsFragment()
            }
            2 -> {
                CheckInFragment()
            }
            else -> ActionsFragment()
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}