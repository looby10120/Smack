package com.example.smack.Controller

import android.app.Application
import com.example.smack.Utilities.SharePrefs

class App : Application() {

    companion object {
        lateinit var prefs: SharePrefs
    }

    override fun onCreate() {
        prefs = SharePrefs(applicationContext)
        super.onCreate()
    }
}