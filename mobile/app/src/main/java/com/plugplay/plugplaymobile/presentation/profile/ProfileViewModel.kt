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
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Error loading profile") }
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
                    _state.update { it.copy(isOrdersLoading = false, error = throwable.message ?: "Error loading orders") }
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
                    _state.update { it.copy(isUpdating = false, error = throwable.message ?: "Error cancelling order") }
                }
        }
    }

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

        viewModelScope.launch {
            // Гарантируем получение самого свежего списка адресов из стейта
            val finalAddresses = addresses ?: (_state.value.profile?.addresses ?: emptyList())

            updateProfileUseCase(firstName, lastName, phoneNumber, email, currentPassword, newPassword, finalAddresses)
                .onSuccess { updatedProfile ->
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateSuccess = true,
                            profile = updatedProfile
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            error = throwable.message ?: "Error updating profile"
                        )
                    }
                }
        }
    }

    // --- ПОЛНОСТЬЮ ИСПРАВЛЕННАЯ ЛОГИКА АДРЕСОВ ---

    fun deleteAddress(addressId: Int) {
        val currentProfile = _state.value.profile ?: return
        val updatedAddresses = currentProfile.addresses.filter { it.id != addressId }

        // Мгновенно обновляем UI
        _state.update { state ->
            state.copy(profile = state.profile?.copy(addresses = updatedAddresses))
        }

        updateAddressesInternal(updatedAddresses)
    }

    fun addAddress(city: String, street: String, house: String, apartment: String?) {
        val currentProfile = _state.value.profile ?: return
        if (city.isBlank() || street.isBlank() || house.isBlank()) return

        // [FIX] Генерируем временный уникальный ID, чтобы UI не открывал форму редактирования
        val tempId = -(System.currentTimeMillis() % 1000000).toInt()

        val newAddress = UserAddress(
            id = tempId,
            city = city,
            street = street,
            house = house,
            apartments = apartment?.ifBlank { null }
        )

        val updatedAddresses = currentProfile.addresses + newAddress

        // Мгновенно обновляем UI
        _state.update { state ->
            state.copy(profile = state.profile?.copy(addresses = updatedAddresses))
        }

        updateAddressesInternal(updatedAddresses)
    }

    fun editAddress(addressId: Int?, city: String, street: String, house: String, apartment: String?) {
        val currentProfile = _state.value.profile ?: return
        if (addressId == null) return

        val updatedAddresses = currentProfile.addresses.map { address ->
            if (address.id == addressId) {
                address.copy(city = city, street = street, house = house, apartments = apartment?.ifBlank { null })
            } else {
                address
            }
        }

        // Мгновенно обновляем UI
        _state.update { state ->
            state.copy(profile = state.profile?.copy(addresses = updatedAddresses))
        }

        updateAddressesInternal(updatedAddresses)
    }

    private fun updateAddressesInternal(newAddresses: List<UserAddress>) {
        val currentProfile = _state.value.profile ?: return
        updateProfile(
            firstName = currentProfile.firstName,
            lastName = currentProfile.lastName,
            phoneNumber = currentProfile.phoneNumber,
            email = currentProfile.email,
            addresses = newAddresses
        )
    }

    fun resetUpdateState() {
        _state.update { it.copy(updateSuccess = false, error = null) }
    }
}