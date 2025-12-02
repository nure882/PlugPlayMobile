package com.plugplay.plugplaymobile.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.usecase.LoginUseCase
import com.plugplay.plugplaymobile.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthResultState>(AuthResultState.Idle)
    val state: StateFlow<AuthResultState> = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.getAuthStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthResultState.Error("Будь ласка, заповніть усі поля.")
            return
        }

        viewModelScope.launch {
            _state.value = AuthResultState.Loading

            loginUseCase(email, password)
                .onSuccess {
                    _state.value = AuthResultState.Success
                }
                .onFailure { error ->
                    _state.value = AuthResultState.Error(error.message ?: "Помилка входу.")
                }
        }
    }

    fun register(firstName: String, lastName: String, phoneNumber: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthResultState.Loading
            registerUseCase(firstName, lastName, phoneNumber, email, password)
                .onSuccess {
                    _state.value = AuthResultState.Success
                }
                .onFailure { error ->
                    _state.value = AuthResultState.Error(error.message ?: "Помилка реєстрації.")
                }
        }
    }

    fun signInWithGoogle(googleIdToken: String) {
        viewModelScope.launch {
            _state.value = AuthResultState.Loading
            authRepository.loginWithGoogle(googleIdToken)
                .onSuccess {
                    _state.value = AuthResultState.Success
                }
                .onFailure { error ->
                    _state.value = AuthResultState.Error(error.message ?: "Помилка входу через Google.")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetState() {
        _state.value = AuthResultState.Idle
    }
}