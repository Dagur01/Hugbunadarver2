package com.example.hugbunadarver2.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.SignUpRequest
import kotlinx.coroutines.launch

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class SignUpViewModel : ViewModel() {

    var state by mutableStateOf(SignUpState())
        private set

    fun onEmailChange(v: String) { state = state.copy(email = v) }
    fun onPasswordChange(v: String) { state = state.copy(password = v) }
    fun onConfirmPasswordChange(v: String) { state = state.copy(confirmPassword = v) }

    fun signUp(onSuccess: (String) -> Unit) {
        if (state.password != state.confirmPassword) {
            state = state.copy(error = "Passwords do not match")
            return
        }
        if (state.password.length < 8) {
            state = state.copy(error = "Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.signUp(SignUpRequest(state.email, state.password))

                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(loading = false)
                    onSuccess(res.body()!!.token)
                } else {
                    val msg = res.errorBody()?.string()?.ifBlank { null }
                        ?: "Email taken or weak password"
                    state = state.copy(loading = false, error = msg)
                }
            } catch (e: Exception) {
                state = state.copy(loading = false, error = "Network error")
            }
        }
    }
}