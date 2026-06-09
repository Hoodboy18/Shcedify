package com.example.shcedify.onboarding.personal.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserProfile(
    val id: String = "",
    val firstName: String = "",
    val secondName: String = "",
    val lastName: String = "",
    val secondLastName: String = "",
    val numCuenta: String = "",
    val carrera: String = "",
    val phone: String = "",
    val birthDate: String = ""
)