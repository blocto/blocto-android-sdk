package com.portto.valuedapp

import android.app.Application
import timber.log.Timber

class ValueDapp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}