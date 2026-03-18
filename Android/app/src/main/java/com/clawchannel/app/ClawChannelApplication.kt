package com.clawchannel.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClawChannelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
