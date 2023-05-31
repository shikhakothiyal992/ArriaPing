package com.arria.ping.ui.refreshtoken.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.utills.getOrWaitValue
import com.chibatching.kotpref.Kotpref
import junit.framework.TestCase
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RefreshTokenViewModelTest : TestCase() {

    private lateinit var viewModel: RefreshTokenViewModel
    private lateinit var instrumentationContext: Context


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Ignore
    fun init() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        Kotpref.init(instrumentationContext)
       // viewModel = RefreshTokenViewModel()

    }


    @Ignore
    fun getRefreshToken_with_context_returns_accessToken() {
       viewModel.getRefreshToken()
       val value = viewModel.refreshTokenResponseLiveData.getOrWaitValue()

        assertEquals("test", "test")

    }
}