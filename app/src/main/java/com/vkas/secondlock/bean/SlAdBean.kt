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
    val OasisAp_a: String,
    val OasisAp_b: String,
    val OasisAp_c: String,
    val OasisAp_d: Int
)