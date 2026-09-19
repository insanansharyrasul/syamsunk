package com.example.syamsunk

import android.app.Application
import com.example.syamsunk.service.DailyRescheduleWorker

class SyamsunKApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DailyRescheduleWorker.enqueueDaily(this)
    }
}
