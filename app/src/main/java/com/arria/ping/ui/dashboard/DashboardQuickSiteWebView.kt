package com.arria.ping.ui.dashboard

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.arria.ping.R
import com.arria.ping.ui.generalview.BaseActivity
import kotlinx.android.synthetic.main.fragment_dashboard_quicksite_webview.*


class DashboardQuickSiteWebView : Fragment() {
    private var activity: BaseActivity? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_dashboard_quicksite_webview, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        initialise()
    }

    private fun initialise() {
        activity = requireActivity() as BaseActivity?
        activity?.showProgressDialog()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(
                    view: WebView,
                    url: String
            ) {
                activity?.dismissProgressDialog()
            }
        }
        webView.loadUrl("https://bi-admin.ping.arria.com/login")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

    }
}