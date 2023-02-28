package com.vkas.secondlock.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.google.gson.reflect.TypeToken
import com.vkas.secondlock.app.App.Companion.mmkvSl
import com.vkas.secondlock.bean.SlAppBean
import com.vkas.secondlock.key.Constant
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SLUtils {
    //应用列表
    var appList:MutableList<SlAppBean> = ArrayList()
    //是否在右边（在App列表界面）
    var isOnRight:Int = 0
    /**
     * 获取应用列表
     */
    @DelicateCoroutinesApi
    fun getAppList(context: Context){
        GlobalScope.launch(Dispatchers.IO) {
            getAllLauncherIconPackages(context)
        }
    }
    // 需要获取所有apk 添加permission <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>
    private fun getAllLauncherIconPackages(context: Context) {
        val launcherIconPackageList: MutableList<SlAppBean> = ArrayList()
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        //set MATCH_ALL to prevent any filtering of the results
        val resolveInfos =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        for (info in resolveInfos) {
            val data =  context.packageManager.getApplicationInfo(info.activityInfo.packageName,0)
            if(info.activityInfo.packageName != context.packageName){
                val slAppBean = SlAppBean()
                slAppBean.appNameSl = context.packageManager.getApplicationLabel(data).toString()
                slAppBean.packageNameSl = info.activityInfo.packageName
                slAppBean.appIconSl = context.packageManager.getApplicationIcon(data)
                slAppBean.installTime = context.packageManager.getPackageInfo(info.activityInfo.packageName, 0).firstInstallTime
                launcherIconPackageList.add(slAppBean)
            }
        }
        appList = updateLockedContent(launcherIconPackageList)
            .asSequence()
            .distinctBy { it.packageNameSl }
            .sortedByDescending { it.installTime }
            .sortedBy { it.isLocked }
            .toMutableList()
        KLog.e("TAG","appList---1--${appList.size}")
    }

    /**
     * 更新已加锁内容
     */
    fun updateLockedContent(launcherIconPackageList:MutableList<SlAppBean>):MutableList<SlAppBean>{
        var lockApps: MutableList<String> = ArrayList()
        val data = mmkvSl.decodeString(Constant.STORE_LOCKED_APPLICATIONS, "")
        if (!Utils.isNullOrEmpty(data)) {
            lockApps = JsonUtil.fromJson(
                data,
                object : TypeToken<MutableList<String>?>() {}.type
            )
        }
        lockApps.forEach { lock ->
            launcherIconPackageList.forEach { all ->
                if (all.packageNameSl == lock) {
                    all.isLocked = true
                }
            }
        }
        return launcherIconPackageList
    }
    /**
     * 清除应用数据
     */
    fun clearApplicationData(){
        //清除密码
        MmkvUtils.set(Constant.LOCK_CODE_SL, "")
        MmkvUtils.set(Constant.STORE_LOCKED_APPLICATIONS, "")
        appList.forEach {
            it.isLocked = false
        }
    }

}