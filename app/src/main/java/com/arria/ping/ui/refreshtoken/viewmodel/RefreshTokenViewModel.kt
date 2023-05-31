package com.arria.ping.ui.refreshtoken.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arria.ping.ui.refreshtoken.model.RefreshTokenResponse
import com.arria.ping.ui.refreshtoken.repository.RefreshTokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefreshTokenViewModel @Inject constructor(
        private val refreshTokenRepository: RefreshTokenRepository
) : ViewModel() {

    private var _refreshTokenResponseMutableLiveData = MutableLiveData<RefreshTokenResponse>()

    val refreshTokenResponseLiveData: LiveData<RefreshTokenResponse>
        get() = _refreshTokenResponseMutableLiveData

    fun getRefreshToken() = viewModelScope.launch {
        _refreshTokenResponseMutableLiveData = refreshTokenRepository.getRefreshToken()
    }

}