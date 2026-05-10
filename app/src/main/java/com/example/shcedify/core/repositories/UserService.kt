package com.example.shcedify.core.repositories

import com.example.shcedify.core.ResponseService
import com.example.shcedify.onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}