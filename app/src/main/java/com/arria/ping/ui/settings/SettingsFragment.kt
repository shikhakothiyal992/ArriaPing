package com.arria.ping.ui.settings

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.arria.ping.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_more.*

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var progressDialog: CustomProgressDialog
    private val refreshTokenViewModel by viewModels<RefreshTokenViewModel>()
    private val settingViewModel by viewModels<SettingsViewModel>()
    private lateinit var mainActivity: MainActivity
    private var dialog: Dialog? = null
    private var settingsErrorDialog: Dialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        Logger.info("Setting Screen", "Settings")
        getAndSetAppVersion()
        progressDialog = CustomProgressDialog(requireActivity())

        text_username.text = getString(R.string.user_name_text, StorePrefData.firstName, StorePrefData.lastName)
        text_email.text = StorePrefData.email

        if (StorePrefData.isDeviceHasBiometricFeatures) {
            touch_id_layout.visibility = View.VISIBLE
        } else {
            touch_id_layout.visibility = View.GONE
        }
        touch_switch.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked) {
                touch_switch.isChecked = true
                StorePrefData.isTouchIDEnabled = true
                StorePrefData.isUserAllowedBiometric = true
                StorePrefData.isUserBioMetricLoggedIn = true
                Logger.info("Touch ID Enabled", "Settings")
            } else {
                touch_switch.isChecked = false
                StorePrefData.isTouchIDEnabled = false
                StorePrefData.isUserAllowedBiometric = false
                StorePrefData.isUserBioMetricLoggedIn = false
                Logger.info("Touch ID Disabled", "Settings")
            }

        }

        touch_switch.isChecked = StorePrefData.isTouchIDEnabled

        text_logout.setOnClickListener {
            Logger.info("Logout button clicked", "Settings")
            showLogoutDialog()
        }

    }

    fun getAndSetAppVersion() {
        val manager = requireActivity().packageManager
        val info = manager.getPackageInfo(requireActivity().packageName, 0)

        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            info.versionCode
        }
        app_version.text = getString(R.string.app_version_text, info.versionName, versionCode)
    }

    fun doLogout() {
        settingViewModel.doLogout()
        settingViewModel.logoutMutableLiveData.observe(requireActivity(), {
            when (it.status) {
                com.arria.ping.model.responsehandlers.Status.LOADING -> progressDialog.showProgressDialog()

                com.arria.ping.model.responsehandlers.Status.SUCCESS -> {
                    progressDialog.dismissProgressDialog()
                    CommonUtil.navigateToLogin(mainActivity)
                }
                com.arria.ping.model.responsehandlers.Status.ERROR -> {
                    progressDialog.dismissProgressDialog()
                    when (it.code) {
                        400 -> {
                            callRefreshTokenAPI()
                        }
                        IpConstants.OFFLINE_ERROR_CODE -> {
                            if(settingsErrorDialog == null){
                                showSettingsErrorDialog(getString(R.string.network_error_title), getString(R.string.network_error_description))
                            }

                        }
                        else -> {
                            if(settingsErrorDialog == null){
                                showSettingsErrorDialog(getString(R.string.logout_failure_title_text), getString(R.string.logout_failure_description_text))
                            }

                        }
                    }
                }
            }
        })
    }

    fun showLogoutDialog() {
        dialog = DialogUtil.getLogoutDialog(
                requireActivity(),
                getString(R.string.logout),
                {
                    doLogout()
                    dialog?.dismiss()
                    dialog = null

                },
                getString(R.string.cancel),
                {
                    Logger.info("Log out Cancelled ", "Settings")
                    dialog?.dismiss()
                    dialog = null

                },
        )
        dialog?.show()
    }

    fun callRefreshTokenAPI() {
        refreshTokenViewModel.getRefreshToken()
        refreshTokenViewModel.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                        progressDialog.showProgressDialog()
                    }
                    Status.SUCCESS -> {
                        progressDialog.dismissProgressDialog()
                        doLogout()
                    }
                    Status.UNSUCCESSFUL -> {
                        progressDialog.dismissProgressDialog()
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                        progressDialog.dismissProgressDialog()
                    }
                    Status.OFFLINE -> {
                        progressDialog.dismissProgressDialog()
                        if(settingsErrorDialog == null){
                            showSettingsErrorDialog(getString(R.string.network_error_title), getString(R.string.network_error_description))
                        }

                    }
                }
            }
        })
    }

    fun showSettingsErrorDialog(errorTitle: String, errorDescription: String) {
        settingsErrorDialog = DialogUtil.getErrorDialogAccessDialog(
                requireActivity(),
                errorTitle,
                errorDescription,
                getString(R.string.ok_text),
                {
                    settingsErrorDialog?.dismiss()
                    settingsErrorDialog = null

                },
                null,
                null,
        )
        settingsErrorDialog?.show()
    }

}

