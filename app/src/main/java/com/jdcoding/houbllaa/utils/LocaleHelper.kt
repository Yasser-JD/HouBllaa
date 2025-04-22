package com.jdcoding.houbllaa.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.core.os.ConfigurationCompat
import java.util.Locale

object LocaleHelper {

    private const val PREFERENCE_NAME = "language_pref"
    private const val SELECTED_LANGUAGE = "selected_language"

    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val defaultLanguage = ConfigurationCompat.getLocales(context.resources.configuration)[0]?.language ?: "en"
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    fun setLocale(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    private fun saveLanguage(context: Context, language: String) {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }
        
        return context.createConfigurationContext(configuration)
    }

    fun getDisplayLanguage(language: String): String {
        return when (language) {
            "en" -> "English"
            "fr" -> "Français"
            "ar" -> "العربية"
            else -> "English"
        }
    }
}
