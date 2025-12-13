package com.plugplay.plugplaymobile.presentation.payment

import androidx.lifecycle.ViewModel
import com.plugplay.plugplaymobile.data.model.LiqPayInitResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor() : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Подія для запуску SDK з UI
    private val _shouldLaunchSdk = MutableStateFlow(false)
    val shouldLaunchSdk: StateFlow<Boolean> = _shouldLaunchSdk.asStateFlow()

    // Дані для оплати
    var paymentData: LiqPayInitResponse? = null
    var currentOrderId: Int? = null

    // Цей метод тепер просто дає команду UI запустити SDK
    fun payForOrder() {
        if (paymentData != null) {
            _shouldLaunchSdk.value = true
        }
    }

    // Метод, який UI викличе після того, як успішно запустить лаунчер SDK
    fun onSdkLaunched() {
        _shouldLaunchSdk.value = false
    }
}