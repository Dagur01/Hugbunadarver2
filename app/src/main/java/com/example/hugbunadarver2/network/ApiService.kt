package com.example.hugbunadarver2.network

import com.example.hugbunadarver2.profile.ProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse
    @GET("profile/profile")
    suspend fun getUserProfile(): ProfileResponse

}
