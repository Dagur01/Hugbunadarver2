package com.example.hugbunadarver2.friends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.FriendProfileDto
import com.example.hugbunadarver2.network.FriendRequestDto
import com.example.hugbunadarver2.network.SendFriendRequestRequest
import kotlinx.coroutines.launch
import com.example.hugbunadarver2.network.MovieInvitationDto
import com.example.hugbunadarver2.network.MovieInviteRequest

data class FriendsState(
    val emailInput: String = "",
    val friends: List<String> = emptyList(),
    val pendingRequests: List<FriendRequestDto> = emptyList(),
    val selectedProfile: FriendProfileDto? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val inviteEmailInput: String = "",
    val sentMovieInvitations: List<com.example.hugbunadarver2.network.MovieInvitationDto> = emptyList(),
    val receivedMovieInvitations: List<com.example.hugbunadarver2.network.MovieInvitationDto> = emptyList()
)

class FriendsViewModel : ViewModel() {
    var state by mutableStateOf(FriendsState())
        private set

    fun onEmailInputChange(value: String) {
        state = state.copy(emailInput = value)
    }

    fun loadFriends() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.getFriendsList()
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(
                        friends = res.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Failed to load friends"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun sendFriendRequest() {
        val email = state.emailInput.trim()
        if (email.isBlank()) {
            state = state.copy(error = "Please enter an email")
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true, error = null, successMessage = null)
            try {
                val res = ApiClient.api.sendFriendRequest(
                    SendFriendRequestRequest(email)
                )

                if (res.isSuccessful) {
                    state = state.copy(
                        loading = false,
                        successMessage = "Friend request sent",
                        emailInput = ""
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Failed to send friend request"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.getPendingFriendRequests()
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(
                        pendingRequests = res.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Failed to load friend requests"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun acceptFriendRequest(id: Long) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.acceptFriendRequest(id)
                if (res.isSuccessful) {
                    state = state.copy(
                        pendingRequests = state.pendingRequests.filterNot { it.id == id },
                        successMessage = "Friend request accepted"
                    )
                    loadFriends()
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to accept request"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }

    fun rejectFriendRequest(id: Long) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.rejectFriendRequest(id)
                if (res.isSuccessful) {
                    state = state.copy(
                        pendingRequests = state.pendingRequests.filterNot { it.id == id },
                        successMessage = "Friend request rejected"
                    )
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to reject request"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }

    fun loadFriendProfile(email: String) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null, selectedProfile = null)
            try {
                val res = ApiClient.api.getFriendProfile(email)
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(
                        selectedProfile = res.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Failed to load profile"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun clearSelectedProfile() {
        state = state.copy(selectedProfile = null)
    }

    fun clearMessage() {
        state = state.copy(error = null, successMessage = null)
    }

    fun onInviteEmailChange(value: String) {
        state = state.copy(inviteEmailInput = value)
    }

    fun sendMovieInvitation(movieId: Long) {
        val email = state.inviteEmailInput.trim()
        if (email.isBlank()) {
            state = state.copy(error = "Please enter a friend email")
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true, error = null, successMessage = null)
            try {
                val res = ApiClient.api.inviteFriendToMovie(
                    MovieInviteRequest(
                        email = email,
                        movieId = movieId
                    )
                )

                if (res.isSuccessful) {
                    state = state.copy(
                        loading = false,
                        successMessage = "Movie invitation sent",
                        inviteEmailInput = ""
                    )
                    loadSentMovieInvitations()
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Failed to send movie invitation"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun loadSentMovieInvitations() {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.getSentMovieInvitations()
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(sentMovieInvitations = res.body()!!)
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to load sent invitations"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }

    fun loadReceivedMovieInvitations() {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.getReceivedMovieInvitations()
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(receivedMovieInvitations = res.body()!!)
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to load received invitations"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }

    fun acceptMovieInvitation(id: Long) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.acceptMovieInvitation(id)
                if (res.isSuccessful) {
                    state = state.copy(successMessage = "Invitation accepted")
                    loadReceivedMovieInvitations()
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to accept invitation"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }

    fun rejectMovieInvitation(id: Long) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.rejectMovieInvitation(id)
                if (res.isSuccessful) {
                    state = state.copy(successMessage = "Invitation rejected")
                    loadReceivedMovieInvitations()
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to reject invitation"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }
}