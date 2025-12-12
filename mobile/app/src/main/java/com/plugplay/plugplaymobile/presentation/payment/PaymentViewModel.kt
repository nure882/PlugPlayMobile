package com.plugplay.plugplaymobile.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.usecase.InitPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val initPaymentUseCase: InitPaymentUseCase
) : ViewModel() {

    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Сюди передамо ID з навігації
    var currentOrderId: Int? = null

    fun payForOrder() {
        val orderId = currentOrderId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            initPaymentUseCase(orderId)
                .onSuccess { url ->
                    _paymentUrl.value = url // Це викличе відкриття браузера
                    _isLoading.value = false
                }
                .onFailure {
                    // Тут можна обробити помилку
                    _isLoading.value = false
                }
        }
    }

    // Скидаємо URL після відкриття, щоб не відкривалося двічі
    fun onPaymentUrlOpened() {
        _paymentUrl.value = null
    }
}