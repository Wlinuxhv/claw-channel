package com.clawchannel.app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * 本地存储管理
 * - Token 存储（自动登录）
 * - 设置存储（服务器地址）
 * - 用户信息存储
 */
class TokenStore(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("claw_channel", Context.MODE_PRIVATE)
    
    // ========== Token 相关 ==========
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }
    
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    
    fun hasToken(): Boolean = getAccessToken() != null
    
    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .apply()
    }
    
    // ========== 用户信息 ==========
    
    fun saveUserInfo(userId: Long, username: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }
    
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1)
    
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    
    // ========== 设置相关 ==========
    
    fun saveServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
    }
    
    fun getServerUrl(): String = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    
    // ========== 管理员模式 ==========
    
    fun setAdminMode(isAdmin: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply()
    }
    
    fun isAdmin(): Boolean = prefs.getBoolean(KEY_IS_ADMIN, false)
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_IS_ADMIN = "is_admin"
        
        const val DEFAULT_SERVER_URL = "http://192.168.3.90:8080/"
    }
}