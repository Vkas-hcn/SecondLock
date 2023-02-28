package com.vkas.secondlock.service

import android.app.Service
import kotlinx.coroutines.Job
import android.app.*
import android.content.Intent
import android.os.IBinder
import com.xuexiang.xui.utils.Utils
import kotlinx.coroutines.*

import android.graphics.BitmapFactory

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vkas.secondlock.R
import com.vkas.secondlock.utils.FrontProcessManagement
import com.vkas.secondlock.utils.LockWindow
import com.vkas.secondlock.utils.SLUtils

class LockService : Service() {
    private var mLockJob: Job? = null
    override fun onCreate() {
        super.onCreate()
        mLockJob = GlobalScope.launch(Dispatchers.IO) {
            // 上一个App包名
            var lastAppPackageName = ""
            while (true) {
                delay(300)
                val top = FrontProcessManagement.getForegroundPackageName(applicationContext)
                if (!Utils.isNullOrEmpty(top) && top != lastAppPackageName) {
                    lastAppPackageName = top
                    SLUtils.appList.forEach {
                        if (it.isLocked && top == it.packageNameSl) {
                            withContext(Dispatchers.Main) {
                                LockWindow.getInstance().initWindow(applicationContext)
                                LockWindow.getInstance().showPasswordBox()
                            }
                        }
                    }
                }
            }
        }
        val messageNotificatioManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("5996773", "Oasis Applock", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true) //是否在桌面icon右上角展示小红点
            channel.lightColor = Color.GREEN //小红点颜色
            channel.setShowBadge(false) //是否在久按桌面图标时显示此渠道的通知
            messageNotificatioManager.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        builder.setContentText("Oasis Applock is working")
        builder.setWhen(System.currentTimeMillis())
        builder.setChannelId("5996773")
        val notification: Notification = builder.build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}