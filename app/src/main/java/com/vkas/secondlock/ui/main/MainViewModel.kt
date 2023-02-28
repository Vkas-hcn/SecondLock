package com.vkas.secondlock.ui.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.vkas.secondlock.R
import com.vkas.secondlock.app.App
import com.vkas.secondlock.app.App.Companion.mmkvSl
import com.vkas.secondlock.base.BaseViewModel
import com.vkas.secondlock.bean.SlAppBean
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.ui.wight.LockerDialog
import com.vkas.secondlock.ui.wight.PasswordDialog
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.MmkvUtils
import com.vkas.secondlock.utils.SLUtils
import com.vkas.secondlock.utils.SLUtils.clearApplicationData
import com.vkas.secondlock.utils.SLUtils.updateLockedContent
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil

class MainViewModel (application: Application) : BaseViewModel(application) {
    val liveLock: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * 存储已加锁的应用
     */
    fun storeLockedApplications(appList: MutableList<SlAppBean>) {
        val lockApps: MutableList<String> = ArrayList()
        appList.forEach {
            if (it.isLocked) {
                it.packageNameSl?.let { it1 ->
                    lockApps.add(it1)
                }
            }
        }
        MmkvUtils.set(Constant.STORE_LOCKED_APPLICATIONS, JsonUtil.toJson(lockApps))
        SLUtils.appList = updateLockedContent(appList)
        KLog.e("TAG", "JsonUtil.toJson(lockApps)===${JsonUtil.toJson(lockApps)}")
    }

    /**
     * 是否弹出设置密码框
     */
    fun whetherPopUpPasswordSettingBox(activity: Activity, appListAdapter: AppListAdapter) {
        if (App.isFrameDisplayed) {
            return
        }
        val data = mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            PasswordDialog(activity, true).show()
        }else{
            PasswordDialog(activity, false)
                .setForgetButton(object : PasswordDialog.OnForgetClickListener {
                    override fun doForget() {
                        showClearPasswordPopUp(activity, appListAdapter,-1)
                    }
                })
                .show()
        }
    }

    /**
     * 展示清除密码弹框
     */
    fun showClearPasswordPopUp(activity: Activity, appListAdapter: AppListAdapter,pos: Int) {
        KLog.e("TAG", "展示清除密码弹框-----1")
        LockerDialog(activity,true)
            .setMessage(activity.getString(R.string.are_you_sure_to_reset_them))
            ?.setCancelButton(object : LockerDialog.OnCancelClickListener {
                override fun doCancel() {
                    unlockJump(activity, appListAdapter,pos)
                }
            })
            ?.setConfirmButton(object : LockerDialog.OnConfirmClickListener {
                override fun doConfirm() {
                    clearApplicationData()
                    appListAdapter.notifyDataSetChanged()
                    if (App.isFrameDisplayed) {
                        return
                    }
                    PasswordDialog(activity, true).show()
                }
            })
            ?.show()
        App.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
    }

    /**
     * 展示设置密码弹框
     */
    fun showSettingPasswordPopUp(activity: Activity) {
        if (App.isFrameDisplayed) {
            return
        }
        PasswordDialog(activity, true).show()
    }

    /**
     * 无密码设置密码
     */
    fun noPasswordSetPassword(activity: Activity) {
        KLog.e("TAG", "展示首页密码弹框----1")
        if (App.isFrameDisplayed) {
            return
        }
        KLog.e("TAG", "展示首页密码弹框----2")
        if (Utils.isNullOrEmpty(mmkvSl.getString(Constant.LOCK_CODE_SL, ""))) {
            App.isFrameDisplayed = true
            KLog.e("TAG"," App.isFrameDisplayed==${ App.isFrameDisplayed}")
            PasswordDialog(activity, true).show()
        }
    }

    /**
     * 点击弹出密码弹框
     */
    fun clickToPopPasswordBox(activity: Activity, appListAdapter: AppListAdapter, pos: Int) {
        if (App.isFrameDisplayed) {
            return
        }
        unlockJump(activity, appListAdapter, pos)
    }


    /**
     * 解锁跳转
     */
    private fun unlockJump(activity: Activity, appListAdapter: AppListAdapter, pos: Int) {
        if (App.isFrameDisplayed) {
            return
        }
        PasswordDialog(activity, false)
            .setForgetButton(object : PasswordDialog.OnForgetClickListener {
                override fun doForget() {
                    showClearPasswordPopUp(activity, appListAdapter,pos)
                }
            })
            .setFinishButton(object : PasswordDialog.OnFinishClickListener {
                override fun doFinish() {
                    if(pos!=-1){
                        liveLock.postValue(pos)
                    }
                }
            })
            .show()
    }
    fun setIcon(textView:TextView,context: Context){
        val text = textView.text.toString()
        val startIndex = text.indexOf("[icon]")
        val endIndex = startIndex + "[icon]".length

        val spannable = SpannableString(text)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_lock)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

        val span = drawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
        spannable.setSpan(span, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        textView.text = spannable
    }
    fun openInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

}