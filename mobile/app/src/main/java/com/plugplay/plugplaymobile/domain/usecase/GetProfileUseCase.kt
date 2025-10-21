package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return repository.getProfile()
    }
}