package com.vkas.secondlock.ui.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondlock.BR
import com.vkas.secondlock.R
import com.vkas.secondlock.ad.SlLoadAppAd
import com.vkas.secondlock.ad.SlLoadLockAd
import com.vkas.secondlock.app.App
import com.vkas.secondlock.base.AdBase
import com.vkas.secondlock.base.BaseActivity
import com.vkas.secondlock.bean.SlAppBean
import com.vkas.secondlock.databinding.ActivityMainBinding
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.key.Constant.logTagSl
import com.vkas.secondlock.ui.broad.SlBroadcastReceiver
import com.vkas.secondlock.ui.web.WebSlActivity
import com.vkas.secondlock.ui.wight.LockerDialog
import com.vkas.secondlock.ui.wight.PasswordDialog
import com.vkas.secondlock.ui.wight.SlLockeringDialog
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.SLUtils
import com.vkas.secondlock.utils.SLUtils.isThresholdReached
import com.xuexiang.xutil.net.JsonUtil
import com.xuexiang.xutil.tip.ToastUtils
import kotlinx.coroutines.*

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var appListBean: MutableList<SlAppBean>
    private lateinit var appListAdapter: AppListAdapter
    private var lockFrameJob: Job? = null
    private var liveLock = MutableLiveData<Bundle>()
    private var jobNativeAdsSl: Job? = null
    private var launchLockingJob: Job? = null

    //重复点击
    var repeatClick = false
    private var jobRepeatClick: Job? = null
    private var isHaveAd: Boolean = false

    //点击下标
    private var positionApp: Int = 0

    // 跳转后弹框
    private var bounceBoxAfterJump = false
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_main
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = SLClick()
        binding.inMainTitle.imgLeft.visibility = View.GONE
        binding.inMainTitle.imgRight.setOnClickListener {
            setTitleNav()

            binding.sidebarShowsSL = binding.sidebarShowsSL != true
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            SLUtils.isOnRight = when (checkedId) {
                R.id.radio_button0 -> 0
                R.id.radio_button1 -> 1
                else -> 0
            }
            sortApplicationToEmptyList()
        }
    }

    override fun initData() {
        super.initData()
        liveEventBusReceive()
        initRecyclerView()
        createBroadcast()
        viewModel.setIcon(binding.tvEmpty, this)
        AdBase.getAppInstance().whetherToShowSl = false
        initHomeAd()
    }

    override fun initViewObservable() {
        super.initViewObservable()
        viewModel.liveLock.observe(this, {
            App.whetherEnteredSuccessPassword = true
            showLockFrame(it)
        })
    }

    private fun liveEventBusReceive() {
        LiveEventBus
            .get(Constant.REFRESH_LOCK_LIST, Boolean::class.java)
            .observeForever {
                sortApplicationToEmptyList()
            }
        //插屏关闭后跳转
        LiveEventBus
            .get(Constant.PLUG_SL_ADVERTISEMENT_SHOW, Boolean::class.java)
            .observeForever {
                KLog.e("state", "插屏关闭接收=${it}")
                AdBase.getLockInstance().advertisementLoadingSl(this@MainActivity)
                if(!it){
                    //重复点击
                    jobRepeatClick = lifecycleScope.launch {
                        if (!repeatClick) {
                            App.timesLockingAndUnlocking = 0
                            lockClickJudgment()
                            repeatClick = true
                        }
                        delay(1000)
                        repeatClick = false
                    }
                }
            }
        //密码框关闭后刷新广告
        LiveEventBus
            .get(Constant.WHETHER_REFRESH_NATIVE_AD, Boolean::class.java)
            .observeForever {
                //重复点击
                jobRepeatClick = lifecycleScope.launch {
                    if (!repeatClick) {
                        KLog.e("state", "密码框关闭后刷新广告=${it}")
                        refreshNativeAds()
                        repeatClick = true
                    }
                    delay(1000)
                    repeatClick = false
                }
            }
    }

    private fun initRecyclerView() {
        appListBean = ArrayList()
        SLUtils.isOnRight = 0
        appListBean = SLUtils.appList
        appListAdapter = AppListAdapter(appListBean)
        viewModel.whetherPopUpPasswordSettingBox(this, appListAdapter)
        KLog.e("TAG", "appList---2--${SLUtils.appList.size}")
        var typeEnum = false
        appListBean.forEach {
            if (it.isLocked) {
                typeEnum = true
            }
        }
        binding.dataEmpty = typeEnum

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recAppList.layoutManager = layoutManager
        binding.recAppList.adapter = appListAdapter
        appListAdapter.addChildClickViewIds(R.id.img_down_state)
        appListAdapter.setOnItemChildClickListener { _, _, position ->
            KLog.e("TAG", "setOnItemChildClickListener-11111")
            if (!Settings.canDrawOverlays(this@MainActivity)
            ) {
                KLog.e("TAG", "setOnItemChildClickListener-q")
                requestAlertWindowPermission()
                return@setOnItemChildClickListener
            }
            if (!hasPackageUseStatusPermission()) {
                KLog.e("TAG", "setOnItemChildClickListener-d")

                requestPackageUseStatusPermission()
                return@setOnItemChildClickListener
            }
            KLog.e("TAG", "setOnItemChildClickListener-22222")
            positionApp = position
            if (App.timesLockingAndUnlocking > 3) {
                isHaveAd = true
                launchLockingAdvertisement()
            } else {
                isHaveAd = false
                App.timesLockingAndUnlocking++
                lockClickJudgment()
            }
        }
    }

    /**
     * 加锁点击判断
     */
    private fun lockClickJudgment() {
        showLockFrame(positionApp)
    }

    /**
     * 启动加锁广告
     */
    private fun launchLockingAdvertisement() {
        launchLockingJob = lifecycleScope.launch {
            App.isAppOpenSameDaySl()
            if (isThresholdReached()) {
                KLog.d(logTagSl, "广告达到上线")
            App.timesLockingAndUnlocking = 0
            isHaveAd = false
            lockClickJudgment()
                return@launch
            }
            AdBase.getLockInstance().advertisementLoadingSl(this@MainActivity)
            val mDialog = SlLockeringDialog(this@MainActivity, 8000)
            mDialog.show()
            try {
                withTimeout(8000L) {
                    delay(2000)
                    KLog.e(logTagSl, "jobStartSl?.isActive=${launchLockingJob?.isActive}")
                    while (launchLockingJob?.isActive == true) {
                        val showState =
                            SlLoadLockAd
                                .displayLockAdvertisementSl(this@MainActivity)
                        if (showState) {
                            launchLockingJob?.cancel()
                            launchLockingJob = null
                            mDialog.dismiss()
                        }
                        delay(1000L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                KLog.d(logTagSl, "connect---插屏超时")
                if (launchLockingJob != null) {
                    mDialog.dismiss()
                    lockClickJudgment()
                }
            }
        }
    }

    /**
     * 加锁弹框
     */
    private fun showLockFrame(position: Int) {
        lockFrameJob = lifecycleScope.launch {
            if (!isHaveAd) {
                SlLockeringDialog(this@MainActivity).show()
                delay(1000)
            }
            appListBean.getOrNull(position)?.isLocked =
                appListBean.getOrNull(position)?.isLocked != true
            KLog.e("TAG","appListBean.getOrNull(position)?.isLocked1--->${appListBean.getOrNull(position)?.isLocked}")

            sortApplicationToEmptyList()
            KLog.e("TAG","appListBean.getOrNull(position)?.isLocked2--->${appListBean.getOrNull(position)?.isLocked}")
            viewModel.storeLockedApplications(appListBean)
        }
    }

    /**
     * 应用列表排序,至空
     */
    private fun sortApplicationToEmptyList() {
        val list = appListBean
            .asSequence()
            .distinctBy { it.packageNameSl }
            .sortedByDescending { it.installTime }
            .sortedBy { it.isLocked }
            .toMutableList()
        var typeEnum = false
        if (SLUtils.isOnRight == 0) {
            appListBean.forEach {
                if (it.isLocked) {
                    typeEnum = true
                }
            }
            binding.dataEmpty = typeEnum
        } else {
            binding.dataEmpty = true
        }
        appListBean = list
        appListAdapter.setList(appListBean)
        SLUtils.appList = appListBean
        binding.recAppList.scrollToPosition(0)
    }

    /**
     * 创建广播
     */
    private fun createBroadcast() {
        //创建广播
        val innerReceiver = SlBroadcastReceiver()
        //动态注册广播
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //启动广播
        registerReceiver(innerReceiver, intentFilter)
    }

    private fun Context.hasPackageUseStatusPermission(): Boolean {
        return try {
            val appOpsManager =
                ContextCompat.getSystemService(this, AppOpsManager::class.java) ?: return false
            val mode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    packageName
                )
            } else {
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun Context.requestPackageUseStatusPermission() {
        runCatching {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    data = "package:${applicationContext.packageName}".toUri()
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            App.whetherJumpPermission = true
        }
    }

    private fun Context.requestAlertWindowPermission() {
        runCatching {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = "package:${applicationContext.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            App.whetherJumpPermission = true
        }
    }

    fun setTitleNav() {
        if (binding.sidebarShowsSL == true) {
            binding.inHomeNavigation.inNavTitle.let {
                it.imgRight.visibility = View.VISIBLE
                it.tvMiddle.text = getString(R.string.app_lock)
            }
        } else {
            binding.inHomeNavigation.inNavTitle.let {
                it.imgRight.visibility = View.GONE
                it.tvMiddle.text = getString(R.string.setting)
                it.imgLeft.setOnClickListener {
                    binding.sidebarShowsSL = false
                }
            }
        }

    }

    inner class SLClick {
        fun clickMain() {
            if (binding.sidebarShowsSL == true) {
                setTitleNav()
                binding.sidebarShowsSL = false
            }
        }

        fun toSetPassword() {
            App.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
            setTitleNav()
            binding.sidebarShowsSL = false
            LockerDialog(this@MainActivity, true)
                .setMessage(getString(R.string.are_you_sure_to_reset_them))
                ?.setConfirmButton(object : LockerDialog.OnConfirmClickListener {
                    override fun doConfirm() {
                        SLUtils.clearApplicationData()
                        appListAdapter.notifyDataSetChanged()
                        App.whetherJumpPermission = true
                        PasswordDialog(this@MainActivity, true).show()
                    }
                })
                ?.show()
        }

        fun clickMainMenu() {

        }

        fun toContactUs() {
            val uri = Uri.parse("mailto:${Constant.MAILBOX_SL_ADDRESS}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            runCatching {
                startActivity(intent)
            }.onFailure {
                ToastUtils.toast("Please set up a Mail account")
            }
        }

        fun toPrivacyPolicy() {
            startActivity(WebSlActivity::class.java)
        }

        fun toUpdate() {
            viewModel.openInBrowser(
                this@MainActivity,
                Constant.SHARE_SL_ADDRESS + this@MainActivity.packageName
            )
        }

        fun toShare() {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                Constant.SHARE_SL_ADDRESS + this@MainActivity.packageName
            )
            intent.type = "text/plain"
            startActivity(intent)
        }
    }

    private fun initHomeAd() {
        jobNativeAdsSl = lifecycleScope.launch {
            while (isActive) {
                SlLoadAppAd.setDisplayAppNativeAdSl(this@MainActivity, binding)
                if (AdBase.getAppInstance().whetherToShowSl) {
                    jobNativeAdsSl?.cancel()
                    jobNativeAdsSl = null
                }
                delay(1000L)
            }
        }
    }

    /**
     * 首页弹框
     */
    private fun homeFrame() {
        if (appListBean.isEmpty()) {
            sortApplicationToEmptyList()
        }
        if (App.forgotPassword == Constant.SKIP_TO_NORMAL_PASSWORD) {
            KLog.e("TAG", "onResume-----1")
            viewModel.noPasswordSetPassword(this)
        }
        if (App.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD) {
            KLog.e("TAG", "onResume-----2")
            viewModel.showSettingPasswordPopUp(this)
        }
        if (App.forgotPassword == Constant.SKIP_TO_FORGET_PASSWORD) {
            KLog.e("TAG", "onResume-----3")
            viewModel.showClearPasswordPopUp(this, appListAdapter, -1)
        }
    }

    /**
     * 刷新原生广告
     */
    private fun refreshNativeAds() {
        lifecycleScope.launch {
            delay(300)
            if (App.isFrameDisplayed) {
                return@launch
            }
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            if (App.nativeAdRefreshSl && !App.whetherJumpPermission) {
                AdBase.getAppInstance().whetherToShowSl = false
                if (AdBase.getAppInstance().appAdDataSl != null) {
                    KLog.d(logTagSl, "onResume------>11")
                    SlLoadAppAd.setDisplayAppNativeAdSl(this@MainActivity, binding)
                } else {
                    binding.appListAdSl = false
                    KLog.d(logTagSl, "onResume------>22")
                    AdBase.getAppInstance().advertisementLoadingSl(this@MainActivity)
                    initHomeAd()
                }
            }
            App.whetherJumpPermission = false
        }
    }

    override fun onResume() {
        super.onResume()
        homeFrame()
        refreshNativeAds()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.isFrameDisplayed = false
        App.whetherEnteredSuccessPassword = false
    }
}