package com.vkas.secondlock.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.blankj.utilcode.util.ProcessUtils
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tencent.mmkv.MMKV
import com.vkas.secondlock.BuildConfig
import com.vkas.secondlock.base.AppManagerSlMVVM
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.ui.start.StartActivity
import com.vkas.secondlock.utils.ActivityUtils
import com.vkas.secondlock.utils.CalendarUtils
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.MmkvUtils
import com.xuexiang.xui.XUI
import com.xuexiang.xutil.XUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class App : Application(), LifecycleObserver {
    private var flag = 0
    private var job_sl: Job? = null
    private var ad_activity_sl: Activity? = null
    private var top_activity_sl: Activity? = null

    companion object {
        //是否输入成功密码过
        var whetherEnteredSuccessPassword = false
        // 加锁，解锁次数
        var timesLockingAndUnlocking = 0
        //弹框是否正在展示
        var isFrameDisplayed = false

        //忘记密码
        var forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD

        // app当前是否在后台
        var isBackDataSl = false

        // 是否进入后台（三秒后）
        var whetherBackgroundSl = false
        // 是否是跳转权限
        var whetherJumpPermission = false
        // 原生广告刷新
        var nativeAdRefreshSl = false
        val mmkvSl by lazy {
            //启用mmkv的多进程功能
            MMKV.mmkvWithID("SecondLock", MMKV.MULTI_PROCESS_MODE)
        }

        //当日日期
        var adDateSl = ""

        /**
         * 判断是否是当天打开
         */
        fun isAppOpenSameDaySl() {
            adDateSl = mmkvSl.decodeString(Constant.CURRENT_SL_DATE, "").toString()
            if (adDateSl == "") {
                MmkvUtils.set(Constant.CURRENT_SL_DATE, CalendarUtils.formatDateNow())
            } else {
                if (CalendarUtils.dateAfterDate(adDateSl, CalendarUtils.formatDateNow())) {
                    MmkvUtils.set(Constant.CURRENT_SL_DATE, CalendarUtils.formatDateNow())
                    MmkvUtils.set(Constant.CLICKS_SL_COUNT, 0)
                    MmkvUtils.set(Constant.SHOW_SL_COUNT, 0)
                }
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
//        initCrash()
        setActivityLifecycleSl(this)
        MobileAds.initialize(this) {}
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (ProcessUtils.isMainProcess()) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            Firebase.initialize(this)
            FirebaseApp.initializeApp(this)
            XUI.init(this) //初始化UI框架
            XUtil.init(this)
            LiveEventBus
                .config()
                .lifecycleObserverAlwaysActive(true)
            //是否开启打印日志
            KLog.init(BuildConfig.DEBUG)
        }
//        Core.init(this, MainActivity::class)
//        sendTimerInformation()
        isAppOpenSameDaySl()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        nativeAdRefreshSl = true
        job_sl?.cancel()
        job_sl = null
        //从后台切过来，跳转启动页
        if (whetherBackgroundSl && !isBackDataSl && forgotPassword == Constant.SKIP_TO_NORMAL_PASSWORD && !whetherJumpPermission) {
            jumpGuidePage()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopState() {
        job_sl = GlobalScope.launch {
            whetherBackgroundSl = false
            delay(3000L)
            whetherBackgroundSl = true
            ad_activity_sl?.finish()
            ActivityUtils.getActivity(StartActivity::class.java)?.finish()
        }
    }

    /**
     * 跳转引导页
     */
    private fun jumpGuidePage() {
        whetherBackgroundSl = false
        val intent = Intent(top_activity_sl, StartActivity::class.java)
        intent.putExtra(Constant.RETURN_SL_CURRENT_PAGE, true)
        top_activity_sl?.startActivity(intent)
    }

    private fun setActivityLifecycleSl(application: Application) {
        //注册监听每个activity的生命周期,便于堆栈式管理
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                AppManagerSlMVVM.get().addActivity(activity)
                if (activity !is AdActivity) {
                    top_activity_sl = activity
                } else {
                    ad_activity_sl = activity
                }
                KLog.v("Lifecycle", "onActivityCreated" + activity.javaClass.name)
            }

            override fun onActivityStarted(activity: Activity) {
                KLog.v("Lifecycle", "onActivityStarted" + activity.javaClass.name)
                if (activity !is AdActivity) {
                    top_activity_sl = activity
                } else {
                    ad_activity_sl = activity
                }
                flag++
                isBackDataSl = false
            }

            override fun onActivityResumed(activity: Activity) {
                KLog.v("Lifecycle", "onActivityResumed=" + activity.javaClass.name)
                if (activity !is AdActivity) {
                    top_activity_sl = activity
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity is AdActivity) {
                    ad_activity_sl = activity
                } else {
                    top_activity_sl = activity
                }
                KLog.v("Lifecycle", "onActivityPaused=" + activity.javaClass.name)
            }

            override fun onActivityStopped(activity: Activity) {
                flag--
                if (flag == 0) {
                    isBackDataSl = true
                }
                KLog.v("Lifecycle", "onActivityStopped=" + activity.javaClass.name)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                KLog.v("Lifecycle", "onActivitySaveInstanceState=" + activity.javaClass.name)

            }

            override fun onActivityDestroyed(activity: Activity) {
                AppManagerSlMVVM.get().removeActivity(activity)
                KLog.v("Lifecycle", "onActivityDestroyed" + activity.javaClass.name)
                ad_activity_sl = null
                top_activity_sl = null
            }
        })
    }
}