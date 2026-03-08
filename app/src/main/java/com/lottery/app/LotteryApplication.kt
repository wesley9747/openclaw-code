package com.lottery.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 彩票管理 App 入口
 */
@HiltAndroidApp
class LotteryApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化操作
    }
}