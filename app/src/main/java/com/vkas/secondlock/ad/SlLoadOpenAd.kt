package com.vkas.secondlock.ad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondlock.app.App
import com.vkas.secondlock.base.AdBase
import com.vkas.secondlock.bean.SlAdBean
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.key.Constant.logTagSl
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.SLUtils.recordNumberOfAdClickSl
import com.vkas.secondlock.utils.SLUtils.recordNumberOfAdDisplaysSl
import com.vkas.secondlock.utils.SLUtils.takeSortedAdIDSl
import com.xuexiang.xutil.net.JsonUtil
import java.util.*

object SlLoadOpenAd {
    private val adBase = AdBase.getOpenInstance()
    /**
     * 加载启动页广告
     */
    private fun loadSlartupPageAdvertisementSl(context: Context, adData: SlAdBean) {
        if (adData.sl_open.getOrNull(adBase.adIndexSl)?.sl_type == "screen") {
            loadSlartInsertAdSl(context, adData)
        } else {
            loadOpenAdvertisementSl(context, adData)
        }
    }

    /**
     * 加载开屏广告
     */
    fun loadOpenAdvertisementSl(context: Context, adData: SlAdBean) {
        KLog.e("loadOpenAdvertisementSl", "adData().sl_open=${JsonUtil.toJson(adData.sl_open)}")
        KLog.e(
            "loadOpenAdvertisementSl",
            "id=${JsonUtil.toJson(takeSortedAdIDSl(adBase.adIndexSl, adData.sl_open))}"
        )

        val id = takeSortedAdIDSl(adBase.adIndexSl, adData.sl_open)

        KLog.d(logTagSl, "open--开屏广告id=$id;权重=${adData.sl_open.getOrNull(adBase.adIndexSl)?.sl_weight}")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            id,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    adBase.loadTimeSl = Date().time
                    adBase.isLoadingSl = false
                    adBase.appAdDataSl = ad

                    KLog.d(logTagSl, "open--开屏广告加载成功")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adBase.isLoadingSl = false
                    adBase.appAdDataSl = null
                    if (adBase.adIndexSl < adData.sl_open.size - 1) {
                        adBase.adIndexSl++
                        loadSlartupPageAdvertisementSl(context, adData)
                    } else {
                        adBase.adIndexSl = 0
                        if(!adBase.isFirstRotation){
                            AdBase.getOpenInstance().advertisementLoadingSl(context)
                            adBase.isFirstRotation =true
                        }
                    }
                    KLog.d(logTagSl, "open--开屏广告加载失败: " + loadAdError.message)
                }
            }
        )
    }


    /**
     * 开屏广告回调
     */
    private fun advertisingOpenCallbackSl() {
        if (adBase.appAdDataSl !is AppOpenAd) {
            return
        }
        (adBase.appAdDataSl as AppOpenAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                //取消全屏内容
                override fun onAdDismissedFullScreenContent() {
                    KLog.d(logTagSl, "open--关闭开屏内容")
                    adBase.whetherToShowSl = false
                    adBase.appAdDataSl = null
                    if (!App.whetherBackgroundSl) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
                }

                //全屏内容无法显示时调用
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    adBase.whetherToShowSl = false
                    adBase.appAdDataSl = null
                    KLog.d(logTagSl, "open--全屏内容无法显示时调用")
                }

                //显示全屏内容时调用
                override fun onAdShowedFullScreenContent() {
                    adBase.appAdDataSl = null
                    adBase.whetherToShowSl = true
                    recordNumberOfAdDisplaysSl()
                    adBase.adIndexSl = 0
                    KLog.d(logTagSl, "open---开屏广告展示")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    KLog.d(logTagSl, "open---点击open广告")
                    recordNumberOfAdClickSl()
                }
            }
    }

    /**
     * 展示Open广告
     */
    fun displayOpenAdvertisementSl(activity: AppCompatActivity) {
        if (adBase.appAdDataSl == null) {
            KLog.d(logTagSl, "open---开屏广告加载中。。。")
            return
        }
        if (adBase.whetherToShowSl || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSl, "open---前一个开屏广告展示中或者生命周期不对")
            return
        }
        if (adBase.appAdDataSl is AppOpenAd) {
            advertisingOpenCallbackSl()
            (adBase.appAdDataSl as AppOpenAd).show(activity)
        } else {
            startInsertScreenAdCallbackSl()
            (adBase.appAdDataSl as InterstitialAd).show(activity)
        }
    }
    /**
     * 判断open广告展示条件
     */
    fun judgeConditionsOpenAd(activity: AppCompatActivity): Boolean{
        if (adBase.appAdDataSl == null) {
            KLog.d(logTagSl, "open---开屏广告加载中。。。")
            return false
        }
        if (adBase.whetherToShowSl || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSl, "open---前一个开屏广告展示中或者生命周期不对")
            return false
        }
        return true
    }

    /**
     * 加载启动页插屏广告
     */
    fun loadSlartInsertAdSl(context: Context, adData: SlAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDSl(adBase.adIndexSl, adData.sl_open)
        KLog.d(
            logTagSl,
            "open--插屏广告id=$id;权重=${adData.sl_open.getOrNull(adBase.adIndexSl)?.sl_weight}"
        )

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { KLog.d(logTagSl, "open---连接插屏加载失败=$it") }
                    adBase.isLoadingSl = false
                    adBase.appAdDataSl = null
                    if (adBase.adIndexSl < adData.sl_open.size - 1) {
                        adBase.adIndexSl++
                        loadSlartupPageAdvertisementSl(context, adData)
                    } else {
                        adBase.adIndexSl = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    adBase.loadTimeSl = Date().time
                    adBase.isLoadingSl = false
                    adBase.appAdDataSl = interstitialAd
                    KLog.d(logTagSl, "open--启动页插屏加载完成")
                }
            })
    }

    /**
     * SlartInsert插屏广告回调
     */
    private fun startInsertScreenAdCallbackSl() {
        if (adBase.appAdDataSl !is InterstitialAd) {
            return
        }
        (adBase.appAdDataSl as InterstitialAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagSl, "open--插屏广告点击")
                    recordNumberOfAdClickSl()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagSl, "open--关闭SlartInsert插屏广告${App.isBackDataSl}")
                    if (!App.whetherBackgroundSl) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
                    adBase.appAdDataSl = null
                    adBase.whetherToShowSl = false
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    KLog.d(logTagSl, "Ad failed to show fullscreen content.")
                    adBase.appAdDataSl = null
                    adBase.whetherToShowSl = false
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    KLog.e("TAG", "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    adBase.appAdDataSl = null
                    recordNumberOfAdDisplaysSl()
                    // Called when ad is shown.
                    adBase.whetherToShowSl = true
                    adBase.adIndexSl = 0
                    KLog.d(logTagSl, "open----插屏show")
                }
            }
    }
}