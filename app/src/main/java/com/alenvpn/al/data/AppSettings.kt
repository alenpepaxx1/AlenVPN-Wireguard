package com.alenvpn.al.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSettings {
    private const val PREFS_NAME = "vpn_settings"
    private lateinit var prefs: SharedPreferences

    private val _theme = MutableStateFlow("System")
    val theme = _theme.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()

    private val _autoConnect = MutableStateFlow(false)
    val autoConnect = _autoConnect.asStateFlow()

    private val _killSwitch = MutableStateFlow(false)
    val killSwitch = _killSwitch.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _theme.value = prefs.getString("theme", "System") ?: "System"
        _language.value = prefs.getString("language", "en") ?: "en"
        _autoConnect.value = prefs.getBoolean("autoConnect", false)
        _killSwitch.value = prefs.getBoolean("killSwitch", false)
    }

    fun setTheme(themeVal: String) {
        prefs.edit().putString("theme", themeVal).apply()
        _theme.value = themeVal
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _language.value = lang
    }

    fun setAutoConnect(enabled: Boolean) {
        prefs.edit().putBoolean("autoConnect", enabled).apply()
        _autoConnect.value = enabled
    }

    fun setKillSwitch(enabled: Boolean) {
        prefs.edit().putBoolean("killSwitch", enabled).apply()
        _killSwitch.value = enabled
    }
}
