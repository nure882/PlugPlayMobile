// src/main/java/com/plugplay/plugplaymobile/util/LocaleHelper.kt
package com.plugplay.plugplaymobile.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, languageCode: String): ContextWrapper {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setToDefaults() // Скидаємо конфігурацію
        config.setLocale(locale)

        // Оновлюємо конфігурацію контексту
        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }
}