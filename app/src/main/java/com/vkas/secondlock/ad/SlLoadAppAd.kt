package com.vkas.secondlock.ad

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.SnackbarUtils.addView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.vkas.secondlock.app.App
import com.vkas.secondlock.base.AdBase
import com.vkas.secondlock.bean.SlAdBean
import com.vkas.secondlock.databinding.ActivityMainBinding
import com.vkas.secondlock.key.Constant.logTagSl
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.SLUtils.recordNumberOfAdClickSl
import com.vkas.secondlock.utils.SLUtils.recordNumberOfAdDisplaysSl
import com.vkas.secondlock.utils.SLUtils.takeSortedAdIDSl
import java.util.*
import com.vkas.secondlock.R
import com.vkas.secondlock.ui.wight.RoundCornerOutlineProvider

object SlLoadAppAd {
    private val adBase = AdBase.getAppInstance()

    /**
     * 加载vpn原生广告
     */
    fun loadAppAdvertisementSl(context: Context, adData: SlAdBean) {
        val id = takeSortedAdIDSl(adBase.adIndexSl, adData.sl_app)
        KLog.d(logTagSl, "home---原生广告id=$id;权重=${adData.sl_app.getOrNull(adBase.adIndexSl)?.OasisAp_d}")

        val vpnNativeAds = AdLoader.Builder(
            context.applicationContext,
            id
        )
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
            .build()

        vpnNativeAds.withNativeAdOptions(adOptions)
        vpnNativeAds.forNativeAd {
            adBase.appAdDataSl = it
        }
        vpnNativeAds.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                val error =
                    """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                adBase.isLoadingSl = false
                adBase.appAdDataSl = null
                KLog.d(logTagSl, "home---加载vpn原生加载失败: $error")

                if (adBase.adIndexSl < adData.sl_app.size - 1) {
                    adBase.adIndexSl++
                    loadAppAdvertisementSl(context,adData)
                }else{
                    adBase.adIndexSl = 0
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                KLog.d(logTagSl, "home---加载vpn原生广告成功")
                adBase.loadTimeSl = Date().time
                adBase.isLoadingSl = false
                adBase.adIndexSl = 0
            }

            override fun onAdOpened() {
                super.onAdOpened()
                KLog.d(logTagSl, "home---点击vpn原生广告")
                recordNumberOfAdClickSl()
            }
        }).build().loadAd(AdRequest.Builder().build())
    }

    /**
     * 设置展示App原生广告
     */
    fun setDisplayAppNativeAdSl(activity: AppCompatActivity, binding: ActivityMainBinding) {
        activity.runOnUiThread {
            adBase.appAdDataSl?.let { adData ->
                if (adData is NativeAd && !adBase.whetherToShowSl && activity.lifecycle.currentState == Lifecycle.State.RESUMED && !App.isFrameDisplayed) {
                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(R.layout.layout_main_native_sl, null) as NativeAdView
                    // 对应原生组件
                    setCorrespondingNativeComponentSl(adData, adView)
                    binding.slAdFrame.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    binding.appListAdSl = true
                    recordNumberOfAdDisplaysSl()
                    adBase.whetherToShowSl = true
                    App.nativeAdRefreshSl = false
                    adBase.appAdDataSl = null
                    KLog.d(logTagSl, "home--原生广告--展示")
                    //重新缓存
                    AdBase.getAppInstance().advertisementLoadingSl(activity)
                }
            }
        }
    }

    private fun setCorrespondingNativeComponentSl(nativeAd: NativeAd, adView: NativeAdView) {
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)

        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}