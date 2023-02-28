package com.vkas.secondlock.ui.wight

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondlock.R
import com.vkas.secondlock.app.App
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.MmkvUtils
import com.vkas.secondlock.utils.SLUtils
import com.vkas.secondlock.utils.SLUtils.clearApplicationData
import com.xuexiang.xui.utils.Utils
import java.util.*

class PasswordDialog : Dialog, View.OnClickListener, VerifyCodeEditText.OnInputListener {
    private var mContext: Activity? = null
    private var onForgetClickListener: OnForgetClickListener? = null
    private var onFinishClickListener: OnFinishClickListener? = null

    private lateinit var verifyCodeEditText: VerifyCodeEditText
    private lateinit var topText: TextView
    private lateinit var forgetText: TextView
    private lateinit var forgetTip: TextView

    private var fistPassword = ""
    private var secondPassword = ""
    private var isSetPassWord = false
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView
    private lateinit var tv4: TextView
    private lateinit var tv5: TextView
    private lateinit var tv6: TextView
    private lateinit var tv7: TextView
    private lateinit var tv8: TextView
    private lateinit var tv9: TextView
    private lateinit var tv0: TextView
    private lateinit var tvX: TextView
    private lateinit var tvEn:TextView

    //错误次数
    var numberOfErrors = 0

    interface OnForgetClickListener {
        fun doForget()
    }
    interface OnFinishClickListener {
        fun doFinish()
    }
    constructor(context: Activity, isSetPassWord: Boolean = false) : super(context) {
        this.isSetPassWord = isSetPassWord
        this.mContext = context
        initView()
    }

    constructor(context: Activity, themeResId: Int) : super(context, R.style.dialog) {
        this.mContext = context
        initView()
    }

    fun setForgetButton(onClickListener: OnForgetClickListener?): PasswordDialog {
        this.onForgetClickListener = onClickListener
        return this
    }
    fun setFinishButton(onClickListener: OnFinishClickListener?): PasswordDialog {
        this.onFinishClickListener = onClickListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        //设置主题透明，是dialog可以显示圆角
        Objects.requireNonNull(window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.layout_lock_screen)
        val dialogWindow = this.window
        dialogWindow!!.setGravity(Gravity.CENTER)
        // 添加动画
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim)
        // 获取对话框当前的参数值
        val lp = dialogWindow.attributes
        lp.width = context.resources.displayMetrics.widthPixels
        lp.height = context.resources.displayMetrics.heightPixels

        verifyCodeEditText = findViewById(R.id.ed_pass)
        verifyCodeEditText.setOnInputListener(this)
        topText = findViewById(R.id.tv_set_password)
        forgetText = findViewById(R.id.tv_forget)
        forgetTip = findViewById(R.id.tv_forget_tip)
        forgetText.setOnClickListener(this)
        if (this.isSetPassWord) {
            topText.visibility = View.VISIBLE
            forgetText.visibility = View.GONE
            forgetTip.visibility =View.GONE
        } else {
            topText.visibility = View.GONE
            forgetText.visibility = View.VISIBLE
            forgetTip.visibility =View.VISIBLE
        }
        tv0 = findViewById(R.id.tv_0)
        tv0.setOnClickListener(this)
        tv1 = findViewById(R.id.tv_1)
        tv1.setOnClickListener(this)
        tv2 = findViewById(R.id.tv_2)
        tv2.setOnClickListener(this)
        tv3 = findViewById(R.id.tv_3)
        tv3.setOnClickListener(this)
        tv4 = findViewById(R.id.tv_4)
        tv4.setOnClickListener(this)
        tv5 = findViewById(R.id.tv_5)
        tv5.setOnClickListener(this)
        tv6 = findViewById(R.id.tv_6)
        tv6.setOnClickListener(this)
        tv7 = findViewById(R.id.tv_7)
        tv7.setOnClickListener(this)
        tv8 = findViewById(R.id.tv_8)
        tv8.setOnClickListener(this)
        tv9 = findViewById(R.id.tv_9)
        tv9.setOnClickListener(this)
        tvX = findViewById(R.id.tv_x)
        tvX.setOnClickListener(this)
        tvEn = findViewById(R.id.tv_en)
        tvEn.setOnClickListener(this)
        setCanceledOnTouchOutside(false)
        App.isFrameDisplayed = true
        this.setCancelable(false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_0 -> {
                verifyCodeEditText.setText("0", false)
            }
            R.id.tv_1 -> {
                verifyCodeEditText.setText("1", false)
            }
            R.id.tv_2 -> {
                verifyCodeEditText.setText("2", false)
            }
            R.id.tv_3 -> {
                verifyCodeEditText.setText("3", false)
            }
            R.id.tv_4 -> {
                verifyCodeEditText.setText("4", false)
            }
            R.id.tv_5 -> {
                verifyCodeEditText.setText("5", false)
            }
            R.id.tv_6 -> {
                verifyCodeEditText.setText("6", false)
            }
            R.id.tv_7 -> {
                verifyCodeEditText.setText("7", false)
            }
            R.id.tv_8 -> {
                verifyCodeEditText.setText("8", false)
            }
            R.id.tv_9 -> {
                verifyCodeEditText.setText("9", false)
            }
            R.id.tv_x -> {
                verifyCodeEditText.clearInputValue()
                topText.visibility = View.GONE
            }
            R.id.tv_en -> {
                verifyCodeEditText.onKeyDelete()
                topText.visibility = View.GONE
            }
            R.id.tv_forget -> {
                dismiss()
                App.isFrameDisplayed =false
                if (onForgetClickListener != null) {
                    onForgetClickListener!!.doForget()
                }
            }
        }
    }

    /**
     * 设置密码
     */
    private fun setPassword(input: String?) {
        if (Utils.isNullOrEmpty(fistPassword)) {
            fistPassword = input.toString()
            topText.text = context.getString(R.string.input_password_again)
            verifyCodeEditText.clearInputValue()
            return
        } else {
            secondPassword = input.toString()
            if (secondPassword != fistPassword) {
                topText.text = context.getString(R.string.wrong_password)
                verifyCodeEditText.setPasswordErrorColor()
                return
            } else {
                dismiss()
                App.isFrameDisplayed = false
                LiveEventBus.get<Boolean>(Constant.WHETHER_REFRESH_NATIVE_AD)
                    .post(true)
                if (App.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD) {
                    this.mContext?.let { it ->
                        LockerDialog(it)
                            .setMessage(context.getString(R.string.confirm_you_encrypt))
                            ?.setConfirmTv("Sure")
                            ?.setCancelButton(object : LockerDialog.OnCancelClickListener {
                                override fun doCancel() {
                                    App.forgotPassword = Constant.SKIP_TO_ERROR_PASSWORD
//                                    MmkvUtils.set(Constant.LOCK_CODE_SL, "")
                                    PasswordDialog(it, true).show()
                                }
                            })
                            ?.setConfirmButton(object : LockerDialog.OnConfirmClickListener {
                                override fun doConfirm() {
                                    dismiss()
                                    MmkvUtils.set(Constant.LOCK_CODE_SL, secondPassword)
                                    App.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
                                    App.isFrameDisplayed = false
                                    MmkvUtils.set(Constant.STORE_LOCKED_APPLICATIONS, "")
                                    SLUtils.appList.forEach { slAppBean->
                                        slAppBean.isLocked = false
                                    }
                                    LiveEventBus.get<Boolean>(Constant.REFRESH_LOCK_LIST)
                                        .post(true)
                                }
                            })
                            ?.show()
                    }
                } else {
                    MmkvUtils.set(Constant.LOCK_CODE_SL, secondPassword)
                    App.whetherEnteredSuccessPassword = true
                    LiveEventBus.get<Boolean>(Constant.REFRESH_LOCK_LIST)
                        .post(true)
                    this.mContext?.let {
                        LockerDialog(it, false)
                            .setMessage(context.getString(R.string.set_password_successful))
                            ?.setConfirmTv("OK")
                            ?.show()
                    }
                }
            }
        }
    }

    /**
     * 判断密码
     */
    private fun judgePassword(input: String?) {
        val data = App.mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            return
        }
        KLog.e("TAG", "inputValue----->${verifyCodeEditText.inputValue}")
        if (input == data) {
            dismiss()
            App.isFrameDisplayed = false
            LiveEventBus.get<Boolean>(Constant.WHETHER_REFRESH_NATIVE_AD)
                .post(true)
            if (onFinishClickListener != null) {
                onFinishClickListener!!.doFinish()
            }
        } else {
            numberOfErrors += 1
            if (numberOfErrors > 3) {
                this.mContext?.let {
                    LockerDialog(it,true)
                        .setMessage(context.getString(R.string.password_error4_times))
                        ?.setCancelButton(object : LockerDialog.OnCancelClickListener {
                            override fun doCancel() {
                                topText.visibility = View.GONE
                                displayErrorView(false)
                                verifyCodeEditText.clearInputValue()
                            }
                        })
                        ?.setConfirmButton(object : LockerDialog.OnConfirmClickListener {
                            override fun doConfirm() {
                                dismiss()
                                displayErrorView(false)
                                App.isFrameDisplayed = false
                                clearApplicationData()
                                PasswordDialog(mContext!!, true).show()
                            }
                        })
                        ?.show()
                    numberOfErrors = 0
                }
            }
            displayErrorView(true)
        }
    }

    override fun onComplete(input: String?) {
        KLog.e("TAG", "onComplete----->${input}")
        if (App.forgotPassword == Constant.SKIP_TO_NORMAL_PASSWORD) {
            if (Utils.isNullOrEmpty(App.mmkvSl.getString(Constant.LOCK_CODE_SL, ""))) {
                setPassword(input)
            } else {
                judgePassword(input)
            }
        }
        if(App.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD){
            setPassword(input)
        }
        if(App.forgotPassword == Constant.SKIP_TO_FORGET_PASSWORD){
            setPassword(input)
        }
    }

    override fun onChange(input: String?) {
        displayErrorView(false)
    }

    override fun onClear() {
        if (!Utils.isNullOrEmpty(fistPassword)) {
            topText.text = context.getString(R.string.input_password_again)
            verifyCodeEditText.setPasswordNormalColor()
            return
        }
    }

    /**
     * 显示错误view
     */
    private fun displayErrorView(isErrorView: Boolean) {
        if (isErrorView) {
            topText.visibility = View.VISIBLE
            verifyCodeEditText.setPasswordErrorColor()
            topText.text = context.resources.getString(R.string.password_wrong)
            topText.setTextColor(context.resources.getColor(R.color.password_error))
        } else {
            verifyCodeEditText.setPasswordNormalColor()
        }
    }
}