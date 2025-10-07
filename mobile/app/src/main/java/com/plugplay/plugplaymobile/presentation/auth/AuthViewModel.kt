package com.plugplay.plugplaymobile.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.usecase.LoginUseCase
import com.plugplay.plugplaymobile.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // Состояние, которое View будет наблюдать
    private val _state = MutableStateFlow<AuthResultState>(AuthResultState.Idle)
    val state: StateFlow<AuthResultState> = _state.asStateFlow()

    // 💡 Функции для обработки действий пользователя

    fun login(email: String, password: String) {
        // Базовая валидация (можно улучшить!)
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthResultState.Error("Будь ласка, заповніть усі поля.")
            return
        }

        viewModelScope.launch {
            _state.value = AuthResultState.Loading

            loginUseCase(email, password)
                .onSuccess {
                    _state.value = AuthResultState.Success // 🚀 Вход успешен!
                }
                .onFailure { error ->
                    _state.value = AuthResultState.Error(error.message ?: "Помилка входу.")
                }
        }
    }

    fun register(name: String, email: String, password: String) {
        // (Реализация регистрации аналогична, но вызывает RegisterUseCase)
        viewModelScope.launch {
            _state.value = AuthResultState.Loading
            registerUseCase(name, email, password)
                .onSuccess { _state.value = AuthResultState.Success }
                .onFailure { error ->
                    _state.value = AuthResultState.Error(error.message ?: "Помилка реєстрації.")
                }
        }
    }

    fun resetState() {
        _state.value = AuthResultState.Idle
    }
}