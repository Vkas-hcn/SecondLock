package com.vkas.secondlock.bean

import androidx.annotation.Keep

data class SlAdBean (
    var sl_open: MutableList<SlDetailBean> = ArrayList(),
    var sl_app: MutableList<SlDetailBean> = ArrayList(),
    var sl_lock: MutableList<SlDetailBean> = ArrayList(),

    var sl_click_num: Int = 0,
    var sl_show_num: Int = 0
)
@Keep
data class SlDetailBean(
    val sl_id: String,
    val sl_platform: String,
    val sl_type: String,
    val sl_weight: Int
)