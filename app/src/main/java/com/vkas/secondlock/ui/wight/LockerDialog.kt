package com.vkas.secondlock.ui.wight

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.vkas.secondlock.R
import java.util.*

class LockerDialog : Dialog, View.OnClickListener {
    private var mActivity: Activity? = null
    private var onCancelClickListener: OnCancelClickListener? = null

    private var onConfirmClickListener: OnConfirmClickListener? = null
    private var onSingleClickListener: OnSingleClickListener? = null

    private var message = ""
    private lateinit var tvCancel: TextView
    private lateinit var tvConfirm: TextView
    private var confirmTv = "Confirm"

    //单选
    private var singleChoice = false

    interface OnCancelClickListener {
        fun doCancel()
    }

    interface OnConfirmClickListener {
        fun doConfirm()
    }

    interface OnSingleClickListener {
        fun doSingle()
    }

    constructor(activity: Activity, singleChoice: Boolean = false) : super(activity) {
        this.mActivity = activity
        this.singleChoice = singleChoice
        initView()
    }

    constructor(context: Activity, themeResId: Int) : super(context, R.style.dialog) {
        this.mActivity = context
        initView()
    }

    fun setMessage(message: String): LockerDialog? {
        this.message = message
        return this
    }

    fun setConfirmTv(message: String): LockerDialog? {
        this.confirmTv = message
        return this
    }

    fun setCancelButton(onClickListener: OnCancelClickListener?): LockerDialog {
        this.onCancelClickListener = onClickListener
        return this
    }

    fun setConfirmButton(onClickListener: OnConfirmClickListener?): LockerDialog {
        this.onConfirmClickListener = onClickListener
        return this
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        //设置主题透明，是dialog可以显示圆角
        Objects.requireNonNull(window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_tips)
        val dialogWindow = this.window
        dialogWindow!!.setGravity(Gravity.CENTER)
        // 添加动画
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim)
        // 获取对话框当前的参数值
        val lp = dialogWindow.attributes
        lp.width = context.resources.displayMetrics.widthPixels
        val tvMessage: TextView = findViewById(R.id.tv_tips)
        tvMessage.text = this.message
        val linButton: LinearLayout = findViewById(R.id.lin_button)
        val tvSingleConfirm: TextView = findViewById(R.id.tv_single_confirm)
        val imgLogo:ImageView = findViewById(R.id.img_logo)
        val tvState:TextView = findViewById(R.id.tv_state)

        tvSingleConfirm.setOnClickListener(this)
        tvCancel = findViewById(R.id.tv_cancel)
        tvConfirm = findViewById(R.id.tv_confirm)
        tvCancel.setOnClickListener(this)
        tvConfirm.setOnClickListener(this)
        setCanceledOnTouchOutside(false)
        this.setCancelable(false)
        if (this.singleChoice) {
            tvCancel.visibility = View.VISIBLE
            linButton.visibility = View.VISIBLE
            tvSingleConfirm.visibility = View.GONE
            imgLogo.setImageResource(R.drawable.ic_dialog_re)
            tvState.text = "Reset your password"
            tvState.setTextColor(context.resources.getColor(R.color.password_error))
        } else {
            tvCancel.visibility = View.GONE
            linButton.visibility = View.GONE
            tvSingleConfirm.visibility = View.VISIBLE
            tvSingleConfirm.text = this.confirmTv
            imgLogo.setImageResource(R.drawable.ic_dialog_logo)
            tvState.text = "Successful！"
            tvState.setTextColor(context.resources.getColor(R.color.password_success))
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_cancel -> {
                dismiss()
                if (onCancelClickListener != null) {
                    onCancelClickListener!!.doCancel()
                }
            }
            //确认
            R.id.tv_confirm -> {
                dismiss()
                if (onConfirmClickListener != null) {
                    onConfirmClickListener!!.doConfirm()
                }
            }
            R.id.tv_single_confirm -> {
                dismiss()
                if (onSingleClickListener != null) {
                    onSingleClickListener!!.doSingle()
                }
            }
        }
    }
}