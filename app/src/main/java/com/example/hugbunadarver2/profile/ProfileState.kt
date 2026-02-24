package com.example.hugbunadarver2.profile

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val loading: Boolean = false,
    val error: String? = null
)