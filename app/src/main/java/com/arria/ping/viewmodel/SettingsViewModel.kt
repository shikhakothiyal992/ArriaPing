package com.arria.ping.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arria.ping.model.login.Logout
import com.arria.ping.model.responsehandlers.Response
import com.arria.ping.repository.SettingsService
import com.arria.ping.util.ProgressBarDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val settingsService: SettingsService,
) : ViewModel() {


    private var progressBar: ProgressBarDialog? = null
    private var _logoutMutableLiveData = MutableLiveData<Response<Logout?>>()

    val logoutMutableLiveData: LiveData<Response<Logout?>>
        get() = _logoutMutableLiveData


    fun doLogout() {
        progressBar = ProgressBarDialog.instance
        _logoutMutableLiveData.value = Response.loading(null)

        viewModelScope.launch {
            _logoutMutableLiveData.value = settingsService.doLogout()
        }
    }

}