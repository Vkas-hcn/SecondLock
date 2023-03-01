package com.vkas.secondlock.base

import android.content.Context
import com.vkas.secondlock.ad.SlLoadAppAd
import com.vkas.secondlock.ad.SlLoadLockAd
import com.vkas.secondlock.ad.SlLoadOpenAd
import com.vkas.secondlock.app.App
import com.vkas.secondlock.bean.SlAdBean
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.SLUtils
import java.util.*

class AdBase {
    companion object {
        fun getOpenInstance() = InstanceHelper.openLoadSl
        fun getAppInstance() = InstanceHelper.appLoadSl
        fun getLockInstance() = InstanceHelper.lockLoadSl
        private var idCounter = 0

    }

    val id = ++idCounter

    object InstanceHelper {
        val openLoadSl = AdBase()
        val appLoadSl = AdBase()
        val lockLoadSl = AdBase()
    }

    var appAdDataSl: Any? = null

    // 是否正在加载中
    var isLoadingSl = false

    //加载时间
    var loadTimeSl: Long = Date().time

    // 是否展示
    var whetherToShowSl = false

    // openIndex
    var adIndexSl = 0

    // 是否是第一遍轮训
    var isFirstRotation: Boolean = false

    /**
     * 广告加载前判断
     */
    fun advertisementLoadingSl(context: Context) {
        App.isAppOpenSameDaySl()
        if (SLUtils.isThresholdReached()) {
            KLog.d(Constant.logTagSl, "广告达到上线")
            return
        }
        if (isLoadingSl) {
            KLog.d(Constant.logTagSl, "${getInstanceName()}--广告加载中，不能再次加载")
            return
        }
        isFirstRotation = false
        if (appAdDataSl == null) {
            isLoadingSl = true
            KLog.d(Constant.logTagSl, "${getInstanceName()}--广告开始加载")
            loadSlartupPageAdvertisementSl(context, SLUtils.getAdServerDataSl())
        }
        if (appAdDataSl != null && !whetherAdExceedsOneHour(loadTimeSl)) {
            isLoadingSl = true
            appAdDataSl = null
            KLog.d(Constant.logTagSl, "${getInstanceName()}--广告过期重新加载")
            loadSlartupPageAdvertisementSl(context, SLUtils.getAdServerDataSl())
        }
    }

    /**
     * 广告是否超过过期（false:过期；true：未过期）
     */
    private fun whetherAdExceedsOneHour(loadTime: Long): Boolean =
        Date().time - loadTime < 60 * 60 * 1000

    /**
     * 加载启动页广告
     */
    private fun loadSlartupPageAdvertisementSl(context: Context, adData: SlAdBean) {
        adLoaders[id]?.invoke(context, adData)
    }

    private val adLoaders = mapOf<Int, (Context, SlAdBean) -> Unit>(
        1 to { context, adData ->
            val adType = adData.sl_open.getOrNull(adIndexSl)?.OasisAp_a
            if (adType == "screen") {
                SlLoadOpenAd.loadSlartInsertAdSl(context, adData)
            } else {
                SlLoadOpenAd.loadOpenAdvertisementSl(context, adData)
            }
        },
        2 to { context, adData ->
            SlLoadAppAd.loadAppAdvertisementSl(context, adData)
        },
        3 to { context, adData ->
            SlLoadLockAd.loadLockAdvertisementSl(context, adData)
        }
    )

    /**
     * 获取实例名称
     */
    private fun getInstanceName(): String {
        return when (id) {
            1 -> {
                "open"
            }
            2 -> {
                "app"
            }
            3 -> {
                "lock"
            }
            else -> {
                ""
            }
        }
    }
}