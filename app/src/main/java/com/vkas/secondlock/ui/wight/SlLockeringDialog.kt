package com.vkas.secondlock.ui.wight

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.vkas.secondlock.R
import com.xuexiang.xui.widget.progress.HorizontalProgressView
import java.util.*

class SlLockeringDialog : Dialog, HorizontalProgressView.HorizontalProgressUpdateListener {
    private var mContext: Context? = null

    private var onProgressFinishedListener: OnProgressFinishedListener? = null
    private lateinit var tvSpeedProgress: TextView
    private var timeDelay = 1000
    constructor(context: Context,timeDelay:Int=1000) : super(context) {
        this.mContext = context
        this.timeDelay = timeDelay
        initView()
    }

    interface OnProgressFinishedListener {
        fun doFinished()
    }

    constructor(context: Activity, timeDelay:Int=1000) : super(context, R.style.dialog) {
        this.mContext = context
        this.timeDelay = timeDelay
        initView()
    }

    fun setProgressFinishedButton(onClickListener: OnProgressFinishedListener?): SlLockeringDialog {
        this.onProgressFinishedListener = onClickListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        //设置主题透明，是dialog可以显示圆角
        Objects.requireNonNull(window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_lock)
        val dialogWindow = this.window
        dialogWindow!!.setGravity(Gravity.CENTER)
        // 添加动画
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim)
        // 获取对话框当前的参数值
        val lp = dialogWindow.attributes
        lp.width = context.resources.displayMetrics.widthPixels
        val horProViewSl: HorizontalProgressView = findViewById(R.id.hor_pro_view_sl)
        tvSpeedProgress = findViewById(R.id.tv_speed_progress)
        horProViewSl.setProgressViewUpdateListener(this)
        horProViewSl.setProgressDuration(timeDelay)
        horProViewSl.startProgressAnimation()
        setCanceledOnTouchOutside(false)
        this.setCancelable(false)
    }

    override fun onHorizontalProgressStart(view: View?) {
    }

    override fun onHorizontalProgressUpdate(view: View?, progress: Float) {
        tvSpeedProgress.text = "Locking…${progress.toInt()}%"
    }

    override fun onHorizontalProgressFinished(view: View?) {
        dismiss()
    }
}