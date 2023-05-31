package com.arria.ping.ui.filter

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.arria.ping.R
import com.arria.ping.adapter.*
import com.arria.ping.apollo.apolloClientProfile
import com.arria.ping.database.*
import com.arria.ping.log.Logger
import com.arria.ping.profile.*
import com.arria.ping.ui.generalview.BaseActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.activity_login_activty.*
import kotlinx.android.synthetic.main.activity_store_filter.*
import kotlinx.android.synthetic.main.activity_store_filter.btn_apply
import kotlinx.android.synthetic.main.activity_store_filter.common_header_filter
import kotlinx.android.synthetic.main.common_header_filter.*
import kotlinx.android.synthetic.main.common_header_filter.view.*
import kotlinx.android.synthetic.main.common_header_filter_storelist_items.view.*
import java.util.*

@AndroidEntryPoint
class StoreFilterActivity : BaseActivity() {
    private var storeListAdapterDBStoreFilter: StoreListAdapterDB? = null
    private var areaListAdapterDBStoreFilter : AreaListAdapterDB? = null
    private var stateListAdapterDBStoreFilter: StateListAdapterDB? = null
    private var supervisorListAdapterDBStoreFilter: SupervisorListAdapterDB? = null
    private var storeFilterErrorDialog: Dialog? = null
    private val refreshTokenStoreFilter by viewModels<RefreshTokenViewModel>()


    var userFilterAction = ""
    lateinit var dbHelperStoreFilter: DatabaseHelperImpl
    var isAreaChangedStoreFilter = false
    private var isAnySelectionChangedStoreFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_filter)
        dbHelperStoreFilter = DatabaseHelperImpl(DatabaseBuilder.getInstance(applicationContext))

        getIntentData()

        callStoreFilterApi()
        lifecycleScope.launchWhenResumed {

            when (userFilterAction) {
                getString(R.string.area_text) -> {

                    check_box_select_all_store.isChecked =
                            dbHelperStoreFilter.getUsersArea().size == dbHelperStoreFilter.getAllSelectedAreaList(
                                    true
                            ).size && dbHelperStoreFilter.getUsersArea()
                                    .isNotEmpty() && dbHelperStoreFilter.getAllSelectedAreaList(true)
                                    .isNotEmpty()
                }

                getString(R.string.store_text) -> {
                    check_box_select_all_store.isChecked = dbHelperStoreFilter.getUsers().size == dbHelperStoreFilter.getAllSelectedStoreList(
                            true
                    ).size && dbHelperStoreFilter.getUsers()
                            .isNotEmpty() && dbHelperStoreFilter.getAllSelectedStoreList(true)
                            .isNotEmpty()
                }

                getString(R.string.state_text) -> {
                    check_box_select_all_store.isChecked =
                            dbHelperStoreFilter.getUsersState().size == dbHelperStoreFilter.getAllSelectedStoreListState(
                                    true
                            ).size && dbHelperStoreFilter.getUsersState()
                                    .isNotEmpty() && dbHelperStoreFilter.getAllSelectedStoreListState(true)
                                    .isNotEmpty()

                }

                getString(R.string.supervisor_text) -> {
                    check_box_select_all_store.isChecked =
                            dbHelperStoreFilter.getUsersSupervisor().size == dbHelperStoreFilter.getAllSelectedStoreListSupervisor(
                                    true
                            ).size && dbHelperStoreFilter.getUsersSupervisor()
                                    .isNotEmpty() && dbHelperStoreFilter.getAllSelectedStoreListSupervisor(
                                    true
                            )
                                    .isNotEmpty()

                }
                else -> {
                    check_box_select_all_store.isChecked = false
                }
            }
        }
        check_box_select_all_store.isClickable = false
        select_all_parent.setOnClickListener {
            val isChecked: Boolean = !check_box_select_all_store.isChecked

            if (isChecked) {
                check_box_select_all_store.isChecked = true
                when (userFilterAction) {
                    getString(R.string.area_text) -> {
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                        StorePrefData.isAreaChanged = true
                        StorePrefData.isAreaSelected = true
                        isAnySelectionChangedStoreFilter = getString(R.string.area_text)
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelectionArea(isSelect = true)
                        }
                        areaListAdapterDBStoreFilter?.setAllAreaCheckbox(SelectedDataItem.SELECTED_ITEM.selectedItem)
                    }
                    getString(R.string.store_text) -> {
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                        StorePrefData.isStoreSelected = true
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelection(isSelect = true)
                        }
                        storeListAdapterDBStoreFilter?.setAllStoreDBCheckbox(SelectedDataItem.SELECTED_ITEM.selectedItem)
                    }
                    getString(R.string.state_text) -> {
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                        StorePrefData.isStateSelected = true
                        isAnySelectionChangedStoreFilter = getString(R.string.state_text)
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelectionState(isSelect = true)
                        }
                        stateListAdapterDBStoreFilter?.setAllCheckbox(SelectedDataItem.SELECTED_ITEM.selectedItem)
                    }
                    getString(R.string.supervisor_text) -> {
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                        StorePrefData.isSupervisorSelected = true
                        isAnySelectionChangedStoreFilter = getString(R.string.supervisor_text)
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelectionSupervisor(isSelect = true)
                        }
                        supervisorListAdapterDBStoreFilter?.setAllCheckbox(SelectedDataItem.SELECTED_ITEM.selectedItem)
                    }
                }

            } else {
                check_box_select_all_store.isChecked = false
                when (userFilterAction) {
                    getString(R.string.area_text) -> {
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                        StorePrefData.isAreaChanged = true
                        StorePrefData.isAreaSelected = false
                        isAnySelectionChangedStoreFilter = getString(R.string.area_text)
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelectionArea(isSelect = false)
                        }
                        areaListAdapterDBStoreFilter?.setAllAreaCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)

                    }
                    getString(R.string.store_text) -> {
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                        StorePrefData.isStoreSelected = false
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelection(isSelect = false)
                        }
                        storeListAdapterDBStoreFilter?.setAllStoreDBCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)
                    }
                    getString(R.string.state_text) -> {
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                        StorePrefData.isStateSelected = false
                        isAnySelectionChangedStoreFilter = getString(R.string.state_text)
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelectionState(isSelect = false)
                        }
                        stateListAdapterDBStoreFilter?.setAllCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)
                    }
                    getString(R.string.supervisor_text) -> {
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                        StorePrefData.isSupervisorSelected = false
                        isAnySelectionChangedStoreFilter = getString(R.string.supervisor_text)
                        lifecycleScope.launchWhenResumed {
                            dbHelperStoreFilter.updateAllStoreListSelectionSupervisor(isSelect = false)
                        }
                        supervisorListAdapterDBStoreFilter?.setAllCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)
                    }
                }
            }
        }
        search_store.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                search_store.setPadding(-10, 0, 0, 0)
                select_all_parent.visibility = View.GONE
                checkbox_below_view.visibility = View.GONE
                search_store.onActionViewExpanded()
                search_store.setIconifiedByDefault(true)
                if (userFilterAction == getString(R.string.area_text)) {
                    if (query!!.isEmpty()) {
                        setRecycler()
                    } else {
                        areaListAdapterDBStoreFilter?.filter?.filter(query)
                    }
                } else if (userFilterAction == getString(R.string.store_text)) {
                    if (query!!.isEmpty()) {
                        setRecycler()
                    } else {
                        storeListAdapterDBStoreFilter?.filter?.filter(query)
                    }
                } else if (userFilterAction == getString(R.string.state_text)) {
                    if (query!!.isEmpty()) {
                        setRecycler()
                    } else {
                        stateListAdapterDBStoreFilter?.filter?.filter(query)
                    }
                } else if (userFilterAction == getString(R.string.supervisor_text)) {
                    if (query!!.isEmpty()) {
                        setRecycler()
                    } else {
                        supervisorListAdapterDBStoreFilter?.filter?.filter(query)
                    }
                }


                return false
            }

        })
        val cancelIcon = search_store.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setImageResource(R.drawable.ic_icons_delete)

        cancelIcon.setOnClickListener {
            search_store.setIconifiedByDefault(true)
            search_store.setQuery("", false)
            select_all_parent.visibility = View.VISIBLE
            checkbox_below_view.visibility = View.VISIBLE
            check_box_select_all_store.isChecked = false
            setRecycler()
        }
        common_header_filter.reset_text_layout.setOnClickListener {
            Logger.info("Reset Button clicked","Store Filter")
            check_box_select_all_store.isChecked = false
            reset_text.setTextColor(getColor(R.color.reset_text_color))
            reset_text.isFocusableInTouchMode = false
            when (userFilterAction) {
                getString(R.string.area_text) -> {
                    StorePrefData.isAreaChanged = true
                    StorePrefData.isAreaSelected = false
                    StorePrefData.isAreaSelectedDone = false
                    isAnySelectionChangedStoreFilter = getString(R.string.area_text)
                    lifecycleScope.launchWhenResumed {
                        dbHelperStoreFilter.updateAllStoreListSelectionArea(isSelect = false)
                    }
                    areaListAdapterDBStoreFilter?.setAllAreaCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)

                }
                getString(R.string.store_text) -> {
                    StorePrefData.isStoreSelected = false
                    StorePrefData.isStoreSelectedDone = false

                    lifecycleScope.launchWhenResumed {
                        dbHelperStoreFilter.updateAllStoreListSelection(isSelect = false)
                    }
                    storeListAdapterDBStoreFilter?.setAllStoreDBCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)

                }
                getString(R.string.state_text) -> {
                    StorePrefData.isStateSelected = false
                    StorePrefData.isStateSelectedDone = false
                    isAnySelectionChangedStoreFilter = getString(R.string.state_text)
                    lifecycleScope.launchWhenResumed {
                        dbHelperStoreFilter.updateAllStoreListSelectionState(isSelect = false)
                    }
                    stateListAdapterDBStoreFilter?.setAllCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)

                }
                getString(R.string.supervisor_text) -> {
                    StorePrefData.isSupervisorSelected = false
                    StorePrefData.isSupervisorSelectedDone = false
                    isAnySelectionChangedStoreFilter = getString(R.string.supervisor_text)
                    lifecycleScope.launchWhenResumed {
                        dbHelperStoreFilter.updateAllStoreListSelectionSupervisor(isSelect = false)
                    }
                    supervisorListAdapterDBStoreFilter?.setAllCheckbox(SelectedDataItem.UNSELECTED_ITEM.selectedItem)

                }
            }
        }
        common_header_filter.cross_filter_img_layout.setOnClickListener {
            Logger.info("Cancel Button clicked","Store Filter")
            callFilterActivity()
        }

        btn_apply.setOnClickListener {
            when (userFilterAction) {
                getString(R.string.area_text) -> {
                    lifecycleScope.launchWhenResumed {
                        if (dbHelperStoreFilter.getAllSelectedAreaList(true).isNotEmpty()) {
                            StorePrefData.isAreaSelectedDone = true
                            callFilterActivity()
                        } else {
                            callFilterActivity()
                        }
                    }
                    Logger.info("Apply Button clicked","Restaurant Group Filter")
                }
                getString(R.string.store_text) -> {
                    lifecycleScope.launchWhenResumed {
                        if (dbHelperStoreFilter.getAllSelectedStoreList(true).isNotEmpty()) {
                            StorePrefData.isStoreSelectedDone = true
                            callFilterActivity()
                        } else {
                            callFilterActivity()

                        }
                    }
                    Logger.info("Apply Button clicked","Restaurant Filter")
                }
                getString(R.string.state_text) -> {
                    lifecycleScope.launchWhenResumed {
                        if (dbHelperStoreFilter.getAllSelectedStoreListState(true).isNotEmpty()) {
                            StorePrefData.isStateSelectedDone = true
                            callFilterActivity()
                        } else {
                            callFilterActivity()
                        }
                    }
                    Logger.info("Apply Button clicked","State Filter")
                }
                getString(R.string.supervisor_text) -> {
                    lifecycleScope.launchWhenResumed {
                        if (dbHelperStoreFilter.getAllSelectedStoreListSupervisor(true).isNotEmpty()) {
                            StorePrefData.isSupervisorSelectedDone = true
                            callFilterActivity()
                        } else {
                            callFilterActivity()
                        }
                    }
                    Logger.info("Apply Button clicked","Supervisor Filter")
                }
            }
        }


    }

    private fun getIntentData() {
        hideKeyBoard()
        search_store.onActionViewExpanded()
        search_store.setIconifiedByDefault(true)
        Handler(Looper.getMainLooper()).postDelayed({ search_store.clearFocus() }, 300)
        reset_text.isFocusableInTouchMode = false
        val v: View = search_store.findViewById(R.id.search_plate)
        v.setBackgroundColor(Color.TRANSPARENT)

        if (intent.hasExtra("action")) {
            userFilterAction = intent.getStringExtra("action")!!
        }
        when (userFilterAction) {
            getString(R.string.area_text) -> {
                check_box_select_all_store.isChecked = StorePrefData.isAreaSelected && StorePrefData.isAreaSelectedDone
                select_text.text = getString(R.string.select_all_area)
            }
            getString(R.string.store_text) -> {
                if (StorePrefData.isStoreSelected && StorePrefData.isStoreSelectedDone) {
                    check_box_select_all_store.isChecked =
                            !(StorePrefData.isAreaChanged || StorePrefData.isStateChanged || StorePrefData.isSupervisorChanged)
                } else {
                    check_box_select_all_store.isChecked = false
                }

                select_text.text = getString(R.string.select_all_store)
            }
            getString(R.string.state_text) -> {
                if (StorePrefData.isStateSelected && StorePrefData.isStateSelectedDone) {
                    check_box_select_all_store.isChecked = !StorePrefData.isAreaChanged
                } else {
                    check_box_select_all_store.isChecked = false
                }


                select_text.text = getString(R.string.select_all_state)
            }
            getString(R.string.supervisor_text) -> {
                if (StorePrefData.isSupervisorSelected && StorePrefData.isSupervisorSelectedDone) {
                    check_box_select_all_store.isChecked =
                            !(StorePrefData.isAreaChanged || StorePrefData.isStateChanged)
                } else {
                    check_box_select_all_store.isChecked = false
                }

                select_text.text = getString(R.string.select_all_supervisor)
            }
        }
    }

    private fun callFilterActivity() {
        val intent = Intent(this@StoreFilterActivity, FilterActivity::class.java)
        intent.putExtra("action", userFilterAction)
        intent.putExtra("isAnySelectionChanged", isAnySelectionChangedStoreFilter)
        startActivity(intent)
        finish()
    }

    private fun setRecycler() {
        search_store.setPadding(-17, 0, 1, 0)
        select_all_parent.visibility = View.VISIBLE
        checkbox_below_view.visibility = View.VISIBLE
        store_list_rv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        lifecycleScope.launchWhenResumed {
            when (userFilterAction) {
                getString(R.string.area_text) -> {
                    areaListAdapterDBStoreFilter = AreaListAdapterDB(
                            dbHelperStoreFilter.getUsersArea()
                    )
                    store_list_rv.adapter = areaListAdapterDBStoreFilter

                    areaListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                 AreaListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isAreaChecked: Boolean, id: Int, size: Int) {
                            updateList(isAreaChecked, id, size)
                        }
                    })
                    val dbAreaData = dbHelperStoreFilter.getUsersArea()
                    val nonSelectedAreaCountList: ArrayList<Int> = ArrayList()
                    for (areaData in dbAreaData){
                        if(!areaData.isSelect){
                            nonSelectedAreaCountList.add(areaData.id)
                        }
                    }
                    if(nonSelectedAreaCountList.isEmpty()){
                        check_box_select_all_store.isChecked = true
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }else{
                        check_box_select_all_store.isChecked = false
                    }

                }
                getString(R.string.store_text) -> {
                    storeListAdapterDBStoreFilter = StoreListAdapterDB(
                            dbHelperStoreFilter.getUsers()
                    )
                    store_list_rv.adapter = storeListAdapterDBStoreFilter
                    storeListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                  StoreListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isStoreDBChecked: Boolean, id: Int, size: Int) {
                            updateList(isStoreDBChecked, id, size)
                        }

                    })

                    val dbStoreData = dbHelperStoreFilter.getUsers()
                    val nonSelectedStoresCountList: ArrayList<Int> = ArrayList()
                    for (storeData in dbStoreData){
                        if(!storeData.isSelect){
                            nonSelectedStoresCountList.add(storeData.id)
                        }
                    }
                    if(nonSelectedStoresCountList.isEmpty()){
                        check_box_select_all_store.isChecked = true
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }else{
                        check_box_select_all_store.isChecked = false
                    }

                }
                getString(R.string.state_text) -> {
                    stateListAdapterDBStoreFilter = StateListAdapterDB(
                            dbHelperStoreFilter.getUsersState()
                    )
                    store_list_rv.adapter = stateListAdapterDBStoreFilter

                    stateListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                  StateListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isChecked: Boolean, id: Int, size: Int) {
                            updateList(isChecked, id, size)
                        }

                    })
                    val dbStateData = dbHelperStoreFilter.getUsersState()
                    val nonSelectedSateCountList: ArrayList<Int> = ArrayList()
                    for (stateData in dbStateData){
                        if(!stateData.isSelect){
                            nonSelectedSateCountList.add(stateData.id)
                        }
                    }
                    if(nonSelectedSateCountList.isEmpty()){
                        check_box_select_all_store.isChecked = true
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }else{
                        check_box_select_all_store.isChecked = false
                    }
                }
                getString(R.string.supervisor_text) -> {
                    supervisorListAdapterDBStoreFilter = SupervisorListAdapterDB(
                            dbHelperStoreFilter.getUsersSupervisor()
                    )
                    store_list_rv.adapter = supervisorListAdapterDBStoreFilter
                    supervisorListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                       SupervisorListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isChecked: Boolean, id: Int, size: Int) {
                            updateList(isChecked, id, size)
                        }

                    })

                    val dbSupervisorData = dbHelperStoreFilter.getUsersSupervisor()
                    val nonSelectedSupervisorCountList: ArrayList<Int> = ArrayList()
                    for (supervisorData in dbSupervisorData){
                        if(!supervisorData.isSelect){
                            nonSelectedSupervisorCountList.add(supervisorData.id)
                        }
                    }
                    if(nonSelectedSupervisorCountList.isEmpty()){
                        check_box_select_all_store.isChecked = true
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }else{
                        check_box_select_all_store.isChecked = false
                    }
                }
            }

        }
    }

    private fun callStoreFilterApi() {
        lifecycleScope.launchWhenResumed {
            if (userFilterAction == getString(R.string.area_text)) {
                if (dbHelperStoreFilter.getUsersArea().isEmpty()) {
                    callFilterApi()
                } else {
                    setDbData()
                }
            } else if (userFilterAction == getString(R.string.store_text)) {
                if (StorePrefData.isAreaChanged || StorePrefData.isStateChanged || StorePrefData.isSupervisorChanged) {
                    dbHelperStoreFilter.deleteAllStore()
                    StorePrefData.isAreaChanged = false
                    StorePrefData.isStateChanged = false
                    StorePrefData.isSupervisorChanged = false
                }
                if (dbHelperStoreFilter.getUsers().isEmpty()) {
                    callFilterApi()
                } else {
                    setDbData()
                }
            } else if (userFilterAction == getString(R.string.state_text)) {
                if (StorePrefData.isAreaChanged) {
                    dbHelperStoreFilter.deleteAllState()
                    StorePrefData.isAreaChanged = false
                    StorePrefData.isStateSelected = false
                    StorePrefData.isStateSelectedDone = false
                }
                if (dbHelperStoreFilter.getUsersState().isEmpty()) {
                    callFilterApi()
                } else {
                    setDbData()
                }
            } else if (userFilterAction == getString(R.string.supervisor_text)) {
                if (StorePrefData.isAreaChanged || StorePrefData.isStateChanged) {
                    dbHelperStoreFilter.deleteAllSupervisor()
                    StorePrefData.isAreaChanged = false
                    StorePrefData.isStateChanged = false
                }
                if (dbHelperStoreFilter.getUsersSupervisor().isEmpty()) {
                    callFilterApi()
                } else {
                    setDbData()
                }
            }

        }
    }


    private fun setDbData() {
        store_list_rv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        lifecycleScope.launchWhenResumed {
            when (userFilterAction) {
                getString(R.string.area_text) -> {
                    areaListAdapterDBStoreFilter = AreaListAdapterDB(
                            dbHelperStoreFilter.getUsersArea()
                    )
                    store_list_rv.adapter = areaListAdapterDBStoreFilter

                    areaListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                 AreaListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isAreaChecked: Boolean, id: Int, size: Int) {
                            updateList(isAreaChecked, id, size)
                        }
                    })
                    val dbAreaData = dbHelperStoreFilter.getUsersArea()
                    for (areaItem in dbAreaData){
                        if(areaItem.isSelect){
                            reset_text.setTextColor(getColor(R.color.header_color))
                            reset_text.isFocusableInTouchMode = true

                        }
                    }

                }
                getString(R.string.store_text) -> {

                    storeListAdapterDBStoreFilter = StoreListAdapterDB(
                            dbHelperStoreFilter.getUsers()
                    )
                    store_list_rv.adapter = storeListAdapterDBStoreFilter

                    storeListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                  StoreListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isStoreDBChecked: Boolean, id: Int, size: Int) {
                            updateList(isStoreDBChecked, id, size)
                        }

                    })

                    val dbStoreData = dbHelperStoreFilter.getUsers()
                    for (storeItem in dbStoreData){
                        if(storeItem.isSelect){
                            reset_text.setTextColor(getColor(R.color.header_color))
                            reset_text.isFocusableInTouchMode = true

                        }
                    }

                }
                getString(R.string.state_text) -> {
                    stateListAdapterDBStoreFilter = StateListAdapterDB(
                            dbHelperStoreFilter.getUsersState(),
                    )
                    store_list_rv.adapter = stateListAdapterDBStoreFilter

                    stateListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                  StateListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isChecked: Boolean, id: Int, size: Int) {
                            updateList(isChecked, id, size)
                        }

                    })
                    val dbStateData = dbHelperStoreFilter.getUsersState()
                    for (stateItem in dbStateData){
                        if(stateItem.isSelect){
                            reset_text.setTextColor(getColor(R.color.header_color))
                            reset_text.isFocusableInTouchMode = true

                        }
                    }

                }
                getString(R.string.supervisor_text) -> {
                    supervisorListAdapterDBStoreFilter = SupervisorListAdapterDB(
                            dbHelperStoreFilter.getUsersSupervisor()
                    )
                    store_list_rv.adapter = supervisorListAdapterDBStoreFilter

                    supervisorListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                                       SupervisorListAdapterDB.OnItemClickListener {
                        override fun onItemClick(isChecked: Boolean, id: Int, size: Int) {
                            updateList(isChecked, id, size)
                        }

                    })

                    val dbSupervisorData = dbHelperStoreFilter.getUsersSupervisor()
                    for (supervisorItem in dbSupervisorData){
                        if(supervisorItem.isSelect){
                            reset_text.setTextColor(getColor(R.color.header_color))
                            reset_text.isFocusableInTouchMode = true
                        }
                    }

                }
            }


        }
    }


    private fun updateList(isChecked: Boolean, id: Int, size: Int) {
        lifecycleScope.launchWhenResumed {
            when (userFilterAction) {
                getString(R.string.area_text) -> {
                    if(areaListAdapterDBStoreFilter?.selectedAreaListData?.size == 0){
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                    }else{
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }
                    StorePrefData.isAreaChanged = true
                    StorePrefData.isAreaSelected = false
                    dbHelperStoreFilter.updateStoreListCheckedArea(isSelect = isChecked, id = id)
                    isAnySelectionChangedStoreFilter = getString(R.string.area_text)
                    check_box_select_all_store.isChecked =
                            dbHelperStoreFilter.getAllSelectedAreaList(true).size == size

                }
                getString(R.string.store_text) -> {
                    if(storeListAdapterDBStoreFilter?.selectedStoreListData?.size == 0){
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                    }else{
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }
                    StorePrefData.isStoreChanged = true
                    StorePrefData.isStoreSelected = false
                    dbHelperStoreFilter.updateStoreListChecked(isSelect = isChecked, id = id)
                    check_box_select_all_store.isChecked =
                            dbHelperStoreFilter.getAllSelectedStoreList(true).size == size

                }
                getString(R.string.state_text) -> {
                    if(stateListAdapterDBStoreFilter?.selectedStateListData?.size == 0){
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                    }else{
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }
                    StorePrefData.isStateChanged = true
                    StorePrefData.isStateSelected = false
                    dbHelperStoreFilter.updateStoreListCheckedState(isSelect = isChecked, id = id)
                    isAnySelectionChangedStoreFilter = getString(R.string.state_text)
                    check_box_select_all_store.isChecked =
                            dbHelperStoreFilter.getAllSelectedStoreListState(true).size == size

                }
                getString(R.string.supervisor_text) -> {
                    if(supervisorListAdapterDBStoreFilter?.selectedSupervisorListData?.size == 0){
                        reset_text.setTextColor(getColor(R.color.reset_text_color))
                        reset_text.isFocusableInTouchMode = false
                    }else{
                        reset_text.setTextColor(getColor(R.color.header_color))
                        reset_text.isFocusableInTouchMode = true
                    }
                    StorePrefData.isSupervisorChanged = true
                    StorePrefData.isSupervisorSelected = false
                    dbHelperStoreFilter.updateStoreListCheckedSupervisor(isSelect = isChecked, id = id)
                    isAnySelectionChangedStoreFilter = getString(R.string.supervisor_text)
                    check_box_select_all_store.isChecked = dbHelperStoreFilter.getCountSelectedSuperVisorList(1) == size

                }
            }
        }

    }


    override fun onBackPressed() {
        callFilterActivity()
    }

    private fun callFilterApi() {
        val progressDialog = CustomProgressDialog(this@StoreFilterActivity)
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCode = dbHelperStoreFilter.getAllSelectedAreaList(true)
            val stateCode = dbHelperStoreFilter.getAllSelectedStoreListState(true)
            val supervisorNumber = dbHelperStoreFilter.getAllSelectedStoreListSupervisor(true)
            if (userFilterAction == getString(R.string.area_text)) {
                val response = try {
                    apolloClientProfile(this@StoreFilterActivity).query(
                            AreaFilterQuery()).await()
                } catch (e: ApolloHttpException) {
                    Logger.error(e.message.toString(),"Restaurant Group Filter")
                    refreshTokenForStoreFilter()
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                } catch (e: ApolloNetworkException) {
                    showStoreErrorDialog(getString(R.string.network_error_title),
                                         getString(R.string.network_error_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                } catch (e: ApolloException) {
                    Logger.error(e.message.toString(),"Restaurant Group Filter")
                    showStoreErrorDialog(getString(R.string.exception_error_text_title),
                                         getString(R.string.exception_error_text_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                }
                progressDialog.dismissProgressDialog()
                if (response.data?.user != null) {
                    setAreaData(response.data?.user!!)
                }

            } else if (userFilterAction == getString(R.string.state_text)) {
                val response = try {
                    apolloClientProfile(this@StoreFilterActivity).query(
                            StateFilterQuery(
                                    areaCode.toInput())
                    ).await()
                } catch (e: ApolloHttpException) {
                    Logger.error(e.message.toString(),"State Filter")
                    progressDialog.dismissProgressDialog()
                    refreshTokenForStoreFilter()
                    return@launchWhenResumed
                }catch (e: ApolloNetworkException) {
                    showStoreErrorDialog(getString(R.string.network_error_title),
                                         getString(R.string.network_error_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                } catch (e: ApolloException) {
                    Logger.error(e.message.toString(),"State Filter")
                    showStoreErrorDialog(getString(R.string.exception_error_text_title),
                                         getString(R.string.exception_error_text_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                }
                progressDialog.dismissProgressDialog()
                if (response.data?.user != null) {
                    setStateData(response.data?.user!!)
                }
            } else if (userFilterAction == getString(R.string.supervisor_text)) {
                val response = try {
                    apolloClientProfile(this@StoreFilterActivity).query(
                            SupervisorFilterQuery(
                                    areaCode.toInput(), stateCode.toInput())
                    ).await()
                } catch (e: ApolloHttpException) {
                    Logger.error(e.message.toString(),"Supervisor Filter")
                    progressDialog.dismissProgressDialog()
                    refreshTokenForStoreFilter()
                    return@launchWhenResumed
                }catch (e: ApolloNetworkException) {
                    showStoreErrorDialog(getString(R.string.network_error_title),
                                         getString(R.string.network_error_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                } catch (e: ApolloException) {
                    Logger.error(e.message.toString(),"Supervisor Filter")
                    showStoreErrorDialog(getString(R.string.exception_error_text_title),
                                         getString(R.string.exception_error_text_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                }
                progressDialog.dismissProgressDialog()
                if (response.data?.user != null) {
                    setSupervisorData(response.data?.user!!)
                }
            } else if (userFilterAction == getString(R.string.store_text)) {
                val response = try {
                    apolloClientProfile(this@StoreFilterActivity).query(
                            StoreFilterQuery(
                                    areaCode.toInput(), stateCode.toInput(), supervisorNumber.toInput())
                    ).await()
                } catch (e: ApolloHttpException) {
                    Logger.error(e.message.toString(),"Restaurants Filter")
                    progressDialog.dismissProgressDialog()
                    refreshTokenForStoreFilter()
                    return@launchWhenResumed
                }catch (e: ApolloNetworkException) {
                    showStoreErrorDialog(getString(R.string.network_error_title),
                                         getString(R.string.network_error_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                } catch (e: ApolloException) {
                    Logger.error(e.message.toString(),"Restaurants Filter")
                    showStoreErrorDialog(getString(R.string.exception_error_text_title),
                                         getString(R.string.exception_error_text_description))
                    progressDialog.dismissProgressDialog()
                    return@launchWhenResumed
                }
                progressDialog.dismissProgressDialog()
                if (response.data?.user != null) {
                    setStoreData(response.data?.user!!)
                }
            }


        }
    }


    private fun setAreaData(corporateProfile: AreaFilterQuery.User) {
        store_list_rv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        lifecycleScope.launchWhenResumed {
            for (i in corporateProfile.areas.indices) {
                dbHelperStoreFilter.insertAllArea(
                        AreaListEntity(
                                corporateProfile.areas[i].toString(),
                                corporateProfile.areas[i].toString(),
                                false
                        )
                )
            }
            areaListAdapterDBStoreFilter = AreaListAdapterDB(
                    dbHelperStoreFilter.getUsersArea()
            )
            store_list_rv.adapter = areaListAdapterDBStoreFilter

            areaListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                         AreaListAdapterDB.OnItemClickListener {
                override fun onItemClick(isAreaChecked: Boolean, id: Int, size: Int) {
                    updateList(isAreaChecked, id, size)
                }

            })
        }


    }



    private fun setStoreData(corporateProfile: StoreFilterQuery.User) {
        store_list_rv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        lifecycleScope.launchWhenResumed {

            for (i in corporateProfile.stores.indices) {
                dbHelperStoreFilter.insertAll(
                        StoreListEntity(
                                corporateProfile.stores[i].toString(),
                                corporateProfile.stores[i].toString(),
                                false
                        )
                )
            }

            storeListAdapterDBStoreFilter = StoreListAdapterDB(
                    dbHelperStoreFilter.getUsers()
            )
            store_list_rv.adapter = storeListAdapterDBStoreFilter

            storeListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                          StoreListAdapterDB.OnItemClickListener {
                override fun onItemClick(isStoreDBChecked: Boolean, id: Int, size: Int) {
                    updateList(isStoreDBChecked, id, size)
                }

            })

        }


    }


    private fun setStateData(corporateProfile: StateFilterQuery.User) {
        store_list_rv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        lifecycleScope.launchWhenResumed {
            for (i in corporateProfile.states.indices) {
                dbHelperStoreFilter.insertAllState(
                        StateListEntity(
                                corporateProfile.states[i].toString(),
                                corporateProfile.states[i].toString(),
                                false
                        )

                )
            }

            stateListAdapterDBStoreFilter = StateListAdapterDB(
                    dbHelperStoreFilter.getUsersState()
            )
            store_list_rv.adapter = stateListAdapterDBStoreFilter

            stateListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                          StateListAdapterDB.OnItemClickListener {
                override fun onItemClick(isChecked: Boolean, id: Int, size: Int) {
                    updateList(isChecked, id, size)
                }

            })
        }


    }


    private fun setSupervisorData(corporateProfile: SupervisorFilterQuery.User) {
        store_list_rv.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        lifecycleScope.launchWhenResumed {
            for (i in corporateProfile.supervisors.indices) {
                dbHelperStoreFilter.insertAllSupervisor(
                        SuperVisorListEntity(
                                corporateProfile.supervisors[i]?.supervisorName.toString(),
                                corporateProfile.supervisors[i]?.supervisorUserID.toString(),
                                false
                        )

                )

            }
            supervisorListAdapterDBStoreFilter = SupervisorListAdapterDB(
                    dbHelperStoreFilter.getUsersSupervisor()
            )
            store_list_rv.adapter = supervisorListAdapterDBStoreFilter

            supervisorListAdapterDBStoreFilter?.setOnItemClickListener(object :
                                                                               SupervisorListAdapterDB.OnItemClickListener {
                override fun onItemClick(isChecked: Boolean, id: Int, size: Int) {
                    updateList(isChecked, id, size)
                }

            })

        }
    }

    fun showStoreErrorDialog(title: String, description: String){
        if(storeFilterErrorDialog == null){
            storeFilterErrorDialog = DialogUtil.getErrorDialogAccessDialog(
                    this@StoreFilterActivity,
                    title,
                    description,
                    getString(R.string.retry_text),
                    {
                        storeFilterErrorDialog?.dismiss()
                        storeFilterErrorDialog = null
                        callFilterApi()
                    },
                    getString(R.string.cancel_text),
                    {
                        storeFilterErrorDialog?.dismiss()
                        storeFilterErrorDialog = null
                    }
            )
            storeFilterErrorDialog?.show()
        }

    }

    private fun refreshTokenForStoreFilter() {
        refreshTokenStoreFilter.getRefreshToken()
        refreshTokenStoreFilter.refreshTokenResponseLiveData.observe(this, {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callFilterApi()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(MainActivity())
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        if(storeFilterErrorDialog == null){
                            showStoreErrorDialog(getString(R.string.network_error_title),
                                                 getString(R.string.network_error_description))
                        }

                    }
                }
            }
        })
    }
}