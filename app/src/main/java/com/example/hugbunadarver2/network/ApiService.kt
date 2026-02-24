package com.example.hugbunadarver2.network

import com.example.hugbunadarver2.profile.ProfileResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

data class UpdateUsernameRequest(val username: String)
data class UpdateUsernameResponse(val username: String)



interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    // Profile Actions
    @GET("profile/profile")
    suspend fun getUserProfile(): ProfileResponse
    @PATCH("profile/profile")
    suspend fun updateUsername(@Body req: UpdateUsernameRequest): ResponseBody


}
