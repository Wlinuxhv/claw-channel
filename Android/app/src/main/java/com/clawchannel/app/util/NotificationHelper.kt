package com.clawchannel.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.clawchannel.app.MainActivity

object NotificationHelper {
    
    private const val CHANNEL_ID = "claw_channel_messages"
    private const val CHANNEL_NAME = "消息通知"
    private const val CHANNEL_DESCRIPTION = "AI 助手消息通知"
    
    /**
     * 创建通知渠道（Android 8.0+ 必需）
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF6B6B")
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 显示消息通知
     */
    fun showMessageNotification(
        context: Context,
        messageId: Int,
        title: String,
        message: String
    ) {
        // 创建点击通知后打开的 Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        // 显示通知
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(messageId, notification)
            }
        } catch (e: SecurityException) {
            // 如果没有通知权限，捕获异常
            e.printStackTrace()
        }
    }
    
    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 及以下不需要通知权限
        }
    }
    
    /**
     * 显示连接状态通知
     */
    fun showConnectionNotification(
        context: Context,
        isConnected: Boolean
    ) {
        val title = if (isConnected) "已连接" else "连接断开"
        val message = if (isConnected) "AI 助手已在线" else "请检查网络连接"
        
        showMessageNotification(
            context = context,
            messageId = 1001,
            title = title,
            message = message
        )
    }
}
