package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.arria.ping.R
import com.arria.ping.apollo.apolloClientProfile
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.model.profile.UserProfile
import com.arria.ping.profile.UserProfileQuery
import com.arria.ping.util.*
import com.arria.ping.util.globalexceptionhandler.GlobalExceptionHandler
import com.arria.ping.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

@AndroidEntryPoint
abstract class BaseActivity: AppCompatActivity() {

    private var progressBar : ProgressBarDialog?= null
    private val viewModel by viewModels<LoginViewModel>()
    private var userProfileErrorDialo: Dialog? = null
    private var retryUserProfileDialo: Dialog? = null
    override fun onCreate(
            savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        GlobalExceptionHandler(this)
        progressBar = ProgressBarDialog.instance
    }

    fun showProgressDialog() {
        progressBar?.show(supportFragmentManager, ProgressBarDialog.TAG)
    }

    fun dismissProgressDialog() {
        progressBar?.dismiss()
    }
    fun hideKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserProfile() {
        showProgressDialog()
        lifecycleScope.launchWhenResumed {
            try {

                val response = apolloClientProfile(this@BaseActivity).query(UserProfileQuery())
                        .await()
                if (response.data?.user != null) {
                    Logger.info(
                            UserProfileQuery.OPERATION_NAME.name(),
                            "UserProfile",
                            mapQueryFilters(
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    UserProfileQuery.QUERY_DOCUMENT
                            )
                    )
                    val storeId = if (!response.data?.user?.stores.isNullOrEmpty()) {
                        response.data?.user?.stores!![0].toString()
                    } else {
                        ""
                    }

                    viewModel.setUserDataInPreferences(
                            UserProfile(
                                    firstName = response.data?.user?.firstName.toString(),
                                    lastName = response.data?.user?.lastName.toString(),
                                    role = response.data?.user?.role.toString(),
                                    email = response.data?.user?.email.toString(),
                                    storeId = storeId
                            )
                    )
                    navigateToDashboard()
                }else{
                    dismissProgressDialog()
                    bottom_navigation_view.menu.getItem(1).isChecked = true
                    callUserProfileDialog(getString(R.string.exception_error_text_title),
                                               getString(R.string.exception_error_text_description))
                    Logger.error("Failed to get userprofile Data", "Userprofile")
                }
            } catch (apolloHttpException: ApolloHttpException) {
                dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Userprofile")
                        }
                callUserProfileDialog(getString(R.string.exception_error_text_title),
                                           getString(R.string.exception_error_text_description))
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                dismissProgressDialog()
                showUserProfileErrorDialog(getString(R.string.network_error_title),
                                           getString(R.string.network_error_description))
            } catch (e: ApolloException) {
                dismissProgressDialog()
                showUserProfileErrorDialog(getString(R.string.something_wrong_text),
                                           getString(R.string.logout_failure_description_text))
                Logger.error(e.message.toString(), "Userprofile")
            } catch (e: Exception) {
                dismissProgressDialog()
                showUserProfileErrorDialog(getString(R.string.something_wrong_text),
                                           getString(R.string.logout_failure_description_text))
                e.printStackTrace()
            }
        }
    }

    fun showUserProfileErrorDialog(errorTitle: String, errorDescription: String){
        userProfileErrorDialo = DialogUtil.getErrorDialogAccessDialog(
                this,
                errorTitle,
                errorDescription,
                getString(R.string.ok_text),
                {
                        userProfileErrorDialo?.dismiss()
                        userProfileErrorDialo = null

                },
                null,
                null
        )
        userProfileErrorDialo?.show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun callUserProfileDialog(errorTitle: String, errorDescription: String){
        retryUserProfileDialo = DialogUtil.getErrorDialogAccessDialog(
                this,
                errorTitle,
                errorDescription,
                getString(R.string.retry_text),
                {
                    getUserProfile()
                    retryUserProfileDialo?.dismiss()
                    retryUserProfileDialo = null

                },
                getString(R.string.cancel_text),
                {
                    retryUserProfileDialo?.dismiss()
                    retryUserProfileDialo = null

                }
        )
        retryUserProfileDialo?.show()

    }

    fun navigateToDashboard() {
        try {
            dismissProgressDialog()
            StorePrefData.isFromBioMetricLoginORPassword = true
            StorePrefData.whichBottomNavigationClicked = getString(R.string.title_kpis)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}