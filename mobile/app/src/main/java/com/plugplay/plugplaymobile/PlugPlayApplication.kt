package com.plugplay.plugplaymobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Главный класс Application для Hilt.
 * Эта аннотация запускает генерацию всего кода Hilt
 * на уровне приложения.
 */
@HiltAndroidApp
class PlugPlayApplication : Application() {
    // Здесь пока не требуется дополнительная логика.
}
