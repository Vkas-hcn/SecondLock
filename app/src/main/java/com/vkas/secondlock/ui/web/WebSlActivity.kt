package com.vkas.secondlock.ui.web

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vkas.secondlock.BR
import com.vkas.secondlock.R
import com.vkas.secondlock.base.BaseActivity
import com.vkas.secondlock.base.BaseViewModel
import com.vkas.secondlock.databinding.ActivityWebBinding
import com.vkas.secondlock.key.Constant

class WebSlActivity : BaseActivity<ActivityWebBinding, BaseViewModel>() {
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_web
    }

    override fun initVariableId(): Int {
        return BR._all
    }
    override fun initToolbar() {
        super.initToolbar()
        binding.webTitleSt.imgLeft.setOnClickListener {
            finish()
        }
        binding.webTitleSt.tvMiddle.text = getString(R.string.privacy_policy)
    }

    override fun initData() {
        super.initData()
        binding.ppWebSt.loadUrl(Constant.PRIVACY_SL_AGREEMENT)
        binding.ppWebSt.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            }

            override fun onPageFinished(view: WebView, url: String) {
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed()
            }
        }

        binding.ppWebSt.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler, error: SslError
            ) {
                val dialog: AlertDialog? = AlertDialog.Builder(this@WebSlActivity)
                    .setTitle("SSL authentication failed. Do you want to continue accessing?")
                    //设置对话框的按钮
                    .setNegativeButton("cancel") { dialog, _ ->
                        dialog.dismiss()
                        handler.cancel()
                    }
                    .setPositiveButton("continue") { dialog, _ ->
                        dialog.dismiss()
                        handler.cancel()
                    }.create()

                val params = dialog!!.window!!.attributes
                params.width = 200
                params.height = 200
                dialog.window!!.attributes = params
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (Constant.PRIVACY_SL_AGREEMENT == url) {
                    view.loadUrl(url)
                } else {
                    // 系统处理
                    return super.shouldOverrideUrlLoading(view, url)
                }
                return true
            }
        }


    }


    //点击返回上一页面而不是退出浏览器
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.ppWebSt.canGoBack()) {
            binding.ppWebSt.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        binding.ppWebSt.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        binding.ppWebSt.clearHistory()
        (binding.ppWebSt.parent as ViewGroup).removeView(binding.ppWebSt)
        binding.ppWebSt.destroy()
        super.onDestroy()
    }
}