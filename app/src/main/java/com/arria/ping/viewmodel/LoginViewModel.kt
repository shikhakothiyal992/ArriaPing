package com.arria.ping.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.model.login.Login
import com.arria.ping.model.profile.UserProfile
import com.arria.ping.model.responsehandlers.Response
import com.arria.ping.repository.LoginService
import com.arria.ping.util.ProgressBarDialog
import com.arria.ping.util.StorePrefData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
        private val loginService: LoginService,
) : ViewModel() {


    private var progressBar: ProgressBarDialog? = null
    private var _loginMutableLiveData = MutableLiveData<Response<Login?>>()

    val loginMutableLiveData: LiveData<Response<Login?>>
        get() = _loginMutableLiveData


    fun getLogin(loginRequest: LoginRequest) {
        progressBar = ProgressBarDialog.instance
        _loginMutableLiveData.value = Response.loading(null)

        viewModelScope.launch {
            _loginMutableLiveData.value = loginService.getLogin(loginRequest)
        }
    }

    fun setUserDataInPreferences(
            userProfile: UserProfile,
    ) {
        StorePrefData.firstName = userProfile.firstName
        StorePrefData.lastName = userProfile.lastName
        StorePrefData.role = userProfile.role
        StorePrefData.email = userProfile.email
        StorePrefData.StoreIdFromLogin = userProfile.storeId
        StorePrefData.isStoreSelected = true
        StorePrefData.isSupervisorSelected = true
        StorePrefData.isAreaSelected = true
        StorePrefData.isStateSelected = true
        StorePrefData.isForGMCheckInTimeFromLogin = true
    }

}