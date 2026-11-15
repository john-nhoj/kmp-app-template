package com.example.template

import android.app.Application
import com.example.template.shared.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TemplateApplication)
            if (BuildConfig.DEBUG) androidLogger()
        }
    }
}
