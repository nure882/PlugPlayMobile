package com.plugplay.plugplaymobile.presentation.auth

// Состояния, которые View будет отображать
sealed interface AuthResultState {
    data object Idle : AuthResultState // Начальное состояние
    data object Loading : AuthResultState
    data object Success : AuthResultState // Вход/Регистрация успешны
    data class Error(val message: String) : AuthResultState
}