package com.jdcoding.houbllaa

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.google.firebase.FirebaseApp
import com.jdcoding.houbllaa.utils.LocaleHelper

/**
 * Application class for the Houblaa app which provides the repositories
 * for the ViewModels and Fragments.
 */
class HouBlaaApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
    
    override fun attachBaseContext(base: Context) {
        // Apply the saved language before attaching the base context
        val language = LocaleHelper.getLanguage(base)
        super.attachBaseContext(LocaleHelper.setLocale(base, language))
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the saved language when configuration changes
        val language = LocaleHelper.getLanguage(this)
        LocaleHelper.setLocale(this, language)
    }
}
