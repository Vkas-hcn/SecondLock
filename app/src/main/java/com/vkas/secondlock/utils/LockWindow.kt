package com.vkas.secondlock.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.vkas.secondlock.R
import com.vkas.secondlock.app.App.Companion.mmkvSl
import com.vkas.secondlock.key.Constant
import com.vkas.secondlock.ui.wight.VerifyCodeEditText
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.display.ScreenUtils

class LockWindow : VerifyCodeEditText.OnInputListener {
    companion object {
        fun getInstance() = InstanceHelper.lockWindowHelper
    }

    object InstanceHelper {
        val lockWindowHelper = LockWindow()
    }

    var mWindowManager: WindowManager? = null
    var wmParams: WindowManager.LayoutParams? = null
    var mFloatingLayout: View? = null

    //    var wmParamsDialog: WindowManager.LayoutParams? = null
//    var mFloatingLayoutDialog: View? = null
    private lateinit var context: Context

    private lateinit var verifyCodeEditText: VerifyCodeEditText
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
    private lateinit var forgetTextLL: LinearLayout
    private lateinit var topText: TextView
    private lateinit var conDialogTip: ConstraintLayout
    private lateinit var tvConfirm: TextView


    fun initWindow(context: Context) {
        if (mWindowManager != null) {
            return
        }
//        initWindowDialog(context)
        this.context = context
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //设置好悬浮窗的参数
        wmParams = params
        wmParams!!.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        wmParams!!.x = 0
        wmParams!!.y = 50
        wmParams!!.width = WindowManager.LayoutParams.MATCH_PARENT
        wmParams!!.height = WindowManager.LayoutParams.MATCH_PARENT
        wmParams!!.format = PixelFormat.OPAQUE
        wmParams!!.alpha = 1.0f
        processPasswordControls(context)
    }

    //设置可以显示在状态栏上
    //设置悬浮窗口长宽数据
    private val params: WindowManager.LayoutParams
        private get() {
            wmParams = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wmParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                wmParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            //设置可以显示在状态栏上
            wmParams!!.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            val screenWidth = ScreenUtils.getScreenWidth()
            //设置悬浮窗口长宽数据
            wmParams!!.width = screenWidth
            wmParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
            //        wmParams.horizontalMargin=ScreenUtils.dip2px(10);
            return wmParams as WindowManager.LayoutParams
        }

    /**
     * 处理密码控件
     */
    private fun processPasswordControls(context: Context) {
        val inflater = LayoutInflater.from(context)
        mFloatingLayout = inflater.inflate(R.layout.layout_lock_screen, null)
        //寻找控件
        verifyCodeEditText = mFloatingLayout?.findViewById(R.id.ed_pass)!!
        verifyCodeEditText.setOnInputListener(this)
        tv1 = mFloatingLayout?.findViewById(R.id.tv_1)!!
        tv2 = mFloatingLayout?.findViewById(R.id.tv_2)!!
        tv3 = mFloatingLayout?.findViewById(R.id.tv_3)!!
        tv4 = mFloatingLayout?.findViewById(R.id.tv_4)!!
        tv5 = mFloatingLayout?.findViewById(R.id.tv_5)!!
        tv6 = mFloatingLayout?.findViewById(R.id.tv_6)!!
        tv7 = mFloatingLayout?.findViewById(R.id.tv_7)!!
        tv8 = mFloatingLayout?.findViewById(R.id.tv_8)!!
        tv9 = mFloatingLayout?.findViewById(R.id.tv_9)!!
        tv0 = mFloatingLayout?.findViewById(R.id.tv_0)!!
        tvX = mFloatingLayout?.findViewById(R.id.tv_x)!!
        tvEn = mFloatingLayout?.findViewById(R.id.tv_en)!!
        forgetTextLL = mFloatingLayout?.findViewById(R.id.ll_forget)!!
        forgetTextLL.visibility =View.GONE
        topText = mFloatingLayout?.findViewById(R.id.tv_set_password)!!

        conDialogTip = mFloatingLayout?.findViewById(R.id.con_dialog_tip)!!
        tvConfirm =mFloatingLayout?.findViewById(R.id.tv_confirm)!!

        tv1.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("1", false)
        }
        tv2.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("2", false)
        }
        tv3.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("3", false)
        }
        tv4.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("4", false)
        }
        tv5.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("5", false)
        }
        tv6.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("6", false)
        }
        tv7.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("7", false)
        }
        tv8.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("8", false)
        }
        tv9.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("9", false)
        }
        tv0.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("0", false)
        }
        tvX.setOnClickListener { v: View? ->
            verifyCodeEditText.clearInputValue()
        }
        tvEn.setOnClickListener { v: View? ->
            verifyCodeEditText.onKeyDelete()
        }

        conDialogTip.setOnClickListener {}

        tvConfirm.setOnClickListener {
            conDialogTip.visibility = View.GONE
            displayErrorView(false)
            verifyCodeEditText.clearInputValue()
        }
    }

    override fun onComplete(input: String?) {
        KLog.e("TAG", "onComplete========")
        val data = mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            return
        }
        KLog.e("TAG", "inputValue----->${verifyCodeEditText.inputValue}")
        if (verifyCodeEditText.inputValue == data) {
            closeThePasswordBox()
        } else {
            var num = mmkvSl.decodeInt(Constant.NUMBER_OF_ERRORS, 0)
            num += 1
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, num)
            if (num > 3) {
                resetPasswordPopup()
            }
            displayErrorView(true)
        }
    }

    override fun onChange(input: String?) {
    }

    override fun onClear() {
        displayErrorView(false)
    }

    /**
     * 显示错误view
     */
    private fun displayErrorView(isErrorView: Boolean) {
        if (isErrorView) {
            verifyCodeEditText.setPasswordErrorColor()
        } else {
            verifyCodeEditText.setPasswordNormalColor()
        }
    }

    /**
     * 展示密码框
     */
    fun showPasswordBox() {
        // 添加悬浮窗的视图
        if (mWindowManager != null) {
            mWindowManager!!.addView(mFloatingLayout, wmParams)
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
        }
    }

    /**
     * 关闭密码框
     */
    fun closeThePasswordBox() {
        //移除悬浮窗
        if (mWindowManager != null) {
            mWindowManager!!.removeView(mFloatingLayout)
            mWindowManager = null
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
        }

    }

    /**
     * 重置密码弹框
     */
    private fun resetPasswordPopup() {
        conDialogTip.visibility = View.VISIBLE
        MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
    }
}