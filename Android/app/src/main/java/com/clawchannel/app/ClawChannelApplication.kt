package com.clawchannel.app

import android.app.Application
import com.clawchannel.app.util.NotificationHelper

class ClawChannelApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化共享依赖
        AppDependencies.initialize(this)
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)
    }
}
