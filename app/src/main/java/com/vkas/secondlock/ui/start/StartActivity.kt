package com.vkas.secondlock.ui.start

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondlock.BR
import com.vkas.secondlock.BuildConfig
import com.vkas.secondlock.R
import com.vkas.secondlock.ad.SlLoadOpenAd
import com.vkas.secondlock.app.App
import com.vkas.secondlock.base.AdBase
import com.vkas.secondlock.base.BaseActivity
import com.vkas.secondlock.base.BaseViewModel
import com.vkas.secondlock.databinding.ActivityStartBinding
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.key.Constant.logTagSl
import com.vkas.secondlock.service.LockService
import com.vkas.secondlock.ui.broad.SlBroadcastReceiver
import com.vkas.secondlock.ui.main.MainActivity
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.MmkvUtils
import com.vkas.secondlock.utils.SLUtils.getAppList
import com.vkas.secondlock.utils.SLUtils.isThresholdReached
import com.xuexiang.xui.widget.progress.HorizontalProgressView
import kotlinx.coroutines.*

class StartActivity : BaseActivity<ActivityStartBinding, BaseViewModel>(),
    HorizontalProgressView.HorizontalProgressUpdateListener {
    companion object {
        var isCurrentPage: Boolean = false
    }
    private var liveJumpHomePage = MutableLiveData<Boolean>()
    private var liveJumpHomePage2 = MutableLiveData<Boolean>()
    private var jobOpenAdsSt: Job? = null
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_start
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
        isCurrentPage = intent.getBooleanExtra(Constant.RETURN_SL_CURRENT_PAGE, false)

    }

    override fun initToolbar() {
        super.initToolbar()
    }

    override fun initData() {
        super.initData()
        binding.pbStartSt.setProgressViewUpdateListener(this)
        binding.pbStartSt.setProgressDuration(8000)
        binding.pbStartSt.startProgressAnimation()
        liveEventBusSt()
//        lifecycleScope.launch(Dispatchers.IO) {
//            EasyConnectUtils.getIpInformation()
//        }
        startServiceAndBroadcast()
        getAppList(this)
        getFirebaseDataSt()
        jumpHomePageData()
    }

    private fun liveEventBusSt() {
        LiveEventBus
            .get(Constant.OPEN_CLOSE_JUMP, Boolean::class.java)
            .observeForever {
                KLog.d(logTagSl, "关闭开屏内容-接收==${this.lifecycle.currentState}")
                if (this.lifecycle.currentState == Lifecycle.State.STARTED) {
                    jumpPage()
                }
            }
    }
    /**
     * 开启服务和广播
     */
    fun startServiceAndBroadcast() {
        val innerReceiver = SlBroadcastReceiver()
        //动态注册广播
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //启动广播
        registerReceiver(innerReceiver, intentFilter)
        val intentOne = Intent(this, LockService::class.java)
        startService(intentOne)
    }
    private fun getFirebaseDataSt() {
        if (BuildConfig.DEBUG) {
            preloadedAdvertisement()
//            lifecycleScope.launch {
//                val ips = listOf("192.168.0.1", "8.8.8.8", "114.114.114.114")
//                val fastestIP = findFastestIP(ips)
//                KLog.e("TAG", "Fastest IP: $fastestIP")
//                delay(1500)
//                MmkvUtils.set(
//                    Constant.ADVERTISING_SL_DATA,
//                    ResourceUtils.readStringFromAssert("elAdDataFireBase.json")
//                )
//            }
            return
        } else {
            preloadedAdvertisement()
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                MmkvUtils.set(Constant.PROFILE_SL_DATA, auth.getString("st_ser"))
                MmkvUtils.set(Constant.PROFILE_SL_DATA_FAST, auth.getString("st_smar"))
                MmkvUtils.set(Constant.AROUND_SL_FLOW_DATA, auth.getString("stAroundFlow_Data"))
                MmkvUtils.set(Constant.ADVERTISING_SL_DATA, auth.getString("SLAD_Data"))

            }
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
    }

    private fun jumpHomePageData() {
        liveJumpHomePage2.observe(this, {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                KLog.e("TAG", "isBackDataSt==${App.isBackDataSl}")
                delay(300)
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    jumpPage()
                }
            }
        })
        liveJumpHomePage.observe(this, {
            liveJumpHomePage2.postValue(true)
        })
    }

    /**
     * 跳转页面
     */
    private fun jumpPage() {
        // 不是后台切回来的跳转，是后台切回来的直接finish启动页
        if (!isCurrentPage) {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    /**
     * 加载广告
     */
    private fun loadAdvertisement() {
        // 开屏
        AdBase.getOpenInstance().adIndexSl = 0
        AdBase.getOpenInstance().advertisementLoadingSl(this)
        rotationDisplayOpeningAdSl()
        // 首页原生
        AdBase.getAppInstance().adIndexSl = 0
        AdBase.getAppInstance().advertisementLoadingSl(this)

        // 加锁插屏
        AdBase.getLockInstance().adIndexSl = 0
        AdBase.getLockInstance().advertisementLoadingSl(this)
    }

    /**
     * 轮训展示开屏广告
     */
    private fun rotationDisplayOpeningAdSl() {
        jobOpenAdsSt = lifecycleScope.launch {
            try {
                withTimeout(8000L) {
                    delay(3000L)
                    while (isActive) {
                        val showState = SlLoadOpenAd
                            .judgeConditionsOpenAd(this@StartActivity)
                        if (showState) {
                            jobOpenAdsSt?.cancel()
                            jobOpenAdsSt = null
                            binding.pbStartSt.stopProgressAnimation()
                            binding.pbStartSt.progress = 100F
                        }
                        delay(1000L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                KLog.e("TimeoutCancellationException I'm sleeping $e")
                jumpPage()
            }
        }
    }

    /**
     * 预加载广告
     */
    private fun preloadedAdvertisement() {
        App.isAppOpenSameDaySl()
        if (isThresholdReached()) {
            KLog.d(logTagSl, "广告达到上线")
            lifecycleScope.launch {
                delay(3000L)
                binding.pbStartSt.stopProgressAnimation()
                binding.pbStartSt.progress = 100F
                liveJumpHomePage.postValue(true)
            }
        } else {
            loadAdvertisement()
        }
    }

    override fun onHorizontalProgressStart(view: View?) {
    }

    override fun onHorizontalProgressUpdate(view: View?, progress: Float) {

    }

    override fun onHorizontalProgressFinished(view: View?) {
        App.isAppOpenSameDaySl()
        if (!isThresholdReached()) {
            SlLoadOpenAd
                .displayOpenAdvertisementSl(this@StartActivity)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
    }
}