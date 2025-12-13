package com.plugplay.plugplaymobile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.usecase.GetProfileUseCase
import com.plugplay.plugplaymobile.domain.usecase.UpdateProfileUseCase
import com.plugplay.plugplaymobile.domain.usecase.GetUserOrdersUseCase
import com.plugplay.plugplaymobile.domain.usecase.CancelOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.plugplay.plugplaymobile.domain.model.Order

data class ProfileState(
    val profile: UserProfile? = null,
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isOrdersLoading: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun onAuthStatusChanged(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            if (_state.value.profile == null) {
                loadProfile()
            }
            loadOrders()
        } else {
            _state.value = ProfileState()
        }
    }

    fun loadProfile() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getProfileUseCase()
                .onSuccess { profile ->
                    _state.update { it.copy(profile = profile, isLoading = false) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Помилка завантаження профілю.") }
                }
        }
    }

    fun loadOrders() {
        if (_state.value.isOrdersLoading) return
        _state.update { it.copy(isOrdersLoading = true) }
        viewModelScope.launch {
            getUserOrdersUseCase()
                .onSuccess { orders ->
                    val sortedOrders = orders.sortedByDescending { it.orderDate }
                    _state.update { it.copy(orders = sortedOrders, isOrdersLoading = false) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isOrdersLoading = false, error = throwable.message ?: "Помилка завантаження замовлень.") }
                }
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            cancelOrderUseCase(orderId)
                .onSuccess {
                    _state.update { it.copy(isUpdating = false, updateSuccess = true) }
                    loadOrders()
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isUpdating = false, error = throwable.message ?: "Помилка скасування замовлення.") }
                }
        }
    }

    // --- ЛОГИКА ОБНОВЛЕНИЯ ---
    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        addresses: List<UserAddress>? = null
    ) {
        _state.update { it.copy(isUpdating = true, error = null, updateSuccess = false) }

        // Сохраняем старые адреса, если новые не переданы
        val currentAddresses = _state.value.profile?.addresses ?: emptyList()
        val finalAddresses = addresses ?: currentAddresses

        viewModelScope.launch {
            updateProfileUseCase(firstName, lastName, phoneNumber, email, currentPassword, newPassword, finalAddresses)
                .onSuccess { updatedProfile ->
                    // [FIX] МГНОВЕННОЕ ОБНОВЛЕНИЕ
                    // Мы не перезагружаем профиль из сети. Мы берем то, что вернул сервер
                    // в ответе на UPDATE, и сразу подставляем в UI.
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateSuccess = true,
                            profile = updatedProfile // <-- Вот здесь происходит магия "без перезагрузки"
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            error = throwable.message ?: "Помилка оновлення профілю."
                        )
                    }
                }
        }
    }

    fun updateAddresses(newAddresses: List<UserAddress>) {
        val currentProfile = state.value.profile ?: return
        updateProfile(
            firstName = currentProfile.firstName,
            lastName = currentProfile.lastName,
            phoneNumber = currentProfile.phoneNumber,
            email = currentProfile.email,
            addresses = newAddresses
        )
    }

    fun addAddress(city: String, street: String, house: String, apartment: String?) {
        val currentProfile = state.value.profile ?: return
        if (city.isBlank() || street.isBlank() || house.isBlank()) return

        val newAddress = UserAddress(
            id = null, city = city, street = street, house = house, apartments = apartment?.ifBlank { null }
        )
        val addressesToSend = currentProfile.addresses + listOf(newAddress)
        updateAddresses(addressesToSend)
    }

    fun editAddress(addressId: Int?, city: String, street: String, house: String, apartment: String?) {
        val currentProfile = state.value.profile ?: return
        if (addressId == null) return

        val updatedAddresses = currentProfile.addresses.map { address ->
            if (address.id == addressId) {
                address.copy(city = city, street = street, house = house, apartments = apartment?.ifBlank { null })
            } else {
                address
            }
        }
        updateAddresses(updatedAddresses)
    }

    fun deleteAddress(addressId: Int) {
        val currentProfile = state.value.profile ?: return
        val addressesToSend = currentProfile.addresses.filter { it.id != addressId }
        updateAddresses(addressesToSend)
    }

    fun resetUpdateState() {
        _state.update { it.copy(updateSuccess = false, error = null) }
    }
}