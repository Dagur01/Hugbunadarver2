package com.example.hugbunadarver2.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.LoginRequest
import kotlinx.coroutines.launch

data class LoginState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginState())
        private set

    fun onEmailChange(v: String) { state = state.copy(email = v) }
    fun onPasswordChange(v: String) { state = state.copy(password = v) }

    fun login(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.login(LoginRequest(state.email, state.password))
                state = state.copy(loading = false)
                onSuccess(res.token)
            } catch (e: Exception) {
                state = state.copy(loading = false, error = "Innskráning mistókst")
            }
        }
    }
}
