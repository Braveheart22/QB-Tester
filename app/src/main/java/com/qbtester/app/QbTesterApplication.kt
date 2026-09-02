package com.qbtester.app

import android.app.Application
import com.qbtester.app.di.AppContainer

class QbTesterApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
