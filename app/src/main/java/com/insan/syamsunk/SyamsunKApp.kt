package com.insan.syamsunk

import android.app.Application
import com.insan.syamsunk.service.DailyRescheduleWorker

class SyamsunKApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DailyRescheduleWorker.enqueueDaily(this)
    }
}
