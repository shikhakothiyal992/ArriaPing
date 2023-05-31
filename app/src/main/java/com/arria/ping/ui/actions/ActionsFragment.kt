package com.arria.ping.ui.actions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arria.ping.R
import com.arria.ping.apiclient.ApiClientAuth
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apollo.apolloClient
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.model.LoginFail
import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.ui.actions.ceo.*
import com.arria.ping.ui.actions.do_.DOActionAdapter
import com.arria.ping.ui.actions.do_.DOPastActionStoreListActivity
import com.arria.ping.ui.actions.supervisor.SupervisorActionAdapter
import com.arria.ping.ui.actions.supervisor.SupervisorPastActionStoreListActivity
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.kpi.*
import kotlinx.android.synthetic.main.activity_actions.*
import kotlinx.android.synthetic.main.common_header_for_action.view.*
import kotlinx.android.synthetic.main.fragment_action.*
import kotlinx.coroutines.launch
import java.util.*


class ActionsFragment : Fragment() {
    val gson = Gson()
    private lateinit var actionDataGM: GMActionQuery.GeneralManager
    var myCallback: MyCallback? = null
    lateinit var dbHelper: DatabaseHelperImpl
    var areaCode = listOf<String>()
    var stateCode = listOf<String>()
    var storeNumber = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_action, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialise()

    }

    fun setOnItemClickLitener(mOnItemClickListener: MyCallback?) {
        this.myCallback = mOnItemClickListener
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialise() {
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))
        Logger.info("Action Screen","Actions")
        lifecycleScope.launch {
            areaCode = dbHelper.getAllSelectedAreaList(true)
            stateCode = dbHelper.getAllSelectedStoreListState(true)
            storeNumber = dbHelper.getAllSelectedStoreList(true)
        }
        action_rv.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        past_action.setOnClickListener {
            when (StorePrefData.role) {
                getString(R.string.ceo_text) -> {
                    openPastActionCEO()
                }
                getString(R.string.do_text) -> {
                    openPastActionDO()
                }
                getString(R.string.supervisor_text) -> {
                    openPastActionSupervisor()
                }
                else -> {
                    openPastActionGM()
                }
            }

        }
        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                callCEOAction()
            }
            getString(R.string.do_text) -> {
                callDOAction()
            }
            getString(R.string.supervisor_text) -> {
                callSupervisorAction()
            }
            else -> {
                callGMAction()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callGMAction() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            val gmActionsStoreListValue = mutableListOf<String>()
            gmActionsStoreListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                GMActionQuery.OPERATION_NAME.name(), "Actions", mapQueryFilters(
                    QueryData(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        gmActionsStoreListValue,
                        "",
                        "",
                        "",
                        GMActionQuery.QUERY_DOCUMENT
                    )
                )
            )
            val response = try {
                apolloClient(requireContext()).query(GMActionQuery(StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Actions")
                refreshTokenActionFragment(getString(R.string.gm_text))
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialog.dismissProgressDialog()
                actionDataGM = response.data?.generalManager!!



                setDataGM(response.data?.generalManager!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callCEOAction() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(requireContext()).query(CEOActionQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Actions")
                refreshTokenActionFragment(getString(R.string.ceo_text))
                return@launchWhenResumed
            }
            if (response.data?.ceo != null) {
                progressDialog.dismissProgressDialog()
                actionDataCEO = response.data?.ceo!!

                val gmActionsStoreListValue = mutableListOf<String>()
                gmActionsStoreListValue.add(StorePrefData.StoreIdFromLogin)
                Logger.info(
                    CEOActionQuery.OPERATION_NAME.name(), "Actions", mapQueryFilters(
                        QueryData(
                            areaCode,
                            stateCode,
                            Collections.emptyList(),
                            storeNumber,
                            "",
                            "",
                            "",
                            CEOActionQuery.QUERY_DOCUMENT
                        )
                    )
                )

                setDataCEO(response.data?.ceo!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callDOAction() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(requireContext()).query(DOActionQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Actions")
                refreshTokenActionFragment(getString(R.string.do_text))
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialog.dismissProgressDialog()
                actionDataDO = response.data?.do_!!


                Logger.info(
                    DOActionQuery.OPERATION_NAME.name(), "Actions", mapQueryFilters(
                        QueryData(
                            areaCode,
                            Collections.emptyList(),
                            stateCode,
                            storeNumber,
                            "",
                            "",
                            "",
                            DOActionQuery.QUERY_DOCUMENT
                        )
                    )
                )

                setDataDO(response.data?.do_!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callSupervisorAction() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(requireContext()).query(SupervisorActionQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"Actions")
                refreshTokenActionFragment(getString(R.string.supervisor_text))
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {
                progressDialog.dismissProgressDialog()
                actionDataSupervisor = response.data?.supervisor!!

                Logger.info(
                    SupervisorActionQuery.OPERATION_NAME.name(), "Actions", mapQueryFilters(
                        QueryData(
                            areaCode,
                            stateCode,
                            Collections.emptyList(),
                            storeNumber,
                            "",
                            "",
                            "",
                            SupervisorActionQuery.QUERY_DOCUMENT
                        )
                    )
                )

                setDataSupervisor(response.data?.supervisor!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataGM(generalManager: GMActionQuery.GeneralManager) {
        if (generalManager.actions?.store?.currentActions!!.isNotEmpty()) {
            no_new_action_linear.visibility = View.GONE
            action_rv.visibility = View.VISIBLE

            (activity as   BaseActionsActivity?)!!.updateStoreNumber(generalManager.actions.store.storeNumber!!,
                true)

            val gmActionAdapter =
                GMActionAdapter(requireContext(), generalManager.actions.store.currentActions)
            action_rv.adapter = gmActionAdapter
        } else {
            no_new_action_linear.visibility = View.VISIBLE
            action_rv.visibility = View.GONE

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataCEO(ceo: CEOActionQuery.Ceo) {
        if (ceo.actions?.stores?.isNotEmpty() == true) {
            no_new_action_linear.visibility = View.GONE
            action_rv.visibility = View.VISIBLE

            (activity as   BaseActionsActivity?)!!.updateStoreNumber(ceo.actions.stores[0]!!.storeNumber.toString(),
                false)

            val ceoActionAdapter =
                CEOActionAdapter(requireContext(), ceo.actions.stores)
            action_rv.adapter = ceoActionAdapter
        } else {
            no_new_action_linear.visibility = View.VISIBLE
            action_rv.visibility = View.GONE

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataDO(doAction: DOActionQuery.Do_) {
        if (doAction.actions?.stores?.isNotEmpty() == true) {
            no_new_action_linear.visibility = View.GONE
            action_rv.visibility = View.VISIBLE
            (activity as  BaseActionsActivity?)!!.updateStoreNumber(doAction.actions.stores[0]!!.storeNumber.toString(),
                false)

            val gmActionAdapter =
                DOActionAdapter(requireContext(), doAction.actions.stores)
            action_rv.adapter = gmActionAdapter
        } else {
            no_new_action_linear.visibility = View.VISIBLE
            action_rv.visibility = View.GONE

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataSupervisor(supervisor: SupervisorActionQuery.Supervisor) {
        if (supervisor.actions?.stores?.isNotEmpty() == true) {
            no_new_action_linear.visibility = View.GONE
            action_rv.visibility = View.VISIBLE

            (activity as   BaseActionsActivity?)!!.updateStoreNumber(supervisor.actions.stores[0]!!.storeNumber.toString(),
                false)

            val gmActionAdapter =
                SupervisorActionAdapter(requireContext(), supervisor.actions.stores)
            action_rv.adapter = gmActionAdapter
        } else {
            no_new_action_linear.visibility = View.VISIBLE
            action_rv.visibility = View.GONE

        }

    }

    private fun openPastActionGM() {
        val intent = Intent(requireContext(), GMPastActionListActivity::class.java)
        intent.putExtra("past_action_data", gson.toJson(actionDataGM))
        startActivity(intent)
    }

    private fun openPastActionCEO() {
        val intent = Intent(requireContext(), CEOPastActionStoreListActivity::class.java)
        startActivity(intent)
    }

    private fun openPastActionDO() {
        val intent = Intent(requireContext(), DOPastActionStoreListActivity::class.java)
        startActivity(intent)
    }

    private fun openPastActionSupervisor() {
        val intent = Intent(requireContext(), SupervisorPastActionStoreListActivity::class.java)
        startActivity(intent)
    }

    companion object {
        lateinit var actionDataCEO: CEOActionQuery.Ceo
        lateinit var actionDataDO: DOActionQuery.Do_
        lateinit var actionDataSupervisor: SupervisorActionQuery.Supervisor

    }

    interface MyCallback {
        fun updateStoreNumber(storeNumber: String?)
    }

    private fun refreshTokenActionFragment(action: String) {
        val progressDialogRefreshTokenFragment = CustomProgressDialog(requireActivity())
        progressDialogRefreshTokenFragment.showProgressDialog()
        val apiServiceRefreshTokenFragment: ApiInterface = ApiClientAuth().getClient()!!.create(ApiInterface::class.java)
        val callRefreshTokenFragment = apiServiceRefreshTokenFragment.refreshToken(SendRefreshRequest(
            StorePrefData.refreshToken))
        callRefreshTokenFragment.enqueue(object : retrofit2.Callback<LoginSuccess> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: retrofit2.Call<LoginSuccess>,
                responseRefreshTokenFragment: retrofit2.Response<LoginSuccess>,
            ) {
                progressDialogRefreshTokenFragment.dismissProgressDialog()
                if (responseRefreshTokenFragment.isSuccessful) {
                    println("RefreshToken----" + responseRefreshTokenFragment.body()!!)
                    StorePrefData.token = responseRefreshTokenFragment.body()!!.authenticationResult.accessToken
                    when (action) {
                        getString(R.string.gm_text) -> {
                            callGMAction()
                        }
                        getString(R.string.ceo_text) -> {
                            callCEOAction()
                        }
                        getString(R.string.do_text) -> {
                            callDOAction()
                        }
                        getString(R.string.supervisor_text) -> {
                            callSupervisorAction()

                        }
                    }

                } else {
                    val gsonRefreshTokenFragment = Gson()
                    val typeRefreshTokenFragment = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponseRefreshTokenFragment = gsonRefreshTokenFragment.fromJson<LoginFail>(
                        responseRefreshTokenFragment.errorBody()!!.charStream(), typeRefreshTokenFragment
                    )
                    Logger.error(errorResponseRefreshTokenFragment.message,"Actions")
                    Validation().showMessageToast(requireActivity(), errorResponseRefreshTokenFragment.message)
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {
                Logger.error(t.message.toString(),"Actions Refresh Token")
            }
        })
    }
}