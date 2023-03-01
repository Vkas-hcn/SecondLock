package com.vkas.secondlock.ad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

object SlLoadLockAd {
    private val adBase = AdBase.getLockInstance()

    /**
     * 加载lock插屏广告
     */
    fun loadLockAdvertisementSl(context: Context, adData: SlAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDSl(adBase.adIndexSl, adData.sl_lock)
        KLog.d(logTagSl, "lock--插屏广告id=$id;权重=${adData.sl_lock.getOrNull(adBase.adIndexSl)?.OasisAp_d}")

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let {
                        KLog.d(logTagSl, "lock---连接插屏加载失败=$it") }
                    adBase.isLoadingSl = false
                    adBase.appAdDataSl = null
                    if (adBase.adIndexSl < adData.sl_lock.size - 1) {
                        adBase.adIndexSl++
                        loadLockAdvertisementSl(context,adData)
                    }else{
                        adBase.adIndexSl = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    adBase.loadTimeSl = Date().time
                    adBase.isLoadingSl = false
                    adBase.appAdDataSl = interstitialAd
                    adBase.adIndexSl = 0
                    KLog.d(logTagSl, "lock---返回插屏加载成功")
                }
            })
    }

    /**
     * lock插屏广告回调
     */
    private fun lockScreenAdCallback() {
        (adBase.appAdDataSl  as? InterstitialAd)?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagSl, "lock插屏广告点击")
                    recordNumberOfAdClickSl()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagSl, "关闭lock插屏广告${App.isBackDataSl}")
                    LiveEventBus.get<Boolean>(Constant.PLUG_SL_ADVERTISEMENT_SHOW)
                        .post(App.isBackDataSl)
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
                    KLog.d(logTagSl, "lock----show")
                }
            }
    }

    /**
     * 展示lock广告
     */
    fun displayLockAdvertisementSl(activity: AppCompatActivity): Boolean {
        if (adBase.appAdDataSl == null) {
            KLog.d(logTagSl, "lock--插屏广告加载中。。。")
            return false
        }
        if (adBase.whetherToShowSl || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSl, "lock--前一个插屏广告展示中或者生命周期不对")
            return false
        }
        lockScreenAdCallback()
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (adBase.appAdDataSl as InterstitialAd).show(activity)
        }
        return true
    }
}