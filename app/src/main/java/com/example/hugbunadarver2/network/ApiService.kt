package com.example.hugbunadarver2.network

import com.example.hugbunadarver2.home.Movie
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
import retrofit2.http.DELETE

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)
data class SignUpRequest(val email: String, val password: String)
data class AuthResponse(val token: String)

data class UpdateUsernameRequest(val username: String)
data class UpdateUsernameResponse(val username: String)
data class AddMovieRequest(
    val title: String,
    val genre: String,
    val ageRating: Int,
    val duration: Int
)

data class UpdateMovieRequest(
    val title: String,
    val genre: String,
    val ageRating: Int,
    val duration: Int,
    val nowShowing: Boolean
)

data class ScreeningDto(
    val id: Long,
    val screeningTime: String
)

data class MovieHallDto(
    val movieHallId: Long,
    val name: String,
    val location: String,
    val nowShowing: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class SeatDto(
    val seatId: Long,
    val rowNumber: Int,
    val seatNumber: Int,
    val price: Int?,
    val booked: Boolean
)

data class CreateBookingRequest(
    val movieId: Long,
    val hallId: Long,
    val seatId: Long,
    val screeningId: Long,
    val discountCode: String? = null
)

data class BookingDto(
    val bookingid: Long,
    val movie: Movie,
    val movieHall: MovieHallDto,
    val seat: SeatDto,
    val screening: ScreeningDto,
    val discountCode: String?,
    val discountPercent: Int?
)

data class SendFriendRequestRequest(
    val email: String
)

data class SimpleUserDto(
    val userId: Long,
    val email: String,
    val username: String?
)

data class FriendRequestDto(
    val id: Long,
    val status: String,
    val createdAt: String,
    val fromUser: SimpleUserDto,
    val toUser: SimpleUserDto
)

data class FriendProfileDto(
    val email: String,
    val username: String?,
    val isFriend: Boolean,
    val friends: List<String>,
    val profilePictureBase64: String?
)

data class MovieInviteRequest(
    val email: String,
    val movieId: Long
)

data class MovieInvitationDto(
    val id: Long,
    val movieId: Long,
    val status: String,
    val createdAt: String,
    val inviter: SimpleUserDto,
    val invitee: SimpleUserDto
)


interface ApiService {

    /**
     * User authentication
     */
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signUp(@Body req: SignUpRequest): Response<AuthResponse>

    /**
     * Profile
     */
    @GET("profile/profile")
    suspend fun getUserProfile(): Response<ProfileResponse>

    @PATCH("profile/profile")
    suspend fun updateUsername(@Body req: UpdateUsernameRequest): ResponseBody

    @Multipart
    @PATCH("profile/profile/picture")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): ResponseBody

    @DELETE("profile/delete")
    suspend fun deleteAccount(): Response<Unit>

    /**
     * Movies
     */
    @GET("movies")
    suspend fun getMovies(): Response<List<Movie>>

    @GET("movies/{id}")
    suspend fun getMovieById(@Path("id") id: Int): Response<Movie>

    @GET("favorites")
    suspend fun getFavorites(): Response<List<Movie>>

    /**
     * Favorites
     */
    @POST("favorites/{movieId}")
    suspend fun addFavorite(@Path("movieId") movieId: Long): Response<Unit>

    @DELETE("favorites/{movieId}")
    suspend fun removeFavorite(@Path("movieId") movieId: Long): Response<Unit>

    @GET("movies/genre/{genre}")
    suspend fun getMoviesByGenre(
        @Path("genre") genre: String
    ): Response<List<Movie>>

    /**
     * Admin
     */
    @POST("movies")
    suspend fun addMovie(@Body movie: AddMovieRequest): Response<Movie>

    @PATCH("movies/{movieId}")
    suspend fun updateMovie(@Path("movieId") movieId: Long, @Body movie: UpdateMovieRequest): Response<Movie>

    @Multipart
    @PATCH("movies/{movieId}/picture")
    suspend fun uploadMoviePoster(
        @Path("movieId") movieId: Long,
        @Part file: MultipartBody.Part
    ): ResponseBody

    /**
     * Booking
     */

    @GET("screenings")
    suspend fun getScreenings(): Response<List<ScreeningDto>>

    @GET("movieHalls")
    suspend fun getMovieHalls(): Response<List<MovieHallDto>>

    @GET("seats/hall/{hallId}")
    suspend fun getSeatsByHall(@Path("hallId") hallId: Long): Response<List<SeatDto>>

    @POST("bookings")
    suspend fun createBooking(@Body req: CreateBookingRequest): Response<ResponseBody>

    @DELETE("bookings/{bookingId}")
    suspend fun cancelBooking(@Path("bookingId") bookingId: Long): Response<ResponseBody>

    @GET("bookings/screening/{screeningId}/booked-seats")
    suspend fun getBookedSeatsForScreening(
        @Path("screeningId") screeningId: Long
    ): Response<List<Long>>

    @GET("bookings")
    suspend fun getMyBookings(): Response<List<BookingDto>>


    @POST("friends/request")
    suspend fun sendFriendRequest(
        @Body req: SendFriendRequestRequest
    ): Response<FriendRequestDto>

    @GET("friends/requests")
    suspend fun getPendingFriendRequests(): Response<List<FriendRequestDto>>

    @POST("friends/request/{id}/accept")
    suspend fun acceptFriendRequest(@Path("id") id: Long): Response<FriendRequestDto>

    @POST("friends/request/{id}/reject")
    suspend fun rejectFriendRequest(@Path("id") id: Long): Response<FriendRequestDto>

    @GET("friends/list")
    suspend fun getFriendsList(): Response<List<String>>

    @GET("users/{email}/profile")
    suspend fun getFriendProfile(@Path("email") email: String): Response<FriendProfileDto>

    @POST("friends/invite")
    suspend fun inviteFriendToMovie(
        @Body req: MovieInviteRequest
    ): Response<MovieInvitationDto>

    @GET("movies/invitations/sent")
    suspend fun getSentMovieInvitations(): Response<List<MovieInvitationDto>>

    @GET("movies/invitations/received")
    suspend fun getReceivedMovieInvitations(): Response<List<MovieInvitationDto>>

    @POST("movies/invitations/{id}/accept")
    suspend fun acceptMovieInvitation(@Path("id") id: Long): Response<MovieInvitationDto>

    @POST("movies/invitations/{id}/reject")
    suspend fun rejectMovieInvitation(@Path("id") id: Long): Response<MovieInvitationDto>


}
