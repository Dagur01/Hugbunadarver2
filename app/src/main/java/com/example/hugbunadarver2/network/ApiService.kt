package com.example.hugbunadarver2.network

import com.example.hugbunadarver2.profile.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)
data class SignUpRequest(val email: String, val password: String)
data class AuthResponse(val token: String)

data class UpdateUsernameRequest(val username: String)
data class UpdateUsernameResponse(val username: String)



interface ApiService {

        @POST("auth/login")
        suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

        @POST("auth/signup")
        suspend fun signUp(@Body req: SignUpRequest): Response<AuthResponse>

    @GET("profile/profile")
    suspend fun getUserProfile(): Response<ProfileResponse>

    @PATCH("profile/profile")
    suspend fun updateUsername(@Body req: UpdateUsernameRequest): ResponseBody

    @Multipart
    @PATCH("profile/profile/picture")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): ResponseBody

}
