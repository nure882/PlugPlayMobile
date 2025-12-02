package com.plugplay.plugplaymobile

import android.app.Application
import android.content.Context
import com.plugplay.plugplaymobile.util.LocaleHelper // Імпорт вашого хелпера
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class PlugPlayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Примусово ставимо дефолтну локаль Java на англійську
        Locale.setDefault(Locale.ENGLISH)
    }

    override fun attachBaseContext(base: Context) {
        // Прикріплюємо контекст з англійською локаллю ("en")
        super.attachBaseContext(LocaleHelper.setLocale(base, "en"))
    }
}