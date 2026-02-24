package com.example.hugbunadarver2.network

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse
}
