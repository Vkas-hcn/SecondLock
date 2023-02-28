package com.vkas.secondlock.key

object Constant {
    // 是否刷新原生广告
    const val WHETHER_REFRESH_NATIVE_AD = "whetherRefreshNativeAd"
    // 是否刷新加锁列表
    const val REFRESH_LOCK_LIST = "refreshLockList"
    // 跳转忘记密码
    const val SKIP_TO_FORGET_PASSWORD = "skipToForgetPassword"
    // 跳转错误密码
    const val SKIP_TO_ERROR_PASSWORD = "skipToErrorPassword"
    // 正常跳转
    const val SKIP_TO_NORMAL_PASSWORD = "skipToNormalPassword"

    //错误次数
    const val NUMBER_OF_ERRORS = "numberOfErrors"
    //锁密码
    const val LOCK_CODE_SL = "lockCodeSl"
    const val STORE_LOCKED_APPLICATIONS = "storeLockedApplications"
    //应用列表
    const val APPLICATION_LIST_SL = "application_list_sl"
    const val APP_PACKAGE_NAME = "com.vkas.spacelocker"
    // 分享地址
    const val SHARE_SL_ADDRESS="https://play.google.com/store/apps/details?id="
    // privacy_agreement
    const val PRIVACY_SL_AGREEMENT="https://www.baidu.com/"
    // email
    const val MAILBOX_SL_ADDRESS="vkas@qq.com"
    const val RETURN_SL_CURRENT_PAGE ="returnSlCurrentPage"
    // 广告数据
    const val ADVERTISING_SL_DATA="advertisingSlData"
    // 广告包名
    const val ADVERTISING_SL_PACKAGE="com.google.android.gms.ads.AdActivity"
    // 当日日期
    const val CURRENT_SL_DATE="currentSlDate"
    // 点击次数
    const val CLICKS_SL_COUNT="clicksSlCount"
    // 展示次数
    const val SHOW_SL_COUNT="showSlCount"
    //日志tag
    const val logTagSl = "logTagSl"
    //开屏关闭跳转
    const val OPEN_CLOSE_JUMP = "openCloseJump"
    //计时器数据
    const val TIMER_SL_DATA = "timerSlData"
    // 最后时间
    const val LAST_TIME = "lastTime"
    //服务器信息
    const val SERVER_SL_INFORMATION = "serverSlInformation"
    //连接状态
    const val CONNECTION_SL_STATUS = "connectionSlStatus"
    //绕流数据
    const val AROUND_SL_FLOW_DATA = "aroundSlFlowData"
    // 服务器数据
    const val PROFILE_SL_DATA ="profileSlData"
    // 最佳服务器
    const val PROFILE_SL_DATA_FAST ="profileSlDataFast"
    // 是否已连接
    const val WHETHER_SL_CONNECTED="whetherSlConnected"
    // 当前服务器
    const val CURRENT_SL_SERVICE="currentSlService"
    // lock插屏广告展示
    const val PLUG_SL_ADVERTISEMENT_SHOW="plugSlAdvertisementShow"
    // Faster server
    const val FASTER_SL_SERVER= "Faster server"
    //ip信息
    const val IP_INFORMATION= "ipInformation"
    // 已连接返回
    const val CONNECTED_SL_RETURN="connectedSlReturn"
    // 未连接返回
    const val NOT_CONNECTED_SL_RETURN="notConnectedSlReturn"

    const val LOCK_IS_INIT_DB = "lock_is_init_db" //是否初始化了数据库表

    const val LOCK_IS_INIT_FAVITER = "lock_is_init_faviter" //是否初始化了faviter数据表

}