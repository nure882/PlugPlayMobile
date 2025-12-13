package com.plugplay.plugplaymobile

import android.app.Application
import android.content.Context
import com.plugplay.plugplaymobile.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import ua.privatbank.liqpay.LiqPay
import java.util.Locale

@HiltAndroidApp
class PlugPlayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Locale.setDefault(Locale.ENGLISH)

        // [ВАЖЛИВО] Ініціалізація SDK
        try {
            LiqPay.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, "en"))
    }
}